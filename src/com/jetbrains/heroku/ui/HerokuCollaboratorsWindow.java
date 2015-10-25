package com.jetbrains.heroku.ui;

import com.heroku.api.Collaborator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.jetbrains.heroku.service.HerokuProjectService;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mh
 * @since 26.12.11
 */
public class HerokuCollaboratorsWindow extends HerokuToolWindow {

    private AtomicInteger selectedRow;
    private CollaboratorTableModel tableModel;

    @Override
    protected void setWindowInfo(ContentInfo contentInfo) {
        contentInfo.describe("Collaborators","/javaee/WebServiceClient.png","Collaborators working on this Application");
    }

    @Override
    protected JComponent createContentPane() {
        tableModel = new CollaboratorTableModel(load());
        selectedRow = new AtomicInteger(-1);
        return GuiUtil.table(tableModel, selectedRow);
    }

    private List<Collaborator> load() {
        if (!herokuProjectService.isHerokuProject()) return Collections.emptyList();
        return herokuProjectService.getApplicationCollaborators();
    }

    public void doUpdate() {
        setEnabled(herokuProjectService.isHerokuProject());
        tableModel.update(load());
    }

    @Override
    protected List<AnAction> createActions() {
        return Arrays.<AnAction>asList(
                new AnAction("Add Collaborator Variable", "", icon("/general/add.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        String email = Messages.showInputDialog(getProject(), "Collaborator Heroku email:", "Add Collaborator", Messages.getQuestionIcon());
                        if (email!=null && !email.isEmpty()) return;
                        herokuProjectService.addCollaborator(email);
                        HerokuCollaboratorsWindow.this.doUpdate();
                    }
                },
                new AnAction("Remove Config Variable", "", icon("/general/remove.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        final Collaborator collaborator = tableModel.getCollaborator(selectedRow.get());
                        if (collaborator==null) return;
                        if (Messages.showYesNoDialog("Remove the Collaborator: "+collaborator.getEmail(),"Remove Collaborator",Messages.getQuestionIcon())!=Messages.YES) return;
                        herokuProjectService.removeCollaborator(collaborator.getEmail());
                        HerokuCollaboratorsWindow.this.doUpdate();
                    }
                },
                new AnAction("Update", "", icon("/actions/sync.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        HerokuCollaboratorsWindow.this.doUpdate();
                    }
                }
        );
    }

    public HerokuCollaboratorsWindow(HerokuProjectService herokuProjectService) {
        super(herokuProjectService);
    }
}
