package org.gradleplugins;

public interface GradlePluginVisitor {
    void visitVersion(String version, String notation);

    void visitLatestVersion(String version);

    void visitEnd();
}
