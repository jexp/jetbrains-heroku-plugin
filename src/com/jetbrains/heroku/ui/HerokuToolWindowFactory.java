package com.jetbrains.heroku.ui;

import com.jetbrains.heroku.component.HerokuProjectComponent;
import com.jetbrains.heroku.git.GitHelper;
import com.jetbrains.heroku.git.GitRemoteInfo;
import com.jetbrains.heroku.herokuapi.Application;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import git4idea.GitRemote;

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

    public static final int MAX_DYNOS = 100;
    private final JTabbedPane tabs = new JTabbedPane(1);

    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        final HerokuProjectComponent herokuProjectComponent = project.getComponent(HerokuProjectComponent.class);
        Content content = contentFactory.createContent(createPanel(herokuProjectComponent), "", false);
        toolWindow.getContentManager().addContent(content);
    }

        // todo check for non existent project on heroku and op
    // if there is no app-name configured: provide one for an existing heroku project or create a new one
    // todo async load operations
    private JComponent createPanel(HerokuProjectComponent herokuProjectComponent) {
        updatePanels(herokuProjectComponent);
        return tabs;
    }

    private void updatePanels(HerokuProjectComponent herokuProjectComponent) {
        tabs.removeAll();
        if (herokuProjectComponent.isHerokuProject()) {
            tabs.addTab("Application", createActivePanel(herokuProjectComponent));
            tabs.addTab("Information", createInfoTable(herokuProjectComponent));
            tabs.addTab("Config", createConfigTable(herokuProjectComponent));
            tabs.addTab("Add-Ons", createAddOnTable(herokuProjectComponent));
            tabs.addTab("Collaborators", createCollaboratorTable(herokuProjectComponent));
            tabs.addTab("Logs", createLogViewer(herokuProjectComponent));
        } else {
            tabs.addTab("Setup", createSetupPanel(herokuProjectComponent));
        }
    }

    private JButton updateButton(final Runnable runnable) {
        return new JButton(new AbstractAction("Update") {
            public void actionPerformed(ActionEvent actionEvent) {
                runnable.run();
            }
        });
    }
    private JPanel createSetupPanel(final HerokuProjectComponent herokuProjectComponent) {
        final Project project = herokuProjectComponent.getProject();
        final DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "pref, 10dlu, pref, 10dlu, pref, 10dlu, pref, pref:grow", // columns
                "default, pref:g(1)"));// rows
        final ApplicationsTableModel applicationsTableModel = new ApplicationsTableModel();
        final Runnable updater = new Runnable() {
            public void run() {
                final List<Application> allApps = herokuProjectComponent.getApplicationComponent().allApps();
                applicationsTableModel.update(allApps);
            }
        };
        SwingUtilities.invokeLater(updater);
        final AtomicInteger selectedApplication = new AtomicInteger();
        builder.append(table(applicationsTableModel, selectedApplication), 8);
        builder.append(updateButton(updater));
        builder.append(new JButton(new AbstractAction("Attach") {
            public void actionPerformed(ActionEvent actionEvent) {
                final Application application = applicationsTableModel.getSelectedApplication(selectedApplication);
                if (application != null) {
                    herokuProjectComponent.update(application.getName());
                    attachRemote(project, application);
                    updatePanels(herokuProjectComponent);
                }
            }
        }));
        builder.append(new JButton(new AbstractAction("Clone") {
            {
                setEnabled(false);
            }

            public void actionPerformed(ActionEvent actionEvent) {
                final Application application = applicationsTableModel.getSelectedApplication(selectedApplication);
                if (application != null) {
                    // todo git clone
                }
            }
        }));
        builder.append(new JButton(new AbstractAction("Create New") {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    String newApplicationName = Messages.showInputDialog(project, "Please enter the new Heroku Application Name or leave blank for default:", "New Heroku Application Name", Messages.getQuestionIcon());
                    Application newApplication = herokuProjectComponent.getApplicationComponent().createApplication(newApplicationName);
                    herokuProjectComponent.update(newApplication.getName());
                    attachRemote(project,newApplication);
                    updatePanels(herokuProjectComponent);
                } catch (Exception e) {
                    Messages.showMessageDialog(project, "Error creating application: " + e.getMessage(), "Error Creating Heroku Application", Messages.getErrorIcon());
                }
            }
        }));
        return builder.getPanel();
    }

    private GitRemoteInfo attachRemote(Project project, Application application) {
        final String gitUrl = application.getString("git_url");
        final GitRemoteInfo remote = GitHelper.findRemote(gitUrl, project);
        if (remote==null) {
            GitHelper.addHerokuRemote(project,gitUrl);
            return GitHelper.findRemote(gitUrl, project);
        }
        return null;
    }

    private JPanel createActivePanel(final HerokuProjectComponent herokuProjectComponent) {
        if (!herokuProjectComponent.isHerokuProject()) return null;
        final Project project = herokuProjectComponent.getProject();

        final Application application = herokuProjectComponent.getApplication();

        final String gitUrl = application.getString("git_url");
        GitRemoteInfo herokuRemote = GitHelper.findRemote(gitUrl, project);

        final DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "right:pref, 6dlu, right:pref, 10dlu, right:pref, 6dlu, right:pref, pref:grow", // columns
                "pref"));// rows
        builder.appendSeparator("Heroku Application: " + herokuProjectComponent.getHerokuApplicationName());
        builder.append("URL", link(application.getString("web_url")));
        builder.append("Domain", link(application.getString("domain")));
        builder.append("Owner", new JLabel(application.getString("owner")));
        builder.append("Stack", new JLabel(application.getString("stack")));
        builder.append("Dynos", new JLabel("" + application.get("dynos")));
        builder.append("Workers", new JLabel("" + application.get("workers")));
        if (herokuRemote != null)
            builder.append("Remote", new JLabel(herokuRemote.getName() + " : " + herokuRemote.getUrl()));
        else
            builder.append("Remote", new JButton(new AbstractAction("Add: " + gitUrl) {
                public void actionPerformed(ActionEvent actionEvent) {
                    final GitRemoteInfo herokuRemote = attachRemote(project, application);
                    if (herokuRemote!=null) {
                            setEnabled(false);
                            this.putValue(NAME, herokuRemote.getName() + " : " + herokuRemote.getUrl());
                            this.putValue(SHORT_DESCRIPTION, herokuRemote.getName() + " : " + herokuRemote.getUrl());
                    }
                }
            }));

        //builder.append(table(new GitRemotesTableModel(remotes)), 8);

        builder.append(new JButton(new AbstractAction("Restart") {
            public void actionPerformed(ActionEvent actionEvent) {
                herokuProjectComponent.restartApplication();
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
        if (url==null) {
            return new HyperlinkLabel();
        }
        final HyperlinkLabel label = new HyperlinkLabel(url);
        label.setHyperlinkTarget(url);
        return label;
    }

    private JTextArea createLogViewer(HerokuProjectComponent herokuProjectComponent) {
        if (!herokuProjectComponent.isHerokuProject()) return null;
        String logs = herokuProjectComponent.getApplicationLogs();
        return new JTextArea(logs);
    }

    private Component createCollaboratorTable(HerokuProjectComponent herokuProjectComponent) {
        if (!herokuProjectComponent.isHerokuProject()) return null;
        final List<Map<String, Object>> applicationInfo = herokuProjectComponent.getApplicationCollaborators();
        return table(new ListMapTableModel(applicationInfo, "email", "access"));
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

    private Component createAddOnTable(HerokuProjectComponent herokuProjectComponent) {
        if (!herokuProjectComponent.isHerokuProject()) return null;
        final List<Map<String, Object>> applicationInfo = herokuProjectComponent.getApplicationAddOns();
        return table(new ListMapTableModel(applicationInfo, "name", "description", "configured", "state", "beta", "url"));
    }

    private Component createConfigTable(HerokuProjectComponent herokuProjectComponent) {
        if (!herokuProjectComponent.isHerokuProject()) return null;
        final Map<String, Object> applicationInfo = herokuProjectComponent.getApplicationConfig();
        return table(new MapTableModel(applicationInfo, "Parameter", "Value"));
    }

    private Component createInfoTable(HerokuProjectComponent herokuProjectComponent) {
        if (!herokuProjectComponent.isHerokuProject()) return null;
        final Map<String, Object> applicationInfo = herokuProjectComponent.getApplicationInfo();
        return table(new MapTableModel(applicationInfo, "Setting", "Value"));
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
