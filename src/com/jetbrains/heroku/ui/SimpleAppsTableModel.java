package com.jetbrains.heroku.ui;

import com.heroku.api.App;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.List;

/**
 * @author mh
 * @since 18.12.11
 */
public class SimpleAppsTableModel extends AbstractTableModel {

    enum Columns {Name, Owner, Url, Created}

    private List<App> apps = Collections.emptyList();

    public int getRowCount() {
        return apps.size();
    }


    public int getColumnCount() {
        return Columns.values().length;
    }

    public Object getValueAt(int row, int column) {
        App app = getApplication(row);
        switch (columnFor(column)) {
            case Name:
                return app.getName();
            case Owner:
                return app.getOwnerEmail();
            case Url:
                return app.getWebUrl();
            case Created:
                return app.getCreatedAt();
        }
        return null;
    }

    private Columns columnFor(int column) {
        return Columns.values()[column];
    }

    @Override
    public String getColumnName(int column) {
        return columnFor(column).name();
    }

    public SimpleAppsTableModel update(List<App> applications) {
        this.apps = applications;
        fireTableDataChanged();
        return this;
    }

    public App getApplication(int row) {
        if (row == -1 || row >= getRowCount()) {
            return null;
        }
        return apps.get(row);
    }
}
