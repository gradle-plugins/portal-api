/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradleplugins;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.annotation.Nullable;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradlePluginPortal {
    private final URL portalUrl;
    private ExecutorService executor;
    private Vector<Future<?>> futures;

    GradlePluginPortal(URL portalUrl) {
        this.portalUrl = portalUrl;
    }

    public static GradlePluginPortal connect(URL pluginPortalUrl) {
        return new GradlePluginPortal(pluginPortalUrl);
    }

    private void submit(Runnable runnable) {
        assert executor != null;
        assert futures != null;
        futures.add(executor.submit(tryTask(runnable)));
    }

    public void visit(GradlePluginPortalVisitor visitor) {
        executor = Executors.newFixedThreadPool(20);
        futures = new Vector<>();
        try {
            visitor.visit(portalUrl);
            submit(() -> new SearchPage(portalUrl).visit(visitor));
//            for (Iterator<PortalSearchPage> it = iterator(); it.hasNext(); ) {
//                PortalSearchPage page = it.next();
//                submit(() -> page.visit(visitor));
//            }
        } finally {
            while (!futures.isEmpty()) {
                while (!futures.firstElement().isDone()) {
                    Thread.yield();
                }
                futures.remove(0);
            }
            visitor.visitEnd();
            executor.shutdown();
            try {
                executor.awaitTermination(2, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            executor = null;
        }
    }

//    private Iterator<PortalSearchPage> iterator() {
//        PortalSearchPage s = new PortalSearchPage() {
//            @Override
//            public void visit(GradlePluginPortalVisitor visitor) {
//                throw new UnsupportedOperationException();
//            }
//
//            @Nullable
//            @Override
//            public PortalSearchPage getNextPage() {
//                return new SearchPage(portalUrl);
//            }
//        };
//        return new Iterator<PortalSearchPage>() {
//            PortalSearchPage nextPage = s;
//
//            @Override
//            public boolean hasNext() {
//                return nextPage.getNextPage() != null;
//            }
//
//            @Override
//            public PortalSearchPage next() {
//                return (nextPage = nextPage.getNextPage());
//            }
//        };
//    }

    private abstract class PluginPortalPage {
        private Document document;
        protected final URL pageUrl;

        PluginPortalPage(URL pageUrl) {
            this.pageUrl = pageUrl;
        }

        protected Document loadDocumentIfNeeded() {
            if (document == null) {
                document = fetch(pageUrl);
            }
            return document;
        }

        protected URL toPluginPortalUrl(String path) {
            try {
                if (path.startsWith("/")) {
                    return new URL(getRootUrl().toString() + path);
                }

                int lastSlashIndex = pageUrl.toString().lastIndexOf('/');
                return new URL(pageUrl.toString().substring(0, lastSlashIndex) + "/" + path);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Document fetch(URL url) {
        try {
            return Jsoup.parse(new String(IOUtils.toByteArray(url)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static final Pattern PLUGIN_ID_AND_VERSION_CODE_PATTERN = Pattern.compile("id \"(?<pluginId>[a-zA-Z0-9_\\-.]+)\" version \"(?<pluginVersion>[a-zA-Z0-9_\\-.+]+)\"");
    private static final Pattern PLUGIN_NOTATION_CODE_PATTERN = Pattern.compile("classpath \"(?<groupId>[a-zA-Z0-9_\\-.]+):(?<artifactId>[a-zA-Z_\\-.]+):(?<version>[a-zA-Z0-9_\\-.+]+)\"");

    private class PluginPage extends PluginPortalPage {
        PluginPage(URL pageUrl) {
            super(pageUrl);
        }

        public void visit(GradlePluginVisitor visitor) {
            Elements codeElements = loadDocumentIfNeeded().select("pre > code[class=language-groovy]");
            assert codeElements.size() == 2;

            String pluginDslCode = codeElements.first().text();
            Matcher pluginIdAndVersionCodeMatcher = PLUGIN_ID_AND_VERSION_CODE_PATTERN.matcher(pluginDslCode);
            if (!pluginIdAndVersionCodeMatcher.find()) {
                System.err.println(String.format("Parsing error of plugin DSL code in '%s'", pageUrl.toString()));
                return;
            }
            String pluginId = pluginIdAndVersionCodeMatcher.group("pluginId");
            String pluginVersion = pluginIdAndVersionCodeMatcher.group("pluginVersion");

            String buildscriptCode = codeElements.last().text();
            Matcher pluginNotationCodeMatcher = PLUGIN_NOTATION_CODE_PATTERN.matcher(buildscriptCode);
            if (!pluginNotationCodeMatcher.find()) {
                System.err.println(String.format("Parsing error of buildscript dependency code in '%s'", pageUrl.toString()));
                return;
            }
            String groupId = pluginNotationCodeMatcher.group("groupId");
            String artifactId = pluginNotationCodeMatcher.group("artifactId");
            String version = pluginNotationCodeMatcher.group("version");

            visitor.visitVersion(pluginVersion, String.format("%s:%s:%s", groupId, artifactId, version));
        }
    }

    private class LatestPluginPage extends PluginPage {
        LatestPluginPage(URL pageUrl) {
            super(pageUrl);
        }

        public void visit(GradlePluginVisitor visitor) {
            super.visit(visitor);

            Elements otherVersionElements = loadDocumentIfNeeded().select("div[class*=other-versions] > ul > li > a");

            Vector<Future<?>> fs = new Vector<>();
            for (Element otherVersionElement : otherVersionElements) {
                fs.add(executor.submit(tryTask(() ->
                        new PluginPage(toPluginPortalUrl(otherVersionElement.attr("href"))).visit(visitor)
                )));
            }
            submit(() -> {
                while (!fs.isEmpty()) {
                    while (!fs.firstElement().isDone()) {
                        Thread.yield();
                    }
                    fs.remove(0);
                }
                visitor.visitEnd();
            });
        }
    }

    private interface PortalSearchPage {
        @Nullable
        PortalSearchPage getNextPage();

        void visit(GradlePluginPortalVisitor visitor);
    }

    private class SearchPage extends PluginPortalPage implements PortalSearchPage {
        public SearchPage(URL searchPageUrl) {
            super(searchPageUrl);
        }

        @Override
        public void visit(GradlePluginPortalVisitor visitor) {
            Elements pluginElements = loadDocumentIfNeeded().select("#search-results > tbody > tr");

            if (!hasPlugins(pluginElements)) {
                return;
            }

            submit(() -> getNextPage().visit(visitor));

            for (Element it : pluginElements) {
                Elements pluginIdAndUrlElements = it.select("td > h3 > a");
                assert pluginIdAndUrlElements.size() == 1;
                URL pluginPortalUrl = toPluginPortalUrl(pluginIdAndUrlElements.first().attr("href"));
                String pluginId = pluginIdAndUrlElements.first().text();

                Elements descriptionElements = it.select("td > p");
                assert descriptionElements.size() == 1;
                String description = descriptionElements.first().text();

                Elements latestVersionElements = it.select("td[class=version] > span[class=latest-version]");
                assert latestVersionElements.size() == 1;
                String latestVersion = latestVersionElements.first().text();

                GradlePluginVisitor pluginVisitor = visitor.visitPlugin(pluginId, description);
                pluginVisitor.visitLatestVersion(latestVersion);

                submit(() -> new LatestPluginPage(pluginPortalUrl).visit(pluginVisitor));
            }
        }

        private boolean hasPlugins(Elements e) {
            Elements noPlugin = e.first().select("td > em");
            if (noPlugin.size() == 1) {
                assert noPlugin.first().text().equals("No plugins found.");
                return false;
            }
            return true;
        }

        @Nullable
        @Override
        public PortalSearchPage getNextPage() {
            if (hasMorePage()) {
                return new SearchPage(getNextPageUrl());
            }
            return null;
        }

        private static final String SEARCH_PAGE_NAVIGATION_XPATH = "div[class=page-link clearfix] > a";

        private boolean hasMorePage() {
            return loadDocumentIfNeeded().select(SEARCH_PAGE_NAVIGATION_XPATH).size() > 0;
        }

        @Nullable
        private URL getNextPageUrl() {
            Elements navigationElements = loadDocumentIfNeeded().select(SEARCH_PAGE_NAVIGATION_XPATH);
            if (navigationElements.size() == 0) {
                return null;
            } else if (navigationElements.size() == 2) {
                Element navigationElement = navigationElements.last();
                assert navigationElement.text().equals("Next");
                return toPluginPortalUrl(navigationElement.attr("href"));
            } else {
                assert navigationElements.size() == 1;
                Element navigationElement = navigationElements.first();
                if (navigationElement.text().equals("Next")) {
                    return toPluginPortalUrl(navigationElement.attr("href"));
                }
                return null;
            }
        }
    }

    private Callable<Object> tryTask(Callable<Object> c) {
        return new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    return c.call();
                } catch (Exception e) {
                    System.out.println("FAILING ...");
                    e.printStackTrace();
                    throw e;
                }
            }
        };
    }

    private Runnable tryTask(Runnable r) {
        return () -> {
            try {
                r.run();
            } catch (Exception e) {
                System.out.println("FAILING ...");
                e.printStackTrace();
                throw e;
            }
        };
    }

    private URL getRootUrl() {
        int lastSlashIndex = portalUrl.toString().lastIndexOf('/');
        try {
            return new URL(portalUrl.toString().substring(0, lastSlashIndex));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}