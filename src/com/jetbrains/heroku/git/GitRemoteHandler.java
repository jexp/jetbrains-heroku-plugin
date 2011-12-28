package com.jetbrains.heroku.git;

import com.intellij.openapi.project.Project;

import java.util.List;

/**
* @author mh
* @since 19.12.11
*/
interface GitRemoteHandler {
    public void updateRepository(Project project);

    GitRemoteInfo findRemote(String gitUrl, Project project);
    GitRemoteInfo findOrigin(String origin, Project project);

    List<GitRemoteInfo> getRemotes(Project project);

    boolean isGitEnabled(Project project);
}
