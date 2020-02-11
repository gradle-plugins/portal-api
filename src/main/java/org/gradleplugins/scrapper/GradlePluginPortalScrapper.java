package org.gradleplugins.scrapper;

import org.gradleplugins.GradlePluginPortal;
import org.jsoup.nodes.Document;

import java.net.URL;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/*
GradlePluginPortalScrapper.read("https://plugins.gradle.org").accept(visitor)
 */

public class GradlePluginPortalScrapper {
    private final URL portalUrl;
    private ExecutorService executor;
    private Vector<Future<?>> futures;
    private final GradlePluginPortalReader reader;

    public GradlePluginPortalScrapper(URL portalUrl, GradlePluginPortalReader reader) {
        this.portalUrl = portalUrl;
        this.reader = reader;
    }

    private void submit(Runnable runnable) {
        assert executor != null;
        assert futures != null;
        futures.add(executor.submit(runnable));
    }



    public void accept(GradlePluginPortalVisitor visitor) {
        executor = Executors.newFixedThreadPool(20);
        futures = new Vector<>();
        try {
            reader.readSearchPage(portalUrl).accept(visitor);
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

    private PageReader newSearchPageReader(URL url) {
        return new SearchPageReader(url);
    }

    private interface PageReader {
        void accept(GradlePluginPortalVisitor visitor);
    }

    private class SearchPageReader implements PageReader {
        private final URL pageUrl;

        public SearchPageReader(URL pageUrl) {
            this.pageUrl = pageUrl;
        }

        @Override
        public void accept(GradlePluginPortalVisitor visitor) {
            submit(tryReading(() -> {

            }));
        }

        private Runnable tryReading(Runnable runnable) {
            return () -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    System.out.println("FAILING to read ... " + pageUrl.toString());
                    e.printStackTrace();
                    throw e;
                }
            };
        }
    }

//    Document readUrl(URL url);
}
