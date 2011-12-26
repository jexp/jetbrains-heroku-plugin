package com.jetbrains.heroku;

import com.jetbrains.heroku.service.HerokuApplicationService;
import com.jetbrains.heroku.herokuapi.Credentials;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import com.jetbrains.heroku.ui.AppsTableModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author mh
 * @since 17.12.11
 */
public class HerokuSettings implements Configurable, ActionListener {

    private JTextField emailField = new JTextField();
    private JTextField tokenField = new JTextField();
    private final AppsTableModel appsModel= new AppsTableModel();
    private final HerokuApplicationService herokuApplicationService;

    public HerokuSettings(HerokuApplicationService herokuApplicationService) {
        this.herokuApplicationService = herokuApplicationService;
    }

    @Nls
    public String getDisplayName() {
        return "Heroku";
    }

    public Icon getIcon() {
        return new ImageIcon("/icons/heroku_icon_16.png");
    }

    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        emailField.setColumns(20);
        tokenField.setColumns(10);
        final DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "right:pref, 6dlu, pref, 10dlu, right:pref, 6dlu, pref, 10dlu, pref, 10dlu:grow(0.1)", // columns
                "pref"));// rows
        builder.appendSeparator("Heroku Credentials");
        builder.append("Heroku-Email", emailField);
        builder.append("API-Token", tokenField);
        builder.append(new JButton("Test Credentials") {{
            addActionListener(HerokuSettings.this);
        }},1);
        builder.nextLine();
        builder.append(new JScrollPane(new JTable(appsModel)),10);
        return builder.getPanel();
    }

    public boolean isModified() {
        return true;
    }

    private String getToken() {
        return String.valueOf(tokenField.getText());
    }

    private String getName() {
        return emailField.getText();
    }

    public void apply() throws ConfigurationException {
        if (testLogin()) {
            herokuApplicationService.update(getName(), getToken());
        }
    }

    public void reset() {
        final Credentials credentials = herokuApplicationService.getCredentials();
        this.emailField.setText(credentials != null ? credentials.getEmail() : null);
        this.tokenField.setText(credentials != null ? credentials.getToken() : null);

    }

    public void disposeUIResources() {
        emailField = null;
        tokenField = null;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (testLogin()) {
            Messages.showMessageDialog("Heroku Login successful! Authorized: "+getName(), "Heroku Login", Messages.getInformationIcon());
        }
    }

    private boolean testLogin() {
        final Credentials credentials = herokuApplicationService.login(getName(), getToken());
        final boolean validCredentials = credentials != null && credentials.valid();

        if (validCredentials) {
            this.appsModel.update(herokuApplicationService.allApps(credentials));
        }
        return validCredentials;
    }

}
