package com.jetbrains.heroku.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.jetbrains.heroku.service.HerokuProjectService;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mh
 * @since 26.12.11
 */
public class HerokuConfigWindow extends HerokuToolWindow {

    private AtomicInteger selectedRow;
    private MapTableModel tableModel;

    @Override
    protected void setWindowInfo(ContentInfo contentInfo) {
        contentInfo.describe("Environment","/debugger/threadGroup.png","Heroku and Addon environment variables");
    }

    @Override
    protected JComponent createContentPane() {
        tableModel = new MapTableModel(load(), "Parameter", "Value");
        selectedRow = new AtomicInteger(-1);
        return GuiUtil.table(tableModel, selectedRow);
    }

    public void update() {
        setEnabled(herokuProjectService.isHerokuProject());
        tableModel.update(load());
    }

    private Map load() {
        if (!herokuProjectService.isHerokuProject()) return Collections.emptyMap();
        return (Map)herokuProjectService.getApplicationConfig();
    }

    @Override
    protected List<AnAction> createActions() {
        return Arrays.asList(
                new AnAction("Add Config Variable", "", icon("/general/add.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        String text = Messages.showInputDialog(herokuProjectService.getProject(), "Config Variable key:value", "Add Config Variable", Messages.getQuestionIcon());
                        Pair<String, String> variable = parseVariable(text);
                        if (variable == null) return;
                        herokuProjectService.addConfigVar(variable.first, variable.second);
                        HerokuConfigWindow.this.update();
                    }
                },
                new AnAction("Remove Config Variable", "", icon("/general/remove.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        final String varName = tableModel.getKey(selectedRow.get());
                        herokuProjectService.removeConfigVar(varName);
                        HerokuConfigWindow.this.update();
                    }
                },
                new AnAction("Update", "", icon("/actions/sync.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        HerokuConfigWindow.this.update();
                    }
                }
        );
    }

    private Pair<String, String> parseVariable(String text) {
        if (text == null || !text.contains(":")) return null;
        int idx=text.indexOf(":");
        return Pair.create(text.substring(0, idx), text.substring(idx + 1));
    }

    public HerokuConfigWindow(HerokuProjectService herokuProjectService) {
        super(herokuProjectService);
    }
}
