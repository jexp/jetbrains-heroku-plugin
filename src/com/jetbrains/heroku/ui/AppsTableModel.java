package com.jetbrains.heroku.ui;

import com.heroku.api.App;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.List;

/**
 * @author mh
 * @since 18.12.11
 */
public class AppsTableModel extends AbstractTableModel {

    private App hightlightedApp;

    public void highlight(App hightlightedApp) {
        this.hightlightedApp = hightlightedApp;
    }

    enum Columns {InProject, Name, Owner, Url, Created, Dynos, Workers}

    private List<App> apps = Collections.emptyList();

    public int getRowCount() {
        return apps.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        final Columns columns = columnFor(columnIndex);
        if (columns==Columns.InProject) return Boolean.class;
        return super.getColumnClass(columnIndex);
    }

    public int getColumnCount() {
        return Columns.values().length;
    }

    public Object getValueAt(int row, int column) {
        App app = getApplication(row);
        switch (columnFor(column)) {
            case InProject:
                return hightlightedApp !=null && app.getName().equals(hightlightedApp.getName());
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

    private Columns columnFor(int column) {
        return Columns.values()[column];
    }

    @Override
    public String getColumnName(int column) {
        return columnFor(column).name();
    }

    public AppsTableModel update(List<App> applications) {
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
