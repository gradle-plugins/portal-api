package org.gradleplugins.scrapper;

import org.gradleplugins.GradlePluginPortalVisitor;

public interface GradlePluginPortalPageReader {
    void accept(GradlePluginPortalVisitor visitor);
}
