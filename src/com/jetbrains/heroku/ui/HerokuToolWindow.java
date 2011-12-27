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
public abstract class HerokuToolWindow extends SimpleToolWindowPanel implements Disposable, DataProvider, Updateable {
    protected HerokuProjectService herokuProjectService;

    public HerokuToolWindow(HerokuProjectService herokuProjectService) {
        super(false, false);
        this.herokuProjectService = herokuProjectService;
        final JComponent contentPane = createContentPane();
        if (contentPane!=null) setContent(contentPane);
        setToolbar(createToolbarPanel(createActions()));
    }

    public void update() {}

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
