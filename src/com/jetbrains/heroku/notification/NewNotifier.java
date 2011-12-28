package com.jetbrains.heroku.notification;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author mh
 * @since 28.12.11
 */
public class NewNotifier extends Notifier {
    private static final NotificationGroup HEROKU_BALLON_NOTIFY = new NotificationGroup("Heroku", NotificationDisplayType.STICKY_BALLOON, true);
    private static final NotificationGroup HEROKU_LIGHT_BALLON_NOTIFY = new NotificationGroup("Heroku", NotificationDisplayType.BALLOON, true);
    private static final NotificationGroup HEROKU_TOOLWINDOW_NOTIFY = NotificationGroup.toolWindowGroup("Heroku", "Heroku", true);
    private static final NotificationGroup HEROKU_LOG_NOTIFY = NotificationGroup.logOnlyGroup("Heroku");

    public void notifyMessages(Project project, @NotNull String title, @NotNull String description, Type type, boolean important, @Nullable Collection<String> messages) {
        NotificationGroup group = important ? HEROKU_BALLON_NOTIFY : HEROKU_LOG_NOTIFY;
        final NotificationType notificationType = toNotificationType(type, important);
        final Notification notification = group.createNotification(title, toHtml(description, messages), notificationType, null);
        Notifications.Bus.notify(notification, project);
    }

    private NotificationType toNotificationType(Type type, boolean important) {
        if (type != Type.ERROR) return NotificationType.INFORMATION;
        if (important) return NotificationType.ERROR;
        return NotificationType.WARNING;
    }

}
