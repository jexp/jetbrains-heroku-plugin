package com.jetbrains.heroku.ui;

import com.heroku.api.request.log.LogStreamResponse;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.RegexpFilter;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author mh
 * @since 26.12.11
 * todo integrate in existing tool windows like messages-tool-window (see MavenConsoleImpl)
 */
public class LogView {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final String CONSOLE_FILTER_REGEXP =
            "(?:^|(?:\\[\\w+\\]\\s*))" + RegexpFilter.FILE_PATH_MACROS + ":\\[" + RegexpFilter.LINE_MACROS + "," + RegexpFilter.COLUMN_MACROS + "]";

    private final Project project;
    private final ConsoleView consoleView;
    private final AtomicBoolean isOpen = new AtomicBoolean(false);

    public LogView(Project project) {
        this.project = project;
        consoleView = createConsoleView();
    }

    public ConsoleView getConsoleView() {
        return consoleView;
    }

    private ConsoleView createConsoleView() {
        return createConsoleBuilder(project).getConsole();
    }

    public static TextConsoleBuilder createConsoleBuilder(Project project) {
        TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(project);

        //final ExceptionFilter exceptionFilter = new ExceptionFilter(new ProjectScopeImpl(project, new ProjectFileIndexFacade(project, ge)));
        Filter[] filters = {new RegexpFilter(project, CONSOLE_FILTER_REGEXP)};
        for (Filter filter : filters) {
            builder.addFilter(filter);
        }
        return builder;
    }

    public void update(LogStreamResponse lsr) {
        if (lsr==null) return;
        consoleView.clear();
        printLog(lsr.openStream(), consoleView);
        // consoleView.print();
        // consoleView.printHyperlink();
    }

    private void printLog(InputStream inputStream, final ConsoleView view) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                view.print(line+LINE_SEPARATOR, ConsoleViewContentType.NORMAL_OUTPUT); // todo filter prefixes
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            view.print("Error reading log-stream: " + ioe.getMessage(), ConsoleViewContentType.ERROR_OUTPUT);
        } finally {
            closeReader(reader);
        }
    }

    private void closeReader(BufferedReader reader) {
        if (reader == null) return;
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
