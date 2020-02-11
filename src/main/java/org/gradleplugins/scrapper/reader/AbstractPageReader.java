package org.gradleplugins.scrapper.reader;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;

public class AbstractPageReader {
    private Document document;
    protected final URL pageUrl;

    AbstractPageReader(URL pageUrl) {
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

    protected URL toPluginPortalUrl(String path) {
        try {
//            if (path.startsWith("/")) {
//                return new URL(getRootUrl().toString() + path);
//            }

            int lastSlashIndex = pageUrl.toString().lastIndexOf('/');
            return new URL(pageUrl.toString().substring(0, lastSlashIndex) + "/" + path);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
