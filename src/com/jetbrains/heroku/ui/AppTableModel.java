package com.jetbrains.heroku.ui;

import com.heroku.api.App;

import javax.swing.table.AbstractTableModel;

/**
 * @author mh
 * @since 18.12.11
 */
public class AppTableModel extends AbstractTableModel {

    public AppTableModel(App app) {
        this.app = app;
    }

    enum rows {Name, Owner, Url, Created, Dynos, Workers}

    private App app = new App();

    public int getRowCount() {
        return rows.values().length;
    }

    public int getColumnCount() {
        return 2;
    }

    public Object getValueAt(int row, int column) {
        if (column == 0) return rowFor(row).name();
        switch (rowFor(row)) {
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

    private rows rowFor(int row) {
        return rows.values()[row];
    }

    @Override
    public String getColumnName(int column) {
        return column == 0 ? "Name" : "Value";
    }

    public AppTableModel update(App app) {
        this.app = app;
        fireTableDataChanged();
        return this;
    }
}
