package com.jetbrains.heroku.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * @author mh
 * @since 17.12.11
 */
public class HerokuSettingsAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {
    }

    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        String txt = Messages.showInputDialog(project, "What is your name?", "Input your name", Messages.getQuestionIcon());
        Messages.showMessageDialog(project, "Hello, " + txt + "!\n I am glad to see you.", "Information", Messages.getInformationIcon());
    }
}
