package com.jetbrains.heroku.ui;

import com.heroku.api.App;
import com.heroku.api.request.log.LogStreamResponse;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.jetbrains.heroku.git.GitHelper;
import com.jetbrains.heroku.git.GitRemoteInfo;
import com.jetbrains.heroku.service.HerokuProjectService;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author mh
 * @since 26.12.11
 */
public class HerokuApplicationWindow extends HerokuToolWindow {

    private JComponent view;
    @Override
    protected void setWindowInfo(ContentInfo contentInfo) {
        contentInfo.describe("Application", "/icons/heroku_icon_16.png", "Application Information");
    }

    @Override
    protected JComponent createContentPane() {
        if (!herokuProjectService.isHerokuProject()) return null;
        view = new JPanel(new BorderLayout());
        update();
        return view;
    }

    private JComponent createApplicationView() {
        if (!herokuProjectService.isHerokuProject()) return null;

        final App app = herokuProjectService.getApp();

        final DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "right:pref, 6dlu, right:pref, 10dlu, right:pref, 6dlu, right:pref, pref:grow", // columns
                "pref"));// rows
        builder.appendSeparator("Heroku Application: " + herokuProjectService.getHerokuAppName());
        builder.append("URL", link(app.getWebUrl()));
        builder.append("Owner", new JLabel(app.getOwnerEmail()));
        builder.append("Id", new JLabel(app.getId()));
        builder.append("Domain", link(app.getDomainName()));
        builder.append("Requested Stack", new JLabel("" + app.getRequestedStack()));
        builder.append("Stack", new JLabel(app.getStack()));
        builder.append("Dynos", new JLabel("" + app.getDynos()));
        builder.append("Workers", new JLabel("" + app.getWorkers()));
        builder.append("Created At", new JLabel("" + app.getCreatedAt()));
        builder.append("Create Status", new JLabel("" + app.getCreateStatus()));
        builder.append("Repo Size", new JLabel("" + app.getRepoSize()));
        builder.append("Slug Size", new JLabel("" + app.getSlugSize()));

        builder.append("Remote", new JLabel(getGitRemote(app)));

        return builder.getPanel();
    }

    private String getGitRemote(App app) {
        final String gitUrl = app.getGitUrl();
        GitRemoteInfo herokuRemote = GitHelper.findRemote(gitUrl, herokuProjectService.getProject());
        if (herokuRemote == null) return gitUrl;
        return herokuRemote.getName() + " : " + herokuRemote.getUrl();
    }

    private void update() {
        view.removeAll();
        view.add(createApplicationView(),BorderLayout.CENTER);
    }

        /*
        builder.append("Remote", new JButton(new AbstractAction("Add: " + gitUrl) {
            public void actionPerformed(ActionEvent actionEvent) {
                final GitRemoteInfo herokuRemote = attachRemote(project, app);
                if (herokuRemote != null) {
                    setEnabled(false);
                    this.putValue(NAME, herokuRemote.getName() + " : " + herokuRemote.getUrl());
                    this.putValue(SHORT_DESCRIPTION, herokuRemote.getName() + " : " + herokuRemote.getUrl());
                }
            }
        }));
        */
    @Override
    protected List<AnAction> createActions() {
        return Arrays.asList(
                new AnAction("Restart", "", icon("/general/toolWindowRun.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        herokuProjectService.restartApplication();
                        HerokuApplicationWindow.this.update();
                    }
                },
                new AnAction("Stop", "", icon("/actions/suspend.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        herokuProjectService.stopApplication();
                        HerokuApplicationWindow.this.update();
                    }
                },
                new AnAction("Deploy", "", icon("/actions/resume.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        GitHelper.pushToHeroku(herokuProjectService.getProject());
                        HerokuApplicationWindow.this.update();
                    }
                },
                new AnAction("Update", "", icon("/actions/sync.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        HerokuApplicationWindow.this.update();
                    }
                }
        );
    }

    public HerokuApplicationWindow(HerokuProjectService herokuProjectService) {
        super(herokuProjectService);
    }
}
