package com.heroku.idea;

import com.heroku.idea.component.HerokuApplicationComponent;
import com.heroku.idea.herokuapi.Credentials;
import com.heroku.idea.ui.ApplicationsTableModel;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
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

    private JTextField nameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private final ApplicationsTableModel appsModel= new ApplicationsTableModel();
    private final HerokuApplicationComponent herokuApplicationComponent;

    public HerokuSettings(HerokuApplicationComponent herokuApplicationComponent) {
        this.herokuApplicationComponent = herokuApplicationComponent;
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
        nameField.setColumns(20);
        passwordField.setColumns(10);
        final DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "right:pref, 6dlu, pref, 10dlu, right:pref, 6dlu, pref, 10dlu, pref, 10dlu:grow(0.1)", // columns
                "pref"));// rows
        builder.appendSeparator("Heroku Credentials");
        builder.append("&Login", nameField);
        builder.append("&Password", passwordField);
        builder.append(new JButton("Test") {{
            addActionListener(HerokuSettings.this);
        }},1);
        builder.nextLine();
        builder.append(new JScrollPane(new JTable(appsModel)),10);
        return builder.getPanel();
    }

    public boolean isModified() {
        return true;
    }

    private String getPassword() {
        return String.valueOf(passwordField.getPassword());
    }

    private String getName() {
        return nameField.getText();
    }

    public void apply() throws ConfigurationException {
        if (testLogin()) {
            herokuApplicationComponent.update(getName(), getPassword());
        }
    }

    public void reset() {
        final Credentials credentials = herokuApplicationComponent.getState();
        this.nameField.setText(credentials!=null ? credentials.user() : null);
        this.passwordField.setText(null);

    }

    public void disposeUIResources() {
        nameField = null;
        passwordField = null;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (testLogin()) {
            Messages.showMessageDialog("Heroku Login successful! Authorized: "+getName(), "Heroku Login", Messages.getInformationIcon());
        }
    }

    private boolean testLogin() {
        final Credentials credentials = herokuApplicationComponent.login(getName(), getPassword());
        final boolean validCredentials = credentials != null && credentials.valid();

        if (validCredentials) {
            this.appsModel.update(herokuApplicationComponent.allApps(credentials));
        }
        return validCredentials;
    }

}
