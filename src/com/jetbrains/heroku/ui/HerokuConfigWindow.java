package com.jetbrains.heroku.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Pair;
import com.jetbrains.heroku.service.HerokuProjectService;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

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

    public void doUpdate() {
        setEnabled(herokuProjectService.isHerokuProject());
        tableModel.update(load());
    }

    private Map load() {
        if (!herokuProjectService.isHerokuProject()) return Collections.emptyMap();
        return (Map)herokuProjectService.getApplicationConfig();
    }

    
    static class AddConfigVariableDialog extends DialogWrapper {

        private JTextField keyField;
        private JTextField valueField;

        protected AddConfigVariableDialog() {
            super(true);
            setTitle("Add Config Variable");
            init();
        }

        @Override
        protected JComponent createCenterPanel() {
            final DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("right:pref, 6dlu, pref:grow", "pref"));
            builder.append("Key", keyField = new JTextField(30));
            builder.append("Value:", valueField = new JTextField(50));
            return builder.getPanel();
        }

        @Override
        protected ValidationInfo doValidate() {
            if (getKey().isEmpty()) return new ValidationInfo("Key is empty", keyField);
            if (getValue().isEmpty()) return new ValidationInfo("Value is empty",valueField);
            return super.doValidate();
        }

        private String getValue() {
            return valueField.getText();
        }

        public String getKey() {
            return keyField.getText();
        }

    }
    
    @Override
    protected List<AnAction> createActions() {
        return Arrays.asList(
                new AnAction("Add Config Variable", "", icon("/general/add.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        final AddConfigVariableDialog dialog = new AddConfigVariableDialog();
                        dialog.show();
                        if (dialog.getExitCode()==AddConfigVariableDialog.OK_EXIT_CODE) {
                            herokuProjectService.addConfigVar(dialog.getKey(), dialog.getValue());
                            HerokuConfigWindow.this.doUpdate();
                        }
                    }
                },
                new AnAction("Remove Config Variable", "", icon("/general/remove.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        final String varName = tableModel.getKey(selectedRow.get());
                        herokuProjectService.removeConfigVar(varName);
                        HerokuConfigWindow.this.doUpdate();
                    }
                },
                new AnAction("Update", "", icon("/actions/sync.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        HerokuConfigWindow.this.doUpdate();
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
