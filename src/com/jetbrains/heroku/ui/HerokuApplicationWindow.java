package com.jetbrains.heroku.ui;

import com.heroku.api.App;
import com.heroku.api.Heroku;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.jetbrains.heroku.git.GitHelper;
import com.jetbrains.heroku.git.GitRemoteInfo;
import com.jetbrains.heroku.service.HerokuProjectService;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static com.jetbrains.heroku.ui.GuiUtil.label;
import static com.jetbrains.heroku.ui.GuiUtil.link;

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
        builder.append("Owner", label(app.getOwnerEmail()));
        builder.append("Id", label(app.getId()));
        builder.append("Domain", link(app.getDomainName()));
        builder.append("Requested Stack", label(app.getRequestedStack()));
        builder.append("Stack", label(app.getStack()));
        builder.append("Dynos", label(getDynos(app)));
        builder.append("Workers", label(getWorkers(app)));
        builder.append("Created At", label(app.getCreatedAt()));
        builder.append("Create Status", label(app.getCreateStatus()));
        builder.append("Repo Size", label(app.getRepoSize()));
        builder.append("Slug Size", label(app.getSlugSize()));

        builder.append("Remote", label(getGitRemote(app)));

        return builder.getPanel();
    }

    private int getWorkers(App app) {
        if (app.getStack().equalsIgnoreCase(Heroku.Stack.Cedar.name())) return herokuProjectService.getProcesses("worker").size();
        return app.getWorkers();
    }

    private int getDynos(App app) {
        if (app.getStack().equalsIgnoreCase(Heroku.Stack.Cedar.name())) return herokuProjectService.getProcesses("web").size();
        return app.getDynos();
    }

    private String getGitRemote(App app) {
        final String gitUrl = app.getGitUrl();
        GitRemoteInfo herokuRemote = GitHelper.findRemote(gitUrl, herokuProjectService.getProject());
        if (herokuRemote == null) return gitUrl;
        return herokuRemote.getName() + " : " + herokuRemote.getUrl();
    }

    public void update() {
        setEnabled(herokuProjectService.isHerokuProject());

        view.removeAll();
        final JComponent applicationView = createApplicationView();
        if (applicationView!=null) view.add(applicationView,BorderLayout.CENTER);
    }

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
/*                new AnAction("Change Stack", "", icon("/runConfigurations/scrollToStackTrace.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        final Heroku.Stack[] stacks = Heroku.Stack.values();
                        final String[] stackNames = new String[stacks.length];
                        final String appStack = herokuProjectService.getApp().getStack();
                        int initial=0;
                        for (int i = 0; i < stackNames.length; i++) {
                            stackNames[i]= stacks[i].name();
                            if (appStack.startsWith(stackNames[i])) {
                                initial=i;
                            }
                        }
                        final int newStack = Messages.showChooseDialog(herokuProjectService.getProject(), "Choose new stacks", "Stack", Messages.getQuestionIcon(), stackNames, stackNames[initial]);
                        if (newStack < 0 || newStack >= stacks.length || newStack==initial) return;
                        herokuProjectService.changeStack(stacks[newStack]);
                    }
                }
*/
        );
    }

    public HerokuApplicationWindow(HerokuProjectService herokuProjectService) {
        super(herokuProjectService);
    }
}
