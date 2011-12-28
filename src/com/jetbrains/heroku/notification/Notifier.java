package com.jetbrains.heroku.notification;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author mh
 * @since 28.12.11
 */
public abstract class Notifier {

    protected String toHtml(String description, Collection<String> messages) {
        String desc = toHtml(description);
        if (messages != null) {
            desc += "<hr/>";
            for (String message : messages) {
                desc += toHtml(message);
            }
        }
        return desc;
    }

    public void notifySuccess(Project project, String title, String description) {
        notifyMessage(project, title, description, Type.INFORMATION, true, null);
    }

    public void notifyError(Project project, String title, String description) {
        notifyMessage(project, title, description, Type.ERROR, false, null);
    }

    public void notifyImportantError(Project project, String title, String description) {
        notifyMessage(project, title, description, Type.ERROR, true, null);
    }

    public void notifyMessage(@Nullable Project project, @NotNull String title, @NotNull String description, Type type, boolean important, @Nullable Set<Exception> errors) {
        Collection<String> errorMessages = errorMessages(errors);
        notifyMessages(project, title, description, type, important, errorMessages);
    }

    private Collection<String> errorMessages(Set<Exception> errors) {
        if (errors == null) return Collections.emptySet();

        Collection<String> errorMessages = new HashSet<String>(errors.size());
        for (Exception error : errors) {
            if (error == null) continue;
            errorMessages.add(error.getMessage());
        }
        return errorMessages;
    }

    private String toHtml(String message) {
        return message.replace("\n", "<br/>");
    }

    public void notifyError(@Nullable Project project, @NotNull String title, @NotNull String description, boolean important, @Nullable Exception error) {
        notifyMessage(project, title, description, Type.ERROR, important, Collections.singleton(error));
    }

    public abstract void notifyMessages(Project project, @NotNull String title, @NotNull String description, Type type, boolean important, @Nullable Collection<String> messages);

    public void notifyModalError(String title, String description) {
        Messages.showErrorDialog(description, title);
    }

    public void notifyModalInfo(String title, String description) {
        Messages.showInfoMessage(description, title);
    }

}
