package org.gradleplugins;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DumpRestApi implements GradlePluginPortalVisitor {
    private final File restApiDirectory;
    private final Gson gson = new GsonBuilder().create();
    private Map<String, String> plugins = new HashMap<>();

    DumpRestApi(File restApiDirectory) {
        this.restApiDirectory = restApiDirectory;
    }

    @Override
    public void visit(URL portalUrl) {

    }

    @Override
    public GradlePluginVisitor visitPlugin(String pluginId, String description) {
        plugins.put(pluginId, "https://portal-api.gradleplugins.org/plugins/" + pluginId + "/");

        PluginData data = new PluginData();
        data.pluginId = pluginId;
        data.description = description;

        return new GradlePluginVisitor() {
            @Override
            public void visitVersion(String version, String notation) {
                data.versions.put(version, "https://portal-api.gradleplugins.org/plugins/" + pluginId + "/" + version + "/");

                try {
                    PluginVersionData data = new PluginVersionData();
                    data.pluginId = pluginId;
                    data.version = version;
                    data.notation = notation;

                    File result = new File(restApiDirectory, "plugins/" + pluginId + "/" + version + "/index.html");
                    result.getParentFile().mkdirs();
                    FileUtils.writeStringToFile(result, gson.toJson(data), Charset.defaultCharset());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void visitLatestVersion(String version) {
                data.latestVersion = "https://portal-api.gradleplugins.org/plugins/" + pluginId + "/" + version + "/";
            }

            @Override
            public void visitEnd() {
                try {
                    File result = new File(restApiDirectory, "plugins/" + pluginId + "/index.html");
                    result.getParentFile().mkdirs();
                    FileUtils.writeStringToFile(result, gson.toJson(data), Charset.defaultCharset());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public void visitEnd() {
        File result = new File(restApiDirectory, "plugins/index.html");
        result.getParentFile().mkdirs();
        try {
            FileUtils.writeStringToFile(result, gson.toJson(plugins), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class PluginData {
        String pluginId;
        String description;
        String latestVersion;
        Map<String, String> versions = new HashMap<>();
    }

    private class PluginVersionData {
        String pluginId;
        String version;
        String notation;
    }
}
