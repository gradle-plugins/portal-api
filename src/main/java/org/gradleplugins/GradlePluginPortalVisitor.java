package org.gradleplugins;

import java.net.URL;

public interface GradlePluginPortalVisitor {
    void visit(URL portalUrl);

    GradlePluginVisitor visitPlugin(String pluginId, String description);

    void visitEnd();
}
