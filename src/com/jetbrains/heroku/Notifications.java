package com.jetbrains.heroku;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.intellij.notification.Notifications.Bus;

/**
 * @author mh
 * @since 27.12.11
 */
public class Notifications {
    private static final NotificationGroup HEROKU_BALLON_NOTIFY = new NotificationGroup("Heroku", NotificationDisplayType.STICKY_BALLOON,true);
    private static final NotificationGroup HEROKU_LIGHT_BALLON_NOTIFY = new NotificationGroup("Heroku", NotificationDisplayType.BALLOON,true);
    private static final NotificationGroup HEROKU_TOOLWINDOW_NOTIFY = NotificationGroup.toolWindowGroup("Heroku","Heroku",true);
    private static final NotificationGroup HEROKU_LOG_NOTIFY = NotificationGroup.logOnlyGroup("Heroku");

    public static void notifyMessages(Project project, @NotNull String title, @NotNull String description, NotificationType type, boolean important, @Nullable Collection<String> messages) {
        NotificationGroup group = important ? HEROKU_BALLON_NOTIFY : HEROKU_LOG_NOTIFY;
        final Notification notification = group.createNotification(title, toHtml(description, messages), type, null);
        Bus.notify(notification, project);
    }

    private static String toHtml(String description, Collection<String> messages) {
        String desc = toHtml(description);
        if (messages != null) {
            desc += "<hr/>";
            for (String message : messages) {
                desc += toHtml(message);
            }
        }
        return desc;
    }

    public static void notifySuccess(Project project, String title, String description) {
        Bus.notify(HEROKU_BALLON_NOTIFY.createNotification(title, description, NotificationType.INFORMATION,null), project);
    }

    public static void notifyError(Project project, String title, String description) {
        Bus.notify(HEROKU_BALLON_NOTIFY.createNotification(title, description, NotificationType.ERROR, null), project);
    }

    public static void notifyImportantError(Project project, String title, String description) {
        Bus.notify(HEROKU_BALLON_NOTIFY.createNotification(title, description, NotificationType.ERROR, null), project);
    }

    public static void notifyMessage(@Nullable Project project, @NotNull String title, @NotNull String description, NotificationType type, boolean important, @Nullable Set<Exception> errors) {
        Collection<String> errorMessages = errorMessages(errors);
        notifyMessages(project, title, description, type, important, errorMessages);
    }

    private static Collection<String> errorMessages(Set<Exception> errors) {
        if (errors == null) return Collections.emptySet();

        Collection<String> errorMessages = new HashSet<String>(errors.size());
        for (Exception error : errors) {
            if (error==null) continue;
            errorMessages.add(error.getMessage());
        }
        return errorMessages;
    }

    private static String toHtml(String message) {
        return message.replace("\n", "<br/>");
    }

    public static void notifyError(@Nullable Project project, @NotNull String title, @NotNull String description, boolean important, @Nullable Exception error) {
        notifyMessage(project, title, description, NotificationType.ERROR, important, Collections.singleton(error));
    }
    
    public static void notifyModalError(String title, String description) {
        Messages.showErrorDialog(description,title);
    }
    public static void notifyModalSuccess(String title, String description) {
        Messages.showInfoMessage(description,title);
    }
}
