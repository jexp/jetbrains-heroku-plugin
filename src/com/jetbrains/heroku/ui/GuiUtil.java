package com.jetbrains.heroku.ui;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.impl.BackgroundableActionEnabledHandler;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ErrorManager;

/**
 * @author mh
 * @since 27.12.11
 */
public class GuiUtil {

    public static HyperlinkLabel link(final String url) {
        if (url == null) {
            return new HyperlinkLabel();
        }
        final HyperlinkLabel label = new HyperlinkLabel(url);
        label.setHyperlinkTarget(url);
        label.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            protected void hyperlinkActivated(final HyperlinkEvent e) {
                if (e==null || e.getURL()==null) return;
                BrowserUtil.launchBrowser(e.getURL().toExternalForm());
            }
        });
        return label;
    }

    public static JScrollPane table(TableModel model) {
        return table(model, null);
    }

    public static JScrollPane table(TableModel model, final AtomicInteger selectedRow) {
        final JBTable table = createTable(model);
        table.setAutoCreateRowSorter(true);
        return ScrollPaneFactory.createScrollPane(withSelectionCallback(table, selectedRow));
    }

    private static JBTable createTable(TableModel model) {
        final JBTable table = new JBTable(model);
        table.setDefaultRenderer(Boolean.class, new BooleanCellRenderer());
        return table;
    }

    public static JTable withSelectionCallback(final JTable table, final AtomicInteger selectedRow) {
        if (selectedRow == null) return table;

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                final int viewRow = table.getSelectedRow();
                final int modelRow = viewRow == -1 ? -1 : table.convertRowIndexToModel(viewRow);
                selectedRow.set(modelRow);
            }
        });
        return table;
    }

    public static JLabel label(Object value) {
        return new JLabel(value == null ? "" : value.toString());
    }

    private static class BooleanCellRenderer implements TableCellRenderer {
        private final Icon checkIcon = IconLoader.getIcon("/actions/checked.png");
        final JLabel iconLabel = new JLabel(checkIcon,JLabel.CENTER);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            final boolean isTrue = value instanceof Boolean && ((Boolean) value);
            iconLabel.setIcon(isTrue ? checkIcon : null);
            iconLabel.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            iconLabel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return iconLabel;
        }
    }
    
    public static JButton button(Action action) {
        return new JButton(action);
    }
}
