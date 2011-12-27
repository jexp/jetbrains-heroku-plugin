package com.jetbrains.heroku.ui;

import com.heroku.api.App;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.jetbrains.heroku.git.GitHelper;
import com.jetbrains.heroku.git.GitRemoteInfo;
import com.jetbrains.heroku.service.HerokuProjectService;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mh
 * @since 17.12.11
 */
public class OldHerokuToolWindowFactory implements ToolWindowFactory {

    private final JTabbedPane tabs = new JTabbedPane(1);

    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        final HerokuProjectService herokuProjectService = ServiceManager.getService(project,HerokuProjectService.class);
        Content content = contentFactory.createContent(createPanel(herokuProjectService), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    // todo check for non existent project on heroku and op
    // if there is no app-name configured: provide one for an existing heroku project or create a new one
    // todo async load operations
    private JComponent createPanel(HerokuProjectService herokuProjectService) {
        updatePanels(herokuProjectService);
        return tabs;
    }

    private void updatePanels(HerokuProjectService herokuProjectService) {
        tabs.removeAll();
        if (herokuProjectService.isHerokuProject()) {
        } else {
            tabs.addTab("Setup", createSetupPanel(herokuProjectService));
        }
    }

    private JButton updateButton(final Runnable runnable) {
        return new JButton(new AbstractAction("Update") {
            public void actionPerformed(ActionEvent actionEvent) {
                runnable.run();
            }
        });
    }

    private JPanel createSetupPanel(final HerokuProjectService herokuProjectService) {
        final Project project = herokuProjectService.getProject();
        final DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "pref, 10dlu, pref, 10dlu, pref, 10dlu, pref, pref:grow", // columns
                "default, pref:g(1)"));// rows
        if (!GitHelper.isGitEnabled(project)) {
            builder.append(new JButton(new AbstractAction("Enable Git Integration") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updatePanels(herokuProjectService);
                }
            }));
            return builder.getPanel();
        }

        final AppsTableModel applicationsTableModel = new AppsTableModel();
        final Runnable updater = new Runnable() {
            public void run() {
                final List<App> allApps = herokuProjectService.getApplicationService().allApps();
                applicationsTableModel.update(allApps);
            }
        };
        SwingUtilities.invokeLater(updater);
        final AtomicInteger selectedApplication = new AtomicInteger();
        builder.append(table(applicationsTableModel, selectedApplication), 8);
        builder.append(updateButton(updater));

        builder.append(new JButton(new AbstractAction("Attach") {
            public void actionPerformed(ActionEvent actionEvent) {
                final App app = applicationsTableModel.getSelectedApplication(selectedApplication);
                if (app != null) {
                    herokuProjectService.update(app);
                    attachRemote(project, app);
                    updatePanels(herokuProjectService);
                }
            }
        }));
        builder.append(new JButton(new AbstractAction("Clone") {
            {
                setEnabled(false);
            }

            public void actionPerformed(ActionEvent actionEvent) {
                final App app = applicationsTableModel.getSelectedApplication(selectedApplication);
                if (app != null) {
                    // todo git clone
                }
            }
        }));
        builder.append(new JButton(new AbstractAction("Create New") {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    String newApplicationName = Messages.showInputDialog(project, "Please enter the new Heroku Application Name or leave blank for default:", "New Heroku Application Name", Messages.getQuestionIcon());
                    App newApp = herokuProjectService.getApplicationService().createApplication(newApplicationName);
                    herokuProjectService.update(newApp);
                    attachRemote(project, newApp);
                    updatePanels(herokuProjectService);
                } catch (Exception e) {
                    Messages.showMessageDialog(project, "Error creating application: " + e.getMessage(), "Error Creating Heroku Application", Messages.getErrorIcon());
                }
            }
        }));
        return builder.getPanel();
    }

    private GitRemoteInfo attachRemote(Project project, App app) {
        final String gitUrl = app.getGitUrl();
        final GitRemoteInfo remote = GitHelper.findRemote(gitUrl, project);
        if (remote == null) {
            GitHelper.addHerokuRemote(project, gitUrl);
            return GitHelper.findRemote(gitUrl, project);
        }
        return null;
    }

    private JBScrollPane table(TableModel model, final AtomicInteger selectedRow) {
        return new JBScrollPane(withSelectionCallback(new JTable(model), selectedRow));
    }

    private JTable withSelectionCallback(final JTable table, final AtomicInteger selectedRow) {
        if (selectedRow == null) return table;

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                selectedRow.set(table.convertRowIndexToModel(table.getSelectedRow()));
            }
        });
        return table;
    }
}
