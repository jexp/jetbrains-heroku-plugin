package com.jetbrains.heroku.ui;

import com.jetbrains.heroku.git.GitRemoteInfo;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
* @author mh
* @since 26.12.11
*/
public class GitRemotesTableModel extends AbstractTableModel {
    private final List<GitRemoteInfo> remotes;

    public GitRemotesTableModel(List<GitRemoteInfo> remotes) {
        this.remotes = remotes;
    }

    public int getRowCount() {
        return remotes.size();
    }

    public int getColumnCount() {
        return 2;
    }

    public Object getValueAt(int row, int col) {
        final GitRemoteInfo remote = remotes.get(row);
        switch (col) {
            case 0:
                return remote.getName();
            case 1:
                return remote.getUrl();
        }
        return null;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case 0:
                return "Name";
            case 1:
                return "URL";
        }
        return null;
    }
}
