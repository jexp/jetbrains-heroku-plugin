package com.jetbrains.heroku.ui;

import com.intellij.openapi.diagnostic.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
* @author mh
* @since 29.12.11
*/
public abstract class BackgroundAction extends AbstractAction {
    private static final Logger LOG = Logger.getInstance(Action.class);

    public BackgroundAction(String name, Icon icon) {
        super(name, icon);
    }

    public BackgroundAction(String name) {
        super(name);
    }

    @Override
    public final void actionPerformed(final ActionEvent e) {
        setEnabled(false);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    runActionPerformed(e);
                } catch (Exception e) {
                    LOG.error("Error executing action "+getValue(Action.NAME),e);
                } finally {
                    setEnabled(true);
                }

            }
        });
    }

    public abstract void runActionPerformed(ActionEvent e);
}
