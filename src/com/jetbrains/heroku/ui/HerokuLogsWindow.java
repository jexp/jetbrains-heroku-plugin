package com.jetbrains.heroku.ui;

import com.heroku.api.request.log.LogStreamResponse;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.heroku.service.HerokuProjectService;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author mh
 * @since 26.12.11
 */
public class HerokuLogsWindow extends HerokuToolWindow {

    private LogView logView;

    @Override
    protected void setWindowInfo(ContentInfo contentInfo) {
        contentInfo.describe("Logs", "/debugger/console.png", "Application Logs");
    }

    @Override
    protected JComponent createContentPane() {
        logView = new LogView(herokuProjectService.getProject());
        doUpdate();
        return logView.getConsoleView().getComponent();
    }

    private LogStreamResponse load() {
        if (!herokuProjectService.isHerokuProject()) return null;

        return herokuProjectService.getApplicationLogStream();
    }

    public void doUpdate() {
        setEnabled(herokuProjectService.isHerokuProject());
        logView.update(load());
    }

    @Override
    protected List<AnAction> createActions() {
        return Arrays.<AnAction>asList(
                new AnAction("Update", "", icon("/actions/sync.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        HerokuLogsWindow.this.doUpdate();
                    }
                }
        );
    }

    public HerokuLogsWindow(HerokuProjectService herokuProjectService) {
        super(herokuProjectService);
    }
}
