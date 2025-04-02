package com.nettakrim.souper_secret_settings.actions;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import java.util.Stack;

public class Actions {
    protected final Stack<Action> history;
    protected final Stack<Action> undone;

    public Actions() {
        history = new Stack<>();
        undone = new Stack<>();
        onChange();
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
        onChange();
        return true;
    }

    public void undo() {
        if (history.isEmpty()) {
            onChange();
            return;
        }

        Action action = history.pop();
        if (action.undo()) {
            undone.add(action);
            onChange();
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
        onChange();
    }

    private void onChange() {
        SouperSecretSettingsClient.soupGui.setHistoryButtons(history.size(), undone.size());

        if (SouperSecretSettingsClient.soupData.config.disableState == 1) {
            SouperSecretSettingsClient.soupData.config.disableState = 0;
            SouperSecretSettingsClient.say("option.toggle.prompt", 1);
        }
    }
}
