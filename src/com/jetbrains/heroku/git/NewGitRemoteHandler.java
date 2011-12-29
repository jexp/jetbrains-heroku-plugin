package com.jetbrains.heroku.git;

import com.intellij.notification.impl.NotificationSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author mh
 * @since 19.12.11
 */
public class NewGitRemoteHandler implements GitRemoteHandler{
    public void updateRepository(Project project) {
        getRepository(project).update(GitRepository.TrackedTopic.CONFIG);
    }

    public GitRemoteInfo findRemote(String pattern, final Project project) {
        if (pattern == null) return null;
        final List<GitRemoteInfo> remotes = getRemotes(project);
        for (GitRemoteInfo remote : remotes) {
            if (remote.getUrl().matches(pattern) || remote.getName().matches(pattern)) return remote;
        }
        return null;
    }

    @Override
    public GitRemoteInfo findOrigin(String origin, Project project) {
        if (origin == null) return null;
        final List<GitRemoteInfo> remotes = getRemotes(project);
        for (GitRemoteInfo remote : remotes) {
            if (remote.getName().equalsIgnoreCase(origin)) return remote;
        }
        return null;
    }

    public List<GitRemoteInfo> getRemotes(final Project project) {
        final GitRepository repo = getRepository(project);
        if (repo == null) return Collections.emptyList();
        final ArrayList<GitRemoteInfo> result = new ArrayList<GitRemoteInfo>();
        for (GitRemote remote : repo.getRemotes()) {
            result.add(new NewGitRemoteInfo(remote));
        }
        return result;
    }

    @Override
    public boolean isGitEnabled(Project project) {
        return getRepository(project)!=null;
    }

    private GitRepository getRepository(final Project project) {
        final GitRepositoryManager repositoryManager = GitRepositoryManager.getInstance(project);
        return repositoryManager.getRepositoryForRoot(project.getBaseDir());
    }

    private static class NewGitRemoteInfo implements GitRemoteInfo {

        private final GitRemote remote;

        public NewGitRemoteInfo(GitRemote remote) {
            this.remote = remote;
        }

        public String getName() {
            return remote.getName();
        }

        public String getUrl() {
            return remote.getFirstUrl();
        }

        @Override
        public String toString() {
            return getName()+":"+getUrl();
        }
    }
}
