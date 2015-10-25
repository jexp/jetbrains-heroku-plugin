package com.jetbrains.heroku;

import com.heroku.api.App;
import com.heroku.api.Heroku;
import com.heroku.api.Key;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.jetbrains.heroku.herokuapi.Credentials;
import com.jetbrains.heroku.notification.Notifications;
import com.jetbrains.heroku.service.HerokuApplicationService;
import com.jetbrains.heroku.ui.BackgroundAction;
import com.jetbrains.heroku.ui.SimpleAppsTableModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.jetbrains.heroku.ui.GuiUtil.table;

/**
 * @author mh
 * @since 17.12.11
 */
public class HerokuSettings implements Configurable {

    private JTextField tokenField = new JTextField();
    private final SimpleAppsTableModel appsModel= new SimpleAppsTableModel();
    private final HerokuApplicationService herokuApplicationService;
    private KeysTableModel keyModel= new KeysTableModel();
    private AtomicInteger selectedKey;
    private AtomicInteger selectedApp;

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
        tokenField.setColumns(50);
        reset();
        final DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "right:pref, 6dlu, pref, 10dlu, right:pref:g(0.2), 6dlu, pref, 10dlu, pref, 10dlu:g(0.1)", // columns
                "pref, pref, min(pref;100dlu), pref, min(pref;50dlu)"));// rows
        builder.appendSeparator("Heroku Credentials");
        builder.append("API-Key");
        builder.append(tokenField, 3);
        builder.append(new JButton(new BackgroundAction("Check API-Key") {
            public void runActionPerformed(ActionEvent e) {
                String token = getToken();
                if (token.isEmpty()) {
                    Notifications.notifyModalInfo("<html>Please retrieve your API-Key at your <a href=\"https://api.heroku.com/account\">Heroku Account</a></html>","Need API-Key");
                } else {
                    if (testLogin()) {
                        Notifications.notifyModalInfo("Heroku Login", "Heroku Login successful! Authorized: " + token);
                    }
                }
            }
        }),1);
        builder.nextLine();
        appendAppsTable(builder);
        appendKeysTable(builder);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                testLogin();
            }
        });
        return builder.getPanel();
    }

    private void appendAppsTable(DefaultFormBuilder builder) {
        selectedApp = new AtomicInteger(-1);
        builder.append(table(appsModel,selectedApp), 10);
        builder.append(new JButton(new BackgroundAction("Add Application") {
            public void runActionPerformed(ActionEvent e) {
                Pair<String,Heroku.Stack> newApplicationInfo = Notifications.showCreateNewAppDialog();
                if (newApplicationInfo==null) return;
                App newApp = herokuApplicationService.createApplication(newApplicationInfo.first, newApplicationInfo.second);
                Notifications.notifyModalInfo("Created App", "Sucessfully created App " + newApp.getName() + " on " + newApp.getStack());
                appsModel.update(herokuApplicationService.listApps());
            }
        }), 1);
        builder.append(new JButton(new BackgroundAction("Destroy App") {
            public void runActionPerformed(ActionEvent e) {
                App app = appsModel.getApplication(selectedApp.get());
                if (app==null) return;
                if (Messages.showYesNoDialog("Really destroy app "+app.getName()+" this is irrecoverable!","Destroy App",Messages.getWarningIcon())!=Messages.YES) return;
                herokuApplicationService.destroyApp(app);
                Notifications.notifyModalInfo("Destroyed App", "Sucessfully Destroyed App " + app.getName());
                appsModel.update(herokuApplicationService.listApps());
            }
        }), 1);
        builder.nextLine();
    }

    private void appendKeysTable(DefaultFormBuilder builder) {
        selectedKey = new AtomicInteger(-1);
        builder.append(table(keyModel, selectedKey), 10);
        builder.append(new JButton(new BackgroundAction("Add Key") {
            public void runActionPerformed(ActionEvent e) {
                final String key = Messages.showMultilineInputDialog(null, "Input public ssh key", "Add SSH-KEy", null, Messages.getQuestionIcon(), null);
                herokuApplicationService.addKey(key);
                keyModel.update(herokuApplicationService.listKeys());
            }
        }), 1);
        builder.append(new JButton(new BackgroundAction("Remove Key") {
            public void runActionPerformed(ActionEvent e) {
                Key key = keyModel.getKey(selectedKey.get());
                if (key == null) return;
                herokuApplicationService.removeKey(key);
                keyModel.update(herokuApplicationService.listKeys());
            }
        }), 1);
    }

    public boolean isModified() {
        return true;
    }

    private String getToken() {
        return tokenField.getText();
    }

    public void apply() throws ConfigurationException {
        if (testLogin()) {
            herokuApplicationService.update(getToken());
        }
    }

    public void reset() {
        final Credentials credentials = herokuApplicationService.getCredentials();
        this.tokenField.setText(credentials != null ? credentials.getToken() : null);
    }

    public void disposeUIResources() {
        tokenField = null;
    }

    private boolean testLogin() {
        this.appsModel.update(Collections.<App>emptyList());
        this.keyModel.update(Collections.<Key>emptyList());

        final String token = getToken();
        if (token ==null || token.isEmpty()) {
            Notifications.notifyModalError("Missing API-Key", "Please retrieve the Heroku-API Key from your Heroku Account page.");
            return false;
        }

        if (herokuApplicationService.validateToken(token)) {
            herokuApplicationService.update(token);
            this.appsModel.update(herokuApplicationService.listApps());
            this.keyModel.update(herokuApplicationService.listKeys());
            return true;
        } else {
            Notifications.notifyModalError("Heroku Login", "Heroku Login not successful! Could not authorize: " + token);
            return false;
        }
    }



    private static class KeysTableModel extends AbstractTableModel {
        private final List<Key> keys=new ArrayList<Key>();

        public void update(List<Key> keys) {
            this.keys.clear();
            this.keys.addAll(keys);
            fireTableDataChanged();
        }
        @Override
        public int getRowCount() {
            return keys.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            final Key key = getKey(rowIndex);
            final String[] parts = key.getContents().split(" +");
            switch (columnIndex) {
                case 0 : return parts[2];
                case 1 : return parts[0];
                case 2 : return parts[1];
            }
            return null;
        }


        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0 : return "Comment";
                case 1 : return "Type";
                case 2 : return "Key";
            }
            return null;
        }

        public Key getKey(int row) {
            if (row==-1) return null;
            return keys.get(row);
        }
    }
}
