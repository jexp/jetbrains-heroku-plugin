package com.jetbrains.heroku.ui;

import com.heroku.api.App;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mh
 * @since 18.12.11
 */
public class AppsTableModel extends AbstractTableModel {
    enum cols {Name, Owner, Url, Created, Dynos, Workers}

    private List<App> apps = Collections.emptyList();

    public int getRowCount() {
        return apps.size();
    }

    public int getColumnCount() {
        return cols.values().length;
    }

    public Object getValueAt(int row, int column) {
        App app = getApplication(row);
        switch (columnFor(cols.values()[column])) {
            case Name:
                return app.getName();
            case Owner:
                return app.getOwnerEmail();
            case Url:
                return app.getWebUrl();
            case Created:
                return app.getCreatedAt();
            case Dynos:
                return app.getDynos();
            case Workers:
                return app.getWorkers();
        }
        return null;
    }

    private cols columnFor(cols cols) {
        return cols;
    }

    @Override
    public String getColumnName(int column) {
        return columnFor(cols.values()[column]).name();
    }

    public AppsTableModel update(List<App> applications) {
        this.apps = applications;
        fireTableDataChanged();
        return this;
    }

    public App getApplication(int row) {
        return apps.get(row);
    }

    App getSelectedApplication(AtomicInteger selectedApplication) {
        final int row = selectedApplication.get();
        if (row == -1 || row >= getRowCount()) {
            return null;
        }
        return getApplication(row);
    }
}
