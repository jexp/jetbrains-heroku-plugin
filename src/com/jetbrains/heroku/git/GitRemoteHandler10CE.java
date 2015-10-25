package com.jetbrains.heroku.git;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import git4idea.GitRemote;
import git4idea.GitUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mh
 * @since 19.12.11
 */
public class GitRemoteHandler10CE implements GitRemoteHandler {
    public void updateRepository(Project project) {
    }

    public GitRemoteInfo findRemote(String pattern, Project project) {
        try {
            for (GitRemote remote : GitRemote.list(project, project.getBaseDir())) {
                if (remote.pushUrl().matches(pattern) || remote.name().matches(pattern)) return new OldGitRemoteInfo(remote);
            }
        } catch (VcsException e) {
            throw new RuntimeException("Error fetching git remotes", e);
        }
        return null;
    }

    @Override
    public GitRemoteInfo findOrigin(String origin, Project project) {
        try {
            for (GitRemote remote : GitRemote.list(project, project.getBaseDir())) {
                if (remote.name().equalsIgnoreCase(origin)) return new OldGitRemoteInfo(remote);
            }
        } catch (VcsException e) {
            throw new RuntimeException("Error fetching git remotes", e);
        }
        return null;
    }

    public List<GitRemoteInfo> getRemotes(Project project) {
        try {
            final ArrayList<GitRemoteInfo> result = new ArrayList<GitRemoteInfo>();
            for (GitRemote remote : GitRemote.list(project, project.getBaseDir())) {
                result.add(new OldGitRemoteInfo(remote));
            }
            return result;
        } catch (VcsException e) {
            throw new RuntimeException("Error fetching git remotes", e);
        }
    }

    private class OldGitRemoteInfo implements GitRemoteInfo {
        private final GitRemote remote;

        public OldGitRemoteInfo(GitRemote remote) {
            this.remote = remote;
        }

        public String getName() {
            return remote.name();
        }

        public String getUrl() {
            return remote.pushUrl();
        }
        @Override
        public String toString() {
            return getName()+":"+getUrl();
        }
    }

    @Override
    public boolean isGitEnabled(Project project) {
        return GitUtil.isUnderGit(project.getBaseDir());
    }
}
