package org.gradleplugins.scrapper.reader;

import org.apache.commons.io.IOUtils;
import org.gradleplugins.GradlePluginPortalVisitor;
import org.gradleplugins.GradlePluginVisitor;
import org.gradleplugins.scrapper.GradlePluginPortalPageReader;
import org.gradleplugins.scrapper.GradlePluginPortalReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractPluginPageReader implements GradlePluginPortalPageReader {
    private static final Pattern PLUGIN_ID_AND_VERSION_CODE_PATTERN = Pattern.compile("id \"(?<pluginId>[a-zA-Z0-9_\\-.]+)\" version \"(?<pluginVersion>[a-zA-Z0-9_\\-.+]+)\"");
    private static final Pattern PLUGIN_NOTATION_CODE_PATTERN = Pattern.compile("classpath \"(?<groupId>[a-zA-Z0-9_\\-.]+):(?<artifactId>[a-zA-Z0-9_\\-.]+):(?<version>[a-zA-Z0-9_\\-.+]+)\"");

    private Document document;
    protected final URL pageUrl;

    AbstractPluginPageReader(URL pageUrl) {
        this.pageUrl = pageUrl;
    }

    protected Document loadDocumentIfNeeded() {
        if (document == null) {
            document = fetch(pageUrl);
        }
        return document;
    }

    private static Document fetch(URL url) {
        try {
            return Jsoup.parse(new String(IOUtils.toByteArray(url)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void accept(GradlePluginPortalVisitor visitor) {

    }

    public void accept(GradlePluginVisitor visitor) {
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
