package com.jetbrains.heroku.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.jetbrains.heroku.service.HerokuProjectService;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mh
 * @since 26.12.11
 */
public class HerokuToolWindowFactory implements ToolWindowFactory {
    private final Map<Class,HerokuToolWindow> windows = new LinkedHashMap<Class, HerokuToolWindow>();
    private Project project;
    private ToolWindow toolWindow;

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        final HerokuProjectService herokuProjectService = ServiceManager.getService(project, HerokuProjectService.class);
        addAll(new HerokuApplicationWindow(herokuProjectService),
                new HerokuProcessesWindow(herokuProjectService), new HerokuConfigWindow(herokuProjectService),
                new HerokuAddonsWindow(herokuProjectService), new HerokuCollaboratorsWindow(herokuProjectService),
                new HerokuLogsWindow(herokuProjectService), new HerokuReleasesWindow(herokuProjectService));
        if (herokuProjectService.isHerokuProject())
            getContentManager().setSelectedContent(getContent(HerokuAddonsWindow.class));
        else
            getContentManager().setSelectedContent(getContent(HerokuSetupWindow.class));

    }

    private Content getContent(Class<? extends HerokuToolWindow> type) {
        return getContentManager().getContent(getWindow(type));
    }

    private HerokuToolWindow getWindow(Class<? extends HerokuToolWindow> type) {
        return windows.get(type);
    }

    private ContentManager getContentManager() {
        return toolWindow.getContentManager();
    }

    private void addAll(HerokuToolWindow...toolWindow) {
        for (HerokuToolWindow herokuToolWindow : toolWindow) {
            add(herokuToolWindow);
        }
    }
    private void add(HerokuToolWindow toolWindow) {
        this.windows.put(toolWindow.getClass(),toolWindow);
        toolWindow.addAsContent(getContentManager());
    }

    static class TestToolWindow extends SimpleToolWindowPanel implements DataProvider, Disposable {
        private static final Icon ICON_RUN = IconLoader.getIcon("/actions/execute.png");
        private static final Icon ICON_REMOVE = IconLoader.getIcon("/general/remove.png");
        private static final Icon ICON_ADD = IconLoader.getIcon("/general/add.png");
        private static final Icon ICON_FILTER = IconLoader.getIcon("/ant/filter.png");

        private Project project;

        public TestToolWindow(Project project) {
            super(false, false);
            this.project = project;
            setToolbar(createToolbarPanel());
            setContent(ScrollPaneFactory.createScrollPane(new JTable(10, 10)));
            // TODO setContent(ScrollPaneFactory.createScrollPane(myTree));

        }

        private JPanel createToolbarPanel() {
            final DefaultActionGroup group = new DefaultActionGroup();
            group.add(new RunAction());
            //AnAction action = CommonActionsManager.getInstance().createExpandAllAction(myTreeExpander, this);
            //action.getTemplatePresentation().setDescription(AntBundle.message("ant.explorer.expand.all.nodes.action.description"));
            //group.add(action);
            // group.add(new ContextHelpAction(HelpID.ANT));

            final ActionToolbar actionToolBar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, true);
            final JPanel buttonsPanel = new JPanel(new BorderLayout());
            buttonsPanel.add(actionToolBar.getComponent(), BorderLayout.CENTER);
            return buttonsPanel;
        }

        @Override
        public void dispose() {
            this.project = null;
        }

        private final class RunAction extends AnAction {
            public RunAction() {
                super("RunActionTitle", "RunActionDesc", ICON_RUN);
            }

            public void actionPerformed(AnActionEvent e) {
                // todo
            }

            public void update(AnActionEvent event) {
                final Presentation presentation = event.getPresentation();
                final String place = event.getPlace();
                if (ActionPlaces.UNKNOWN.equals(place)) {
                    presentation.setText("placed in toolbar");
                }
                presentation.setEnabled(true); // todo enable on demand
            }
        }

    }
}
