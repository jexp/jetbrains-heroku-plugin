package com.heroku.idea;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import org.jetbrains.annotations.NotNull;

/**
 * @author mh
 * @since 17.12.11
 */
public class HerokuApplicationComponent implements ApplicationComponent, PersistentStateComponent<Account> {
    private Account account=new Account(null,null);

    public HerokuApplicationComponent() { // inject dependencies
    }

    public void initComponent() {
        // login?
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "Heroku.ApplicationComponent";
    }

    public Account getState() {
        return account;
    }

    public void loadState(Account account) {
        this.account = account;
    }
}
