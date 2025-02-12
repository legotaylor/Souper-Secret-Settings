package com.nettakrim.souper_secret_settings.actions;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import java.util.Stack;

public class Actions {
    protected final Stack<Action> history;
    protected final Stack<Action> undone;

    public Actions() {
        history = new Stack<>();
        undone = new Stack<>();
        updateHistoryButtons();
    }

    protected boolean addToHistory(Action action) {
        if (!history.isEmpty()) {
            Action previous = history.peek();
            if (previous != null && previous.getClass().equals(action.getClass()) && previous.mergeWith(action)) {
                return false;
            }
        }

        history.add(action);
        undone.clear();
        updateHistoryButtons();
        return true;
    }

    public void undo() {
        if (history.isEmpty()) {
            updateHistoryButtons();
            return;
        }

        Action action = history.pop();
        if (action.undo()) {
            undone.add(action);
            updateHistoryButtons();
        } else {
            undo();
        }
    }

    public void redo() {
        if (undone.isEmpty()) {
            return;
        }

        Action action = undone.pop();
        action.redo();
        history.add(action);
        updateHistoryButtons();
    }

    private void updateHistoryButtons() {
        SouperSecretSettingsClient.soupGui.setHistoryButtons(!history.isEmpty(), !undone.isEmpty());
    }
}
