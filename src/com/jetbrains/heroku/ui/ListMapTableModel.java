package com.jetbrains.heroku.ui;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.Map;

/**
* @author mh
* @since 26.12.11
*/
public class ListMapTableModel extends AbstractTableModel {
    private final List<Map<String, Object>> info;
    private final String[] columns;

    public ListMapTableModel(List<Map<String, Object>> info, String... columns) {
        this.info = info;
        this.columns = columns;
    }

    public int getRowCount() {
        return info.size();
    }

    public int getColumnCount() {
        return columns.length;
    }

    public Object getValueAt(int row, int column) {
        return info.get(row).get(columns[column]);
    }

    @Override
    public String getColumnName(int i) {
        return columns[i];
    }
}
