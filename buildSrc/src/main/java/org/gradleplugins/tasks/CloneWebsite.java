package org.gradleplugins.tasks;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public abstract class CloneWebsite extends DefaultTask {
    public abstract DirectoryProperty getRepositoryDirectory();

    public abstract Property<String> getRepositoryUri();

    @TaskAction
    private void doSync() {
        try {
            FileUtils.deleteDirectory(getRepositoryDirectory().get().getAsFile());
            Git git = Git.cloneRepository()
                    .setURI(getRepositoryUri().get())
                    .setDirectory(getRepositoryDirectory().get().getAsFile())
                    .setBranch("gh-pages")
                    .call();
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        }
    }
}
