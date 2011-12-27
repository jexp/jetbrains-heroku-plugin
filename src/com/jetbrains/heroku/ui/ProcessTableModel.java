package com.jetbrains.heroku.ui;

import com.heroku.api.Proc;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mh
 * @since 26.12.11
 */
public class ProcessTableModel extends AbstractTableModel {
    private final List<Proc> processes;

    public ProcessTableModel(List<Proc> processes) {
        this.processes = new ArrayList<Proc>(processes);
    }

    @Override
    public int getRowCount() {
        return processes.size();
    }

    @Override
    public int getColumnCount() {
        return Columns.values().length;
    }

    enum Columns { Pid, Process, Type, Command, AppName, Slug, Action, State, PrettyState, TransitionedAt, Elapsed, Attached, RendezvousUrl}

    @Override
    public Object getValueAt(int row, int column) {
        final Proc process = getProcess(row);
        switch (columnFor(column)) {
            case Pid:
                return process.getUpid();
            case Process:
                return process.getProcess();
            case Type:
                return process.getType();
            case Command:
                return process.getCommand();
            case AppName:
                return process.getAppName();
            case Slug:
                return process.getSlug();
            case Action:
                return process.getAction();
            case State:
                return process.getState();
            case PrettyState:
                return process.getPrettyState();
            case TransitionedAt:
                return process.getTransitionedAt();
            case Elapsed:
                return process.getElapsed();
            case Attached:
                return process.isAttached();
            case RendezvousUrl:
                return process.getRendezvousUrl();
        }
        return null;
    }

    private Columns columnFor(int col) {
        return Columns.values()[col];
    }
    public Proc getProcess(int row) {
        if (row == -1) return null;
        return processes.get(row);
    }

    @Override
    public String getColumnName(int column) {
        return columnFor(column).name();
    }

    public void update(List<Proc> data) {
        this.processes.clear();
        this.processes.addAll(data);
        fireTableDataChanged();
    }
}
