package com.nettakrim.souper_secret_settings.actions;

import java.util.Stack;

public class Actions {
    protected Stack<Action> history;

    public Actions() {
        history = new Stack<>();
    }

    protected void addToHistory(Action action) {
        if (!history.isEmpty()) {
            Action previous = history.peek();
            if (previous != null && previous.getClass().equals(action.getClass()) && previous.mergeWith(action)) {
                return;
            }
        }
        action.backup();
        history.add(action);
    }

    public void undo() {
        if (history.isEmpty()) {
            return;
        }

        Action action = history.pop();
        if (action != null) {
            action.undo();
        }
    }
}
