package com.jetbrains.heroku.notification;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author mh
 * @since 28.12.11
 */
public class OldNotifier extends Notifier {

    @Override
    public void notifyMessages(Project project, @NotNull String title, @NotNull String description, Type type, boolean important, @Nullable Collection<String> messages) {
        if (type == Type.ERROR) {
            notifyModalError(title, toHtml(description, messages));
        } else {
            notifyModalInfo(title, toHtml(description, messages));
        }
    }
    public void notifyModalError(String title, String description) {
        Messages.showErrorDialog(title, description);
    }

    public void notifyModalInfo(String title, String description) {
        Messages.showInfoMessage(title, description);
    }
}
