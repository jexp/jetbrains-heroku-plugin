package com.jetbrains.heroku.ui;

import com.heroku.api.Proc;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.jetbrains.heroku.service.HerokuProjectService;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author mh
 * @since 26.12.11
 */
public class HerokuProcessesWindow extends HerokuToolWindow {

    private static final int MAX_DYNOS = 100;
    private ProcessTableModel tableModel;

    @Override
    protected void setWindowInfo(ContentInfo contentInfo) {
        contentInfo.describe("Processes","/debugger/threads.png","Processes running this Application");
    }

    @Override
    protected JComponent createContentPane() {
        tableModel = new ProcessTableModel(load());
        return GuiUtil.table(tableModel);
    }

    private List<Proc> load() {
        if (!herokuProjectService.isHerokuProject()) return Collections.emptyList();

        return herokuProjectService.getProcesses();
    }

    public void update() {
        setEnabled(herokuProjectService.isHerokuProject());
        tableModel.update(load());
    }

    @Override
    protected List<AnAction> createActions() {
        return Arrays.asList(
                new AnAction("Scale Dynos", "", icon("/debugger/threadSuspended.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        final ScaleInputValidator parser = new ScaleInputValidator(1, MAX_DYNOS);
                        String dynoText = Messages.showInputDialog(herokuProjectService.getProject(), "New Number of Dynos:", "Scale Dynos", Messages.getQuestionIcon(), String.valueOf(herokuProjectService.getApp().getDynos()), parser);
                        Integer dynos = parser.parse(dynoText);
                        if (dynos == null) return;
                        herokuProjectService.scaleDynos(dynos);
                        HerokuProcessesWindow.this.update();
                    }
                },
                new AnAction("Scale Workers", "", icon("/debugger/threadRunning.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        final ScaleInputValidator parser = new ScaleInputValidator(0, MAX_DYNOS);
                        String workersText = Messages.showInputDialog(herokuProjectService.getProject(), "New Number of Workers:", "Scale Workers", Messages.getQuestionIcon(), String.valueOf(herokuProjectService.getApp().getWorkers()), parser);
                        Integer workers = parser.parse(workersText);
                        if (workers == null) return;
                        herokuProjectService.scaleWorkers(workers);
                        HerokuProcessesWindow.this.update();
                    }
                },
                new AnAction("Update", "", icon("/actions/sync.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        HerokuProcessesWindow.this.update();
                    }
                }
        );
    }

    public HerokuProcessesWindow(HerokuProjectService herokuProjectService) {
        super(herokuProjectService);
    }

    private static class ScaleInputValidator implements InputValidator {
        int min, max;

        private ScaleInputValidator(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean checkInput(String s) {
            final Integer value = parse(s);
            return value!=null &&  value >= min && value < max;
        }

        @Override
        public boolean canClose(String s) {
            return true; // todo
        }

        public Integer parse(String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }
}
