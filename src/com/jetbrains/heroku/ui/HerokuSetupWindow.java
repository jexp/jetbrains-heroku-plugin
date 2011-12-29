package com.jetbrains.heroku.ui;

import com.heroku.api.App;
import com.heroku.api.Heroku;
import com.intellij.ide.actions.StartUseVcsAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.jetbrains.heroku.git.GitHelper;
import com.jetbrains.heroku.git.GitRemoteInfo;
import com.jetbrains.heroku.notification.Notifications;
import com.jetbrains.heroku.service.HerokuProjectService;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import static com.jetbrains.heroku.ui.GuiUtil.table;

/**
 * @author mh
 * @since 26.12.11
 */
public class HerokuSetupWindow extends HerokuToolWindow {
    private final static Logger logger=Logger.getInstance(HerokuSetupWindow.class);

    private AtomicInteger selectedRow;
    private AppsTableModel tableModel;
    private Updateable panels;
    private JLabel remoteLabel;

    @Override
    protected void setWindowInfo(ContentInfo contentInfo) {
        contentInfo.describe("Setup", "/vcs/customizeView.png", "Application Setup");
    }

    @Override
    protected JComponent createContentPane() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(table(tableModel = new AppsTableModel(), selectedRow = new AtomicInteger(-1)),BorderLayout.CENTER);
        DefaultFormBuilder builder=new DefaultFormBuilder(new FormLayout("right:pref, 6dlu, pref","pref"));
        builder.append("Current Heroku-Git-Remote:", remoteLabel = new JLabel());
        panel.add(builder.getPanel(), BorderLayout.NORTH);
        update();
        return panel;
    }

    private List<App> load() {
        return herokuProjectService.getApplicationService().listApps();
    }

    public void update() {
        final GitRemoteInfo existingRemote = GitHelper.findRemote(".*heroku.*", herokuProjectService.getProject());
        final List<App> apps = load();
        tableModel.update(apps);
        final App appWithRemote = findAppForRemote(apps, existingRemote);
        tableModel.highlight(appWithRemote);
        representExistingRemote(existingRemote, appWithRemote!=null);
    }

    private void representExistingRemote(GitRemoteInfo existingRemote, boolean appExists) {
        if (existingRemote==null) {
            remoteLabel.setText(null);
            remoteLabel.setToolTipText(null);
            remoteLabel.setForeground(null);
        } else {
            final String remoteText = existingRemote.getName() + " -> " + existingRemote.getUrl();
            if (appExists) {
                remoteLabel.setText(remoteText);
                remoteLabel.setForeground(null);
                remoteLabel.setToolTipText("Git URL belongs to this Heroku Account!");
            } else {
                remoteLabel.setText(remoteText+" (other Account)");
                remoteLabel.setForeground(Color.RED);
                remoteLabel.setToolTipText("Git URL belongs to a different Heroku Account!");
            }
        }
    }

    private App findAppForRemote(List<App> apps, GitRemoteInfo remote) {
        if (remote==null) return null;
        final String remoteUrl = remote.getUrl();
        for (App app : apps) {
            if (app.getGitUrl().equals(remoteUrl)) return app;
        }
        return null;
    }

    @Override
    protected List<AnAction> createActions() {
        final List<AnAction> actions = Arrays.<AnAction>asList(
                new AnAction("Enable Git Integration", "Enable Git VCS integration for project", icon("/vcs/addToVcs.png")) {
                    @Override
                    public void update(AnActionEvent e) {
                        setEnabled(!GitHelper.isGitEnabled(herokuProjectService.getProject()));
                    }

                    public void actionPerformed(AnActionEvent anActionEvent) {
                        new StartUseVcsAction().actionPerformed(anActionEvent);
                        HerokuSetupWindow.this.update();
                        updatePanels();
                    }
                },
                // /general/getProjectfromVCS.png

                new AnAction("Attach", "Attach to existing Heroku Application", icon("/general/vcsSmallTab.png")) {
                    {
                        update(null);
                    }

                    @Override
                    public void update(AnActionEvent e) {
                        setEnabled(!herokuProjectService.isHerokuProject());
                    }

                    public void actionPerformed(AnActionEvent anActionEvent) {
                        final App app = tableModel.getApplication(selectedRow.get());
                        if (app == null) return;
                        final GitRemoteInfo attachedRemote = GitHelper.attachRemote(herokuProjectService.getProject(), app);
                        if (attachedRemote != null) {
                            logger.info("Attached remote "+attachedRemote.getName()+":"+attachedRemote.getUrl()+" to project "+herokuProjectService.getProject().getName());
                            herokuProjectService.update(app);
                            updatePanels();
                            HerokuSetupWindow.this.update();
                        } else {
                            logger.warn("No attached remote attached to project "+herokuProjectService.getProject().getName());
                        }

                    }
                },
                new AnAction("New App", "Create new Heroku Application", icon("/general/add.png")) {
                    {
                        update(null);
                    }

                    @Override
                    public void update(AnActionEvent e) {
                        setEnabled(!herokuProjectService.isHerokuProject());
                    }

                    public void actionPerformed(AnActionEvent anActionEvent) {
                        try {
                            Pair<String,Heroku.Stack> newApplicationInfo = Notifications.showCreateNewAppDialog();
                            if (newApplicationInfo==null) return;
                            App newApp = herokuProjectService.getApplicationService().createApplication(newApplicationInfo.first, newApplicationInfo.second);
                            herokuProjectService.update(newApp);
                            GitHelper.attachRemote(herokuProjectService.getProject(), newApp);
                            updatePanels();
                        } catch (Exception e) {
                            Messages.showErrorDialog("Error creating application: " + e.getMessage(), "Error Creating Heroku Application");
                        }
                    }
                },
                new AnAction("Update", "", icon("/actions/sync.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        HerokuSetupWindow.this.update();
                    }
                }
        );
        for (AnAction action : actions) {
            action.update(null);
        }
        return actions;
    }

    private void updatePanels() {
        panels.update();
    }

    public HerokuSetupWindow(HerokuProjectService herokuProjectService, Updateable updateable) {
        super(herokuProjectService);
        this.panels=updateable;
    }
}
