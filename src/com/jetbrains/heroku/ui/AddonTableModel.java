package com.jetbrains.heroku.ui;

import com.heroku.api.Addon;

import javax.swing.table.AbstractTableModel;
import java.net.URL;
import java.util.*;

/**
* @author mh
* @since 26.12.11
*/
public class AddonTableModel extends AbstractTableModel {
    private final List<Addon> addons;
    private Map<String,Addon> appAddons=new HashMap<String, Addon>();

    public AddonTableModel(List<Addon> allAddons, List<Addon> appAddons) {
        this.addons = new ArrayList<Addon>(allAddons);
        update(appAddons);
        Collections.sort(addons, new AddonComparator());
    }

    public void update(List<Addon> data) {
        this.appAddons.clear();
        for (Addon addon : data) {
            this.appAddons.put(addon.getName(),addon);
        }
        Collections.sort(addons, new AddonComparator());
        fireTableDataChanged();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        final Column column = columnFor(columnIndex);
        if (column==Column.Installed) return Boolean.class;
        if (column==Column.Url) return URL.class;
        if (column==Column.Price) return Price.class;
        return String.class;
    }

    public Addon getAddOn(int row) {
        if (row==-1) return null;
        final Addon addon = addons.get(row);
        if (isInstalled(addon)) return appAddons.get(addon.getName());
        return addon;
    }

    enum Column {Name, Installed, Description, Price, State, Url} // configured, beta

    @Override
    public int getRowCount() {
        return addons.size();
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        Addon addon = getAddOn(row);

        switch (columnFor(col)) {
            case Name:
                return addon.getName();
            case Installed:
                return isInstalled(addon);
            case Description:
                return addon.getDescription();
            /*case configured:
                return addon.getConfigured();
            */
            case State:
                return addon.getState();
            case Price:
                return new Price(addon.getPriceCents(),addon.getPriceUnit());
            case Url:
                return addon.getUrl();
        }
        return null;
    }

    public boolean isInstalled(Addon addon) {
        return appAddons.containsKey(addon.getName());
    }

    private Column columnFor(int column) {
        return Column.values()[column];
    }

    @Override
    public String getColumnName(int column) {
        return columnFor(column).name();
    }

    private class AddonComparator implements Comparator<Addon> {
        @Override
        public int compare(Addon o1, Addon o2) {
            final Boolean installed1 = isInstalled(o1);
            final Boolean installed2 = isInstalled(o2);
            final int r1 = installed2.compareTo(installed1);
            if (r1!=0) return r1;
            return o1.getName().compareTo(o2.getName());
        }
    }
}
