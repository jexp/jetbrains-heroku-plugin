package com.jetbrains.heroku.ui;

import com.heroku.api.Addon;
import com.heroku.api.App;
import com.heroku.api.Collaborator;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.jetbrains.heroku.service.HerokuProjectService;
import com.jetbrains.heroku.git.GitHelper;
import com.jetbrains.heroku.git.GitRemoteInfo;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mh
 * @since 17.12.11
 */
public class HerokuToolWindowFactory implements ToolWindowFactory {

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
            tabs.addTab("Application", createActivePanel(herokuProjectService));
            tabs.addTab("Information", createInfoTable(herokuProjectService));
            tabs.addTab("Config", createConfigTable(herokuProjectService));
            tabs.addTab("Add-Ons", createAddOnTable(herokuProjectService));
            tabs.addTab("Collaborators", createCollaboratorTable(herokuProjectService));
            tabs.addTab("Logs", createLogViewer(herokuProjectService));
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

    private JPanel createActivePanel(final HerokuProjectService herokuProjectService) {
        if (!herokuProjectService.isHerokuProject()) return null;
        final Project project = herokuProjectService.getProject();

        final App app = herokuProjectService.getApp();

        final String gitUrl = app.getGitUrl();
        GitRemoteInfo herokuRemote = GitHelper.findRemote(gitUrl, project);

        final DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "right:pref, 6dlu, right:pref, 10dlu, right:pref, 6dlu, right:pref, pref:grow", // columns
                "pref"));// rows
        builder.appendSeparator("Heroku Application: " + herokuProjectService.getHerokuAppName());
        builder.append("URL", link(app.getWebUrl()));
        builder.append("Domain", link(app.getDomainName()));
        builder.append("Owner", new JLabel(app.getOwnerEmail()));
        builder.append("Stack", new JLabel(app.getStack()));
        builder.append("Dynos", new JLabel("" + app.getDynos()));
        builder.append("Workers", new JLabel("" + app.getWorkers()));
        if (herokuRemote != null)
            builder.append("Remote", new JLabel(herokuRemote.getName() + " : " + herokuRemote.getUrl()));
        else
            builder.append("Remote", new JButton(new AbstractAction("Add: " + gitUrl) {
                public void actionPerformed(ActionEvent actionEvent) {
                    final GitRemoteInfo herokuRemote = attachRemote(project, app);
                    if (herokuRemote != null) {
                        setEnabled(false);
                        this.putValue(NAME, herokuRemote.getName() + " : " + herokuRemote.getUrl());
                        this.putValue(SHORT_DESCRIPTION, herokuRemote.getName() + " : " + herokuRemote.getUrl());
                    }
                }
            }));

        //builder.append(table(new GitRemotesTableModel(remotes)), 8);

        builder.append(new JButton(new AbstractAction("Restart") {
            public void actionPerformed(ActionEvent actionEvent) {
                herokuProjectService.restartApplication();
            }
        }));
        builder.append(new JButton(new AbstractAction("Deploy") {
            public void actionPerformed(ActionEvent actionEvent) {
                GitHelper.pushToHeroku(project);
            }
        }));

        return builder.getPanel();
    }

    private HyperlinkLabel link(final String url) {
        if (url == null) {
            return new HyperlinkLabel();
        }
        final HyperlinkLabel label = new HyperlinkLabel(url);
        label.setHyperlinkTarget(url);
        return label;
    }

    private JTextArea createLogViewer(HerokuProjectService herokuProjectService) {
        if (!herokuProjectService.isHerokuProject()) return null;
        String logs = herokuProjectService.getApplicationLogs();
        return new JTextArea(logs);
    }

    private Component createCollaboratorTable(HerokuProjectService herokuProjectService) {
        if (!herokuProjectService.isHerokuProject()) return null;
        final List<Collaborator> collaborators = herokuProjectService.getApplicationCollaborators();
        return table(new CollaboratorTableModel(collaborators));
    }

    private JBScrollPane table(TableModel model) {
        return table(model, null);
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

    private Component createAddOnTable(HerokuProjectService herokuProjectService) {
        if (!herokuProjectService.isHerokuProject()) return null;
        final List<Addon> addons = herokuProjectService.getApplicationAddOns();
        return table(new AddonTableModel(addons));
    }

    @SuppressWarnings("unchecked")
    private Component createConfigTable(HerokuProjectService herokuProjectService) {
        if (!herokuProjectService.isHerokuProject()) return null;
        final Map applicationInfo = herokuProjectService.getApplicationConfig();
        return table(new MapTableModel(applicationInfo, "Parameter", "Value"));
    }

    private Component createInfoTable(HerokuProjectService herokuProjectService) {
        if (!herokuProjectService.isHerokuProject()) return null;
        final App app = herokuProjectService.getApplicationInfo();
        return table(new AppTableModel(app));
    }

    private static class MapTableModel extends AbstractTableModel {
        private final Map<String, Object> data;
        private final String keyTitle;
        private final String valueTitle;

        public MapTableModel(Map<String, Object> data, String keyTitle, String valueTitle) {
            this.data = data;
            this.keyTitle = keyTitle;
            this.valueTitle = valueTitle;
        }

        @Override
        public String getColumnName(int i) {
            return i == 0 ? keyTitle : valueTitle;
        }

        public int getRowCount() {
            return data.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public Object getValueAt(int row, int column) {
            if (column == 0) return getKey(row);
            if (column == 1) return data.get(getKey(row));
            return null;
        }

        private String getKey(int row) {
            Iterator<String> it = data.keySet().iterator();
            for (int i = 0; i < row && it.hasNext(); i++) {
                it.next();
            }
            return it.next();
        }
    }

    private static class GitRemotesTableModel extends AbstractTableModel {
        private final List<GitRemoteInfo> remotes;

        public GitRemotesTableModel(List<GitRemoteInfo> remotes) {
            this.remotes = remotes;
        }

        public int getRowCount() {
            return remotes.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public Object getValueAt(int row, int col) {
            final GitRemoteInfo remote = remotes.get(row);
            switch (col) {
                case 0:
                    return remote.getName();
                case 1:
                    return remote.getUrl();
            }
            return null;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return "Name";
                case 1:
                    return "URL";
            }
            return null;
        }
    }

    private static class CollaboratorTableModel extends AbstractTableModel {
        private final List<Collaborator> collaborators;

        public CollaboratorTableModel(List<Collaborator> collaborators) {
            this.collaborators = collaborators;
        }

        @Override
        public int getRowCount() {
            return collaborators.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int row, int column) {
            final Collaborator collaborator = collaborators.get(row);
            return column == 0 ? collaborator.getEmail() : collaborator.getAccess();
        }

        @Override
        public String getColumnName(int column) {
            return column == 0 ? "Email" : "Access";
        }
    }

    private static class AddonTableModel extends AbstractTableModel {
        private final List<Addon> addons;

        public AddonTableModel(List<Addon> addons) {
            this.addons = addons;
        }

        enum Column {name, description, configured, state, beta, url}

        @Override
        public int getRowCount() {
            return addons.size();
        }

        @Override
        public int getColumnCount() {
            return Column.values().length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            final Addon addon = addons.get(row);
            switch (columnFor(col)) {
                case name:
                    return addon.getName();
                case description:
                    return addon.getDescription();
                case configured:
                    return addon.getConfigured();
                case state:
                    return addon.getState();
                case beta:
                    return addon.getBeta();
                case url:
                    return addon.getUrl();
            }
            return null;
        }

        private Column columnFor(int column) {
            return Column.values()[column];
        }

        @Override
        public String getColumnName(int column) {
            return columnFor(column).name();
        }
    }

    private class ListMapTableModel extends AbstractTableModel {
        private final List<Map<String, Object>> info;
        private final String[] columns;

        public ListMapTableModel(List<Map<String, Object>> info, String... columns) {
            this.info = info;
            this.columns = columns;
        }

        public int getRowCount() {
            return info.size();
        }

        public int getColumnCount() {
            return columns.length;
        }

        public Object getValueAt(int row, int column) {
            return info.get(row).get(columns[column]);
        }

        @Override
        public String getColumnName(int i) {
            return columns[i];
        }
    }
}
