package com.jetbrains.heroku.ui;

import com.heroku.api.App;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.table.JBTable;
import com.jetbrains.heroku.git.GitHelper;
import com.jetbrains.heroku.git.GitRemoteInfo;
import com.jetbrains.heroku.service.HerokuProjectService;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mh
 * @since 26.12.11
 */
public abstract class HerokuToolWindow extends SimpleToolWindowPanel implements Disposable, DataProvider {
    protected HerokuProjectService herokuProjectService;

    public HerokuToolWindow(HerokuProjectService herokuProjectService) {
        super(false, false);
        this.herokuProjectService = herokuProjectService;
        setContent(createContentPane());
        setToolbar(createToolbarPanel(createActions()));
    }

    protected HyperlinkLabel link(final String url) {
        if (url == null) {
            return new HyperlinkLabel();
        }
        final HyperlinkLabel label = new HyperlinkLabel(url);
        label.setHyperlinkTarget(url);
        label.addHyperlinkListener(new HyperlinkAdapter() {
              @Override
              protected void hyperlinkActivated(final HyperlinkEvent e) {
                BrowserUtil.launchBrowser(e.getURL().toExternalForm());
              }
            });
        return label;
    }

    GitRemoteInfo attachRemote(Project project, App app) {
        final String gitUrl = app.getGitUrl();
        final GitRemoteInfo remote = GitHelper.findRemote(gitUrl, project);
        if (remote == null) {
            GitHelper.addHerokuRemote(project, gitUrl);
            return GitHelper.findRemote(gitUrl, project);
        }
        return null;
    }

    protected JScrollPane table(TableModel model) {
        return table(model, null);
    }

    protected JScrollPane table(TableModel model, final AtomicInteger selectedRow) {
        final JBTable table = new JBTable(model);
        table.setAutoCreateRowSorter(true);
        return ScrollPaneFactory.createScrollPane(withSelectionCallback(table, selectedRow));
    }

    protected JTable withSelectionCallback(final JTable table, final AtomicInteger selectedRow) {
        if (selectedRow == null) return table;

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                final int viewRow = table.getSelectedRow();
                final int modelRow = viewRow == -1 ? -1 : table.convertRowIndexToModel(viewRow);
                selectedRow.set(modelRow);
            }
        });
        return table;
    }

    interface ContentInfo {
        void describe(String title, String icon, String description);
    }

    public HerokuToolWindow addAsContent(ContentManager contentManager) {
        final Content content = contentManager.getFactory().createContent(this, null, false);

        setWindowInfo(new ContentInfo() {
            public void describe(String title, String icon, String description) {
                content.setIcon(icon(icon));
                content.setDescription(description);
                content.setDisplayName(title);
                content.setTabName(title);
                content.setToolwindowTitle(title);
            }
        });
        content.setCloseable(false);
        contentManager.addContent(content);
        return this;
    }

    protected Icon icon(String iconPath) {
        return IconLoader.getIcon(iconPath);
    }

    protected abstract void setWindowInfo(ContentInfo contentInfo);

    protected abstract JComponent createContentPane();

    protected List<AnAction> createActions() {
        return Collections.emptyList();
    }

    private JPanel createToolbarPanel(Collection<AnAction> actions) {
        final DefaultActionGroup group = new DefaultActionGroup();
        for (AnAction action : actions) {
            group.add(action);
        }
        final ActionToolbar actionToolBar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, false);
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(actionToolBar.getComponent(), BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void dispose() {
        this.herokuProjectService = null;
    }
}
