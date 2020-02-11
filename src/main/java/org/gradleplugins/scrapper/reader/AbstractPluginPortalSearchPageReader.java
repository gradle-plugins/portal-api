package org.gradleplugins.scrapper.reader;

import org.gradleplugins.GradlePluginPortal;
import org.gradleplugins.GradlePluginPortalVisitor;
import org.gradleplugins.GradlePluginVisitor;
import org.gradleplugins.scrapper.GradlePluginPortalPageReader;
import org.gradleplugins.scrapper.GradlePluginPortalReader;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.annotation.Nullable;
import java.net.URL;

public abstract class AbstractPluginPortalSearchPageReader extends AbstractPageReader implements GradlePluginPortalPageReader {
    private static final String SEARCH_PAGE_NAVIGATION_XPATH = "div[class=page-link clearfix] > a";


    public AbstractPluginPortalSearchPageReader(URL pageUrl) {
        super(pageUrl);
    }

    @Override
    public void accept(GradlePluginPortalVisitor visitor) {
        Elements pluginElements = loadDocumentIfNeeded().select("#search-results > tbody > tr");

        if (!hasPlugins(pluginElements)) {
            visitor.visitEnd();
            return;
        }

        newSearchPageReader(getNextPageUrl()).accept(visitor);

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

            newLatestPluginPageReader(pluginPortalUrl, pluginId, latestVersion).accept(visitor);
        }
    }

    protected abstract GradlePluginPortalPageReader newLatestPluginPageReader(URL pluginPortalUrl, String pluginId, String latestVersion);

    protected abstract GradlePluginPortalPageReader newSearchPageReader(URL nextPageUrl);

    private boolean hasPlugins(Elements e) {
        Elements noPlugin = e.first().select("td > em");
        if (noPlugin.size() == 1) {
            assert noPlugin.first().text().equals("No plugins found.");
            return false;
        }
        return true;
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
