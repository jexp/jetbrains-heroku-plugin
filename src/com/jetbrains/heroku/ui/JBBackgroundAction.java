package com.jetbrains.heroku.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
* @author mh
* @since 29.12.11
*/
public abstract class JBBackgroundAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(AnAction.class);

    JBBackgroundAction(String text, String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void actionPerformed(final AnActionEvent anActionEvent) {
        final Presentation presentation = anActionEvent.getPresentation();
        presentation.setEnabled(false);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    runActionPerformed(anActionEvent);
                } catch (Exception e) {
                    LOG.error("Error executing action " + JBBackgroundAction.this.toString(), e);
                } finally {
                    presentation.setEnabled(true);
                }

            }
        });
    }

    public abstract void runActionPerformed(AnActionEvent anActionEvent);
}
