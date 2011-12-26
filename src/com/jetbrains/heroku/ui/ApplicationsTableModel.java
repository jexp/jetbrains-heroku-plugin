package com.jetbrains.heroku.ui;

import com.jetbrains.heroku.herokuapi.Application;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mh
 * @since 18.12.11
 */
public class ApplicationsTableModel extends AbstractTableModel {
    String[] cols = {"name", "owner_email", "web_url", "created_at", "dynos", "workers"};
    List<Application> apps = Collections.emptyList();

    public int getRowCount() {
        return apps.size();
    }

    public int getColumnCount() {
        return cols.length;
    }

    public Object getValueAt(int row, int column) {
        Application app = getApplication(row);
        return app.get(getColumnName(column));
    }

    @Override
    public String getColumnName(int column) {
        return cols[column];
    }

    public ApplicationsTableModel update(List<Application> applications) {
        this.apps = applications;
        fireTableDataChanged();
        return this;
    }

    public Application getApplication(int row) {
        return apps.get(row);
    }

    Application getSelectedApplication(AtomicInteger selectedApplication) {
        final int row = selectedApplication.get();
        if (row == -1 || row >= getRowCount()) {
            return null;
        }
        return getApplication(row);
    }
}
