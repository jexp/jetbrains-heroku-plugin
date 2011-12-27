package com.jetbrains.heroku.ui;

import com.heroku.api.Collaborator;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mh
 * @since 26.12.11
 */
public class CollaboratorTableModel extends AbstractTableModel {
    private final List<Collaborator> collaborators;

    public CollaboratorTableModel(List<Collaborator> collaborators) {
        this.collaborators = new ArrayList<Collaborator>(collaborators);
    }

    @Override
    public int getRowCount() {
        return collaborators.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int row, int column) {
        final Collaborator collaborator = getCollaborator(row);
        return column == 0 ? collaborator.getEmail() : collaborator.getAccess();
    }

    public Collaborator getCollaborator(int row) {
        if (row == -1) return null;
        return collaborators.get(row);
    }

    @Override
    public String getColumnName(int column) {
        return column == 0 ? "Email" : "Access";
    }

    public void update(List<Collaborator> data) {
        this.collaborators.clear();
        this.collaborators.addAll(data);
        fireTableDataChanged();
    }
}
