package com.jetbrains.heroku.ui;

import com.heroku.api.Release;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mh
 * @since 26.12.11
 */
public class ReleaseTableModel extends AbstractTableModel {
    private final List<Release> releases;

    public ReleaseTableModel(List<Release> releases) {
        this.releases = new ArrayList<Release>(releases);
    }

    @Override
    public int getRowCount() {
        return releases.size();
    }

    @Override
    public int getColumnCount() {
        return Columns.values().length;
    }

    public String renderRelease(Release releaseInfo) {
        StringBuilder sb=new StringBuilder();
        sb.append("<table>");
        for (Columns columns : Columns.values()) {
            sb.append(String.format("<tr><td>%s</td><td>%s</td></tr>",columns.name(),getReleaseDetail(releaseInfo,columns)));
        }
        sb.append("</table>");
        return sb.toString();
    }

    enum Columns {Name, Description, User, Commit, CreatedAt, Addons, Env, PsTable}

    @Override
    public Object getValueAt(int row, int column) {
        final Release release = getRelease(row);
        final Columns columns = columnFor(column);
        return getReleaseDetail(release, columns);
    }

    private Object getReleaseDetail(Release release, Columns columns) {
        switch (columns) {
            case Name:
                return release.getName();
            case Description:
                return release.getDescription();
            case User:
                return release.getUser();
            case Commit:
                return release.getCommit();
            case CreatedAt:
                return release.getCreatedAt();
            case Env:
                return release.getEnv();
            case Addons:
                return release.getAddons();
            case PsTable:
                return release.getPSTable();
        }
        return null;
    }

    private Columns columnFor(int col) {
        return Columns.values()[col];
    }

    public Release getRelease(int row) {
        if (row == -1) return null;
        return releases.get(row);
    }

    @Override
    public String getColumnName(int column) {
        return columnFor(column).name();
    }

    public void update(List<Release> data) {
        this.releases.clear();
        this.releases.addAll(data);
        fireTableDataChanged();
    }
}
