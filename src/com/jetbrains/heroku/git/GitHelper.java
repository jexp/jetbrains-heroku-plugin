package com.jetbrains.heroku.git;

import com.heroku.api.App;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.heroku.notification.Notifications;
import com.jetbrains.heroku.notification.Type;
import git4idea.commands.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mh
 * @since 18.12.11
 */
public class GitHelper {
    private static final Logger LOG = Logger.getInstance(GitHelper.class);
    public static final String HEROKU_REMOTE = "heroku";
    public static final String HEROKU_DEFAULT_BRANCH = "master";

    private final static GitRemoteHandler remoteHandler = createRemoteHandler();

    private static GitRemoteHandler createRemoteHandler() {
            try {
                Class.forName("git4idea.repo.GitRepositoryManager");
                return createRemoteHandler("GitRemoteHandler11");
            } catch(Exception cnfe2) {
                //throw new IllegalStateException("Incorrect API version, need at least: ");
                return createRemoteHandler("GitRemoteHandler10CE");
            }
    }

    private static GitRemoteHandler createRemoteHandler(final String className)  {
        try {
            return (GitRemoteHandler)Class.forName(GitRemoteHandler.class.getPackage().getName()+"."+className).newInstance();
        } catch (Exception e) {
            LOG.warn("Error creating instance of class " + className + " " + e.getMessage());
            return null;
        }
    }

    public static boolean addHerokuRemote(Project project, String remoteUrl) {
        final GitRemoteInfo herokuOrigin = findHerokuOrigin(project);
        final GitSimpleHandler addRemoteHandler = new GitSimpleHandler(project, project.getBaseDir(), GitCommand.REMOTE);
        addRemoteHandler.setNoSSH(true);
        if (herokuOrigin!=null) {
            LOG.warn("replacing remote "+herokuOrigin+" with "+remoteUrl);
            addRemoteHandler.addParameters("set-url", herokuOrigin.getName(), remoteUrl);
        }
        else {
            LOG.info("adding remote "+HEROKU_REMOTE+" with "+remoteUrl);
            addRemoteHandler.addParameters("add", HEROKU_REMOTE, remoteUrl);
        }
        try {
            addRemoteHandler.run();
            Notifications.notifyMessage(project, "Added Heroku Remote", "Heroku remote <code>" + remoteUrl + "</code> added to project " + project.getName(), Type.INFORMATION, true, null);
            remoteHandler.updateRepository(project);
            return true;
        } catch (VcsException e) {
            LOG.error("error adding remote " + remoteUrl, e);
            Notifications.notifyError(project, "Error adding Remote", "Couldn't add remote <code>" + remoteUrl + "</code>", true, e);
            return false;
        }
    }
    public static boolean removeHerokuRemote(Project project, GitRemoteInfo remoteInfo) {
        final GitSimpleHandler removeRemoteHandler = new GitSimpleHandler(project, project.getBaseDir(), GitCommand.REMOTE);
        removeRemoteHandler.setNoSSH(true);
        LOG.info("removing remote "+remoteInfo);
        removeRemoteHandler.addParameters("rm", remoteInfo.getName());
        try {
            removeRemoteHandler.run();
            Notifications.notifyMessage(project, "Removed Heroku Remote", "Heroku remote <code>" + remoteInfo + "</code> added to project " + project.getName(), Type.INFORMATION, true, null);
            remoteHandler.updateRepository(project);
            return true;
        } catch (VcsException e) {
            LOG.error("error removing remote " + remoteInfo, e);
            Notifications.notifyError(project, "Error Removing Remote", "Couldn't remove remote <code>" + remoteInfo + "</code>", true, e);
            return false;
        }
    }

    public static GitRemoteInfo findRemote(String pattern, final Project project) {
        if (pattern == null) return null;
        return remoteHandler.findRemote(pattern,project);
    }
    public static GitRemoteInfo findHerokuOrigin(final Project project) {
        return remoteHandler.findRemote(".*heroku.*", project);
    }

    private static List<GitRemoteInfo> getRemotes(final Project project) {
        return remoteHandler.getRemotes(project);
    }
    public static boolean isGitEnabled(final Project project) {
        return remoteHandler.isGitEnabled(project);
    }


    public static void pushToHeroku(Project project) {
        final VirtualFile vcsRoot = project.getBaseDir();
        final GitLineHandler handler = new GitLineHandler(project, vcsRoot, GitCommand.PUSH);
        final GitRemoteInfo herokuRemote = findRemote(".*heroku.*", project);
        String remoteName = herokuRemote==null ? HEROKU_REMOTE : herokuRemote.getName();
        final List<String> messages=new ArrayList<String>();
        final String description = "Deploying project " + project.getBaseDir() + " to Heroku remote " + herokuRemote+"\n";
        messages.add(description);

        handler.addParameters("-v", remoteName, HEROKU_DEFAULT_BRANCH);
        handler.addLineListener(new GitLineHandlerAdapter() {
            @Override
            public void onLineAvailable(String line, Key outputType) {
                messages.add(line+"\n");
            }

            @Override
            public void processTerminated(int exitCode) {
                messages.add("exit code " + exitCode+"\n");
            }

            @Override
            public void startFailed(Throwable exception) {
                messages.add("start failed with " + exception.getMessage()+"\n");
            }
        });
        LOG.info("git push to heroku vcs-root " + vcsRoot + " remote " + remoteName);
        // trackPushRejectedAsError(handler, "Rejected push (" + vcsRoot.getPresentableUrl() + "): ");
        final int exitCode = GitHandlerUtil.doSynchronously(handler, "deploying project to heroku", "git push");
        final boolean isError = exitCode != 0;
        Notifications.notifyMessages(project,"Deploy to Heroku",description,isError ? Type.ERROR : Type.INFORMATION, isError,messages);
    }

    /**
     * Install listener that tracks rejected push branch operations as errors
     *
     * @param handler the handler to use
     * @param prefix  the prefix for errors
     */
    public static void trackPushRejectedAsError(final GitLineHandler handler, final String prefix) {
        handler.addLineListener(new GitLineHandlerAdapter() {
            @Override
            public void onLineAvailable(final String line, final Key outputType) {
                if (outputType == ProcessOutputTypes.STDERR && line.startsWith(" ! [")) {
                    //noinspection ThrowableInstanceNeverThrown
                    handler.addError(new VcsException(prefix + line));
                }
            }
        });
    }

    public static GitRemoteInfo attachRemote(Project project, App app) {
        final String gitUrl = app.getGitUrl();
        final GitRemoteInfo remote = findRemote(gitUrl, project);
        if (remote == null) {
            LOG.info("no remote found for url " + gitUrl+" adding to project root "+project.getBaseDir());
            addHerokuRemote(project, gitUrl);
            return findRemote(gitUrl, project);
        }
        LOG.info("found remote for url " + gitUrl+" remote "+remote);
        return remote;
    }

    public static boolean removeRemote(Project project, App app) {
        final String gitUrl = app.getGitUrl();
        final GitRemoteInfo remote = findRemote(gitUrl, project);
        if (remote == null) {
            LOG.warn("no remote found for url " + gitUrl);
            return false;
        }
        removeHerokuRemote(project, remote);
        LOG.info("removed remote for url " + gitUrl);
        return true;
    }
}
