package com.jetbrains.heroku.ui;

import com.heroku.api.Collaborator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.jetbrains.heroku.service.HerokuProjectService;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
        if (!herokuProjectService.isHerokuProject()) return null;
        tableModel = new CollaboratorTableModel(load());
        selectedRow = new AtomicInteger(-1);
        return table(tableModel, selectedRow);
    }

    private List<Collaborator> load() {
        return herokuProjectService.getApplicationCollaborators();
    }

    private void update() {
        tableModel.update(load());
    }

    @Override
    protected List<AnAction> createActions() {
        return Arrays.<AnAction>asList(
                new AnAction("Add Collaborator Variable", "", icon("/general/add.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        String email = Messages.showInputDialog(herokuProjectService.getProject(), "Collaborator Heroku email:", "Add Collaborator", Messages.getQuestionIcon());
                        if (email!=null && !email.isEmpty()) return;
                        herokuProjectService.addCollaborator(email);
                        HerokuCollaboratorsWindow.this.update();
                    }
                },
                new AnAction("Remove Config Variable", "", icon("/general/remove.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        final Collaborator collaborator = tableModel.getCollaborator(selectedRow.get());
                        if (collaborator==null) return;
                        if (Messages.showYesNoDialog("Remove the Collaborator: "+collaborator.getEmail(),"Remove Collaborator",Messages.getQuestionIcon())!=Messages.YES) return;
                        herokuProjectService.removeCollaborator(collaborator.getEmail());
                        HerokuCollaboratorsWindow.this.update();
                    }
                },
                new AnAction("Update", "", icon("/actions/sync.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        HerokuCollaboratorsWindow.this.update();
                    }
                }
        );
    }

    public HerokuCollaboratorsWindow(HerokuProjectService herokuProjectService) {
        super(herokuProjectService);
    }
}
