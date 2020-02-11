package org.gradleplugins.scrapper;

import java.net.URL;

public interface GradlePluginPortalReader {
    GradlePluginPortalPageReader readSearchPage(URL pageUrl);

    GradlePluginPortalPageReader readPluginPage(URL pageUrl);
}
