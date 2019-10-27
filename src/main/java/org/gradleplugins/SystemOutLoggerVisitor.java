package org.gradleplugins;

import java.net.URL;

public class SystemOutLoggerVisitor implements GradlePluginPortalVisitor {
    private final GradlePluginPortalVisitor delegate;

    SystemOutLoggerVisitor(GradlePluginPortalVisitor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void visit(URL portalUrl) {
        System.out.println("Visiting " + portalUrl.toString());
        delegate.visit(portalUrl);
    }

    @Override
    public GradlePluginVisitor visitPlugin(String pluginId, String description) {
        System.out.println(String.format("Visiting plugin '%s'", pluginId));
        GradlePluginVisitor resultDelegate = delegate.visitPlugin(pluginId, description);
        return new GradlePluginVisitor() {
            @Override
            public void visitVersion(String version, String notation) {
                System.out.println(String.format("Visiting plugin '%s' version '%s'", pluginId, version));
                resultDelegate.visitVersion(version, notation);
            }

            @Override
            public void visitLatestVersion(String version) {
                System.out.println(String.format("Visiting plugin '%s' latest version '%s'", pluginId, version));
                resultDelegate.visitLatestVersion(version);
            }

            @Override
            public void visitEnd() {
                System.out.println(String.format("Visiting plugin '%s' finished", pluginId));
                resultDelegate.visitEnd();
            }
        };
    }

    @Override
    public void visitEnd() {
        System.out.println("Visiting portal finished");
        delegate.visitEnd();
    }
}
