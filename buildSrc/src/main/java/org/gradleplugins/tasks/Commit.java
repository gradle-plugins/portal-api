package org.gradleplugins.tasks;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public abstract class Commit extends DefaultTask {
    public abstract DirectoryProperty getRepositoryDirectory();

    public abstract Property<String> getUsername();

    public abstract Property<String> getPassword();

    @TaskAction
    private void doCommit() {
        try {
            Git git = Git.open(getRepositoryDirectory().get().getAsFile());

            git.add().addFilepattern(".").call();
            git.commit().setAuthor("Daniel Lacasse", "daniel@lacasse.io").setMessage("Added testfile").call();

            git.pull().setCredentialsProvider(new UsernamePasswordCredentialsProvider(getUsername().get(), getPassword().get())).setRebase(true).call();
            git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(getUsername().get(), getPassword().get())).call();
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        }
    }
}
