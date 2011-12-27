package com.jetbrains.heroku.ui;

import com.intellij.ide.BrowserUtil;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import java.util.concurrent.atomic.AtomicInteger;

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
                BrowserUtil.launchBrowser(e.getURL().toExternalForm());
              }
            });
        return label;
    }

    public static JScrollPane table(TableModel model) {
        return table(model, null);
    }

    public static JScrollPane table(TableModel model, final AtomicInteger selectedRow) {
        final JBTable table = new JBTable(model);
        table.setAutoCreateRowSorter(true);
        return ScrollPaneFactory.createScrollPane(withSelectionCallback(table, selectedRow));
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
        return new JLabel(value==null ? "" : value.toString());
    }
}
