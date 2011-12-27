package com.jetbrains.heroku.ui;

import javax.swing.table.AbstractTableModel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
* @author mh
* @since 26.12.11
*/
public class MapTableModel extends AbstractTableModel {
    private final Map<String, Object> data;
    private final String keyTitle;
    private final String valueTitle;

    public MapTableModel(Map<String, Object> data, String keyTitle, String valueTitle) {
        this.data = new HashMap<String, Object> (data);
        this.keyTitle = keyTitle;
        this.valueTitle = valueTitle;
    }

    @Override
    public String getColumnName(int i) {
        return i == 0 ? keyTitle : valueTitle;
    }

    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return 2;
    }

    public Object getValueAt(int row, int column) {
        if (column == 0) return getKey(row);
        if (column == 1) return data.get(getKey(row));
        return null;
    }

    public String getKey(int row) {
        if (row==-1) return null;
        Iterator<String> it = data.keySet().iterator();
        for (int i = 0; i < row && it.hasNext(); i++) {
            it.next();
        }
        return it.next();
    }

    public void update(Map<String,Object> data) {
        this.data.clear();
        this.data.putAll(data);
        fireTableDataChanged();
    }
}
