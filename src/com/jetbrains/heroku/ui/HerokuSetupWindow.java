package com.jetbrains.heroku.ui;

import com.heroku.api.App;
import com.heroku.api.Heroku;
import com.intellij.ide.actions.StartUseVcsAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.jetbrains.heroku.git.GitHelper;
import com.jetbrains.heroku.notification.Notifications;
import com.jetbrains.heroku.service.HerokuProjectService;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mh
 * @since 26.12.11
 */
public class HerokuSetupWindow extends HerokuToolWindow {

    private AtomicInteger selectedRow;
    private AppsTableModel tableModel;
    private Updateable panels;

    @Override
    protected void setWindowInfo(ContentInfo contentInfo) {
        contentInfo.describe("Setup", "/vcs/customizeView.png", "Application Setup");
    }

    @Override
    protected JComponent createContentPane() {
        if (herokuProjectService.isHerokuProject()) return null;
        tableModel = new AppsTableModel();
        update();
        selectedRow = new AtomicInteger(-1);
        return GuiUtil.table(tableModel, selectedRow);
    }

    private List<App> load() {
        return herokuProjectService.getApplicationService().listApps();
    }

    public void update() {
        setEnabled(!herokuProjectService.isHerokuProject());
        tableModel.update(load());
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
                        if (GitHelper.attachRemote(herokuProjectService.getProject(), app) != null) {
                            herokuProjectService.update(app);
                            updatePanels();
                            HerokuSetupWindow.this.update();
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
