package com.jetbrains.heroku.notification;

import com.heroku.api.Heroku;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.jetbrains.heroku.notification.Notifier;
import com.jetbrains.heroku.notification.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public static Pair<String, Heroku.Stack> showCreateNewAppDialog() {
        final Heroku.Stack[] stacks = Heroku.Stack.values();
        String[] stackNames = new String[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            stackNames[i] = stacks[i].name();
        }
        final String initialStack = stackNames[Heroku.Stack.Cedar.ordinal()];
        final int index = Messages.showChooseDialog("Please enter the Stack of the new Application", "Application Stack", stackNames, initialStack, Messages.getQuestionIcon());
        if (index<0 || index>= stacks.length) return null;
        final String appName = Messages.showInputDialog("Please enter the new Heroku Application Name or leave blank for default:", "New Heroku Application Name", Messages.getQuestionIcon());
        if (appName==null) return null;
        return Pair.create(appName,stacks[index]);
    }
}
