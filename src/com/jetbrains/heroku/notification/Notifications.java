package com.jetbrains.heroku.notification;

import com.heroku.api.Heroku;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Set;

/**
 * @author mh
 * @since 27.12.11
 */
public class Notifications {

    static Notifier notifier = createNotifier();

    private static Notifier createNotifier() {
        try {
            Class.forName("com.intellij.notification.NotificationGroup");
            return createNotifier("NewNotifier");
        } catch (ClassNotFoundException cnfe) {
            return createNotifier("OldNotifier");
        }
    }

    private static Notifier createNotifier(final String className) {
        try {
            return (Notifier) Class.forName(Notifier.class.getPackage().getName() + "." + className).newInstance();
        } catch (Exception e) {
            System.err.println("Error creating instance of class " + className + " " + e.getMessage());
            return null;
        }
    }


    public static void notifyMessages(Project project, @NotNull String title, @NotNull String description, Type type, boolean important, @Nullable Collection<String> messages) {
        notifier.notifyMessages(project, title, description, type, important, messages);
    }

    public static void notifySuccess(Project project, String title, String description) {
        notifier.notifySuccess(project, title, description);
    }

    public static void notifyError(Project project, String title, String description) {
        notifier.notifyError(project, title, description);
    }

    public static void notifyImportantError(Project project, String title, String description) {
        notifier.notifyImportantError(project, title, description);
    }

    public static void notifyMessage(@Nullable Project project, @NotNull String title, @NotNull String description, Type type, boolean important, @Nullable Set<Exception> errors) {
        notifier.notifyMessage(project, title, description, type, important, errors);
    }

    public static void notifyError(@Nullable Project project, @NotNull String title, @NotNull String description, boolean important, @Nullable Exception error) {
        notifier.notifyError(project, title, description, important, error);
    }

    public static void notifyModalError(String title, String description) {
        notifier.notifyModalError(description, title);
    }

    public static void notifyModalInfo(String title, String description) {
        notifier.notifyModalInfo(description, title);
    }

    static class CreateNewAppDialog extends DialogWrapper {

        private JComboBox stackField;
        private JTextField appNameField;

        protected CreateNewAppDialog() {
            super(true);
            setTitle("Add Config Variable");
            init();
        }

        @Override
        protected JComponent createCenterPanel() {
            final DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("right:pref, 6dlu, pref, pref:grow", "pref"));
            builder.append("Heroku Stack", stackField = new JComboBox(Heroku.Stack.values()),1);
            stackField.setSelectedItem(Heroku.Stack.Cedar);
            builder.nextLine();
            builder.append("Application Name", appNameField = new JTextField(50),2);
            return builder.getPanel();
        }

        public Pair<String,Heroku.Stack> getApplicationInfo() {
            return Pair.create(appNameField.getText(), (Heroku.Stack) stackField.getSelectedItem());
        }
    }
    
    public static Pair<String, Heroku.Stack> showCreateNewAppDialog() {
        final CreateNewAppDialog dialog = new CreateNewAppDialog();
        dialog.show();
        if (dialog.getExitCode()==CreateNewAppDialog.OK_EXIT_CODE) return dialog.getApplicationInfo();
        return null;
    }
}
