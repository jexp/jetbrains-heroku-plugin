package com.heroku.idea.git;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.checkout.GitCheckoutProvider;
import git4idea.commands.*;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import git4idea.ui.GitUIUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author mh
 * @since 18.12.11
 */
public class GitHelper {
    private static final Logger LOG = Logger.getInstance(GitCheckoutProvider.class);
    public static final String HEROKU_REMOTE = "heroku";
    public static final String HEROKU_DEFAULT_BRANCH = "master";

    public static boolean addHerokuRemote(Project project, String remoteUrl) {
        final GitSimpleHandler addRemoteHandler = new GitSimpleHandler(project, project.getBaseDir(), GitCommand.REMOTE);
        addRemoteHandler.setNoSSH(true);
        addRemoteHandler.addParameters("add", HEROKU_REMOTE, remoteUrl);
        try {
            addRemoteHandler.run();
            GitUIUtil.notifyMessage(project, "Added Heroku Remote", "Heroku remote <code>" + remoteUrl + "</code> added to project " + project.getName(), NotificationType.INFORMATION, true, null);
            getRepository(project).update(GitRepository.TrackedTopic.CONFIG);
            return true;
        } catch (VcsException e) {
            LOG.info("addRemote ", e);
            GitUIUtil.notifyError(project, "Couldn't clone", "Couldn't add remote <code>" + remoteUrl + "</code>", true, e);
            return false;
        }
    }

    public static GitRemote findRemote(String gitUrl, final Project project) {
        if (gitUrl == null) return null;
        final List<GitRemote> remotes = getRemotes(project);
        for (GitRemote remote : remotes) {
            if (remote.getFirstUrl().equals(gitUrl)) return remote;
        }
        return null;
    }

    private static List<GitRemote> getRemotes(final Project project) {
        final GitRepository repo = getRepository(project);
        if (repo == null) return Collections.emptyList();
        return new ArrayList<GitRemote>(repo.getRemotes());
    }

    public static GitRepository getRepository(final Project project) {
        final GitRepositoryManager repositoryManager = GitRepositoryManager.getInstance(project);
        return repositoryManager.getRepositoryForRoot(project.getBaseDir());
    }


    @Nullable
    public static void pushToHeroku(Project project) {
        final VirtualFile vcsRoot = project.getBaseDir();
        final GitLineHandler handler = new GitLineHandler(project, vcsRoot, GitCommand.PUSH);
        handler.addParameters("-v", HEROKU_REMOTE, HEROKU_DEFAULT_BRANCH);
        trackPushRejectedAsError(handler, "Rejected push (" + vcsRoot.getPresentableUrl() + "): ");
        GitHandlerUtil.doSynchronously(handler, "deploying project to heroku", "git push");
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
}
