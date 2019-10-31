package org.gradleplugins;

import java.net.URL;

public class TravisKeepAliveVisitor implements GradlePluginPortalVisitor {
    private final GradlePluginPortalVisitor delegate;

    TravisKeepAliveVisitor(GradlePluginPortalVisitor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void visit(URL portalUrl) {
        delegate.visit(portalUrl);
    }

    @Override
    public GradlePluginVisitor visitPlugin(String pluginId, String description) {
        System.out.println(String.format("Visiting plugin '%s'", pluginId));
        GradlePluginVisitor resultDelegate = delegate.visitPlugin(pluginId, description);
        return new GradlePluginVisitor() {
            @Override
            public void visitVersion(String version, String notation) {
                resultDelegate.visitVersion(version, notation);
            }

            @Override
            public void visitLatestVersion(String version) {
                resultDelegate.visitLatestVersion(version);
            }

            @Override
            public void visitEnd() {
                resultDelegate.visitEnd();
            }
        };
    }

    @Override
    public void visitEnd() {
        delegate.visitEnd();
    }
}
