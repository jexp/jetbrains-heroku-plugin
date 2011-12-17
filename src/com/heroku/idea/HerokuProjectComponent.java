package com.heroku.idea;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author mh
 * @since 16.12.11
 */
public class HerokuProjectComponent implements ProjectComponent, PersistentStateComponent {
    Account account = new Account(null,null); // todo interface
    // todo credentials, heroku endpoint etc.
    private final Project project;

    public HerokuProjectComponent(Project project) {
        this.project = project;
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "Heroku.ProjectComponent";
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    public Object getState() {
        return account;
    }

    public void loadState(Object state) {
        this.account= (Account) state;
    }
}
