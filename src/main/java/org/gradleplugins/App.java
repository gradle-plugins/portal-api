/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.gradleplugins;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class App {
    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) throws MalformedURLException {
        GradlePluginPortal.connect(new URL("https://plugins.gradle.org/")).visit(new TravisKeepAliveVisitor(new DumpRestApi(new File(args[0]))));
    }
}
