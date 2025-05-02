package com.nettakrim.souper_secret_settings.actions;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

public class Actions {
    protected final Deque<Action> history;
    protected final Stack<Action> undone;

    public static int defaultLength = 8192;

    public Actions() {
        history = new ArrayDeque<>();
        undone = new Stack<>();
        onChange();
    }

    protected boolean addToHistory(Action action) {
        if (!history.isEmpty()) {
            Action previous = history.peekLast();
            if (previous != null && previous.getClass().equals(action.getClass()) && previous.mergeWith(action)) {
                return false;
            }
        }

        while (history.size() >= SouperSecretSettingsClient.soupData.config.undoLimit) {
            history.removeFirst();
        }

        history.addLast(action);
        undone.clear();
        onChange();
        return true;
    }

    public boolean undo() {
        if (history.isEmpty()) {
            onChange();
            return false;
        }

        Action action = history.removeLast();
        if (action.undo()) {
            undone.add(action);
            onChange();
            tryFixActiveLayer(action);
            return true;
        } else {
            return undo();
        }
    }

    public boolean redo() {
        if (undone.isEmpty()) {
            return false;
        }

        Action action = undone.pop();
        action.redo();
        history.addLast(action);
        onChange();
        tryFixActiveLayer(action);

        return true;
    }

    public void clear() {
        history.clear();
        undone.clear();
        onChange();
    }

    protected void onChange() {
        SouperSecretSettingsClient.soupGui.setHistoryButtons(history.size(), undone.size());

        if (SouperSecretSettingsClient.soupData.config.disableState == 1) {
            SouperSecretSettingsClient.soupData.config.disableState = 0;
            SouperSecretSettingsClient.say("option.toggle.prompt", 1);
        }

        if (SouperSecretSettingsClient.soupRenderer.randomTimer > 0) {
            SouperSecretSettingsClient.soupRenderer.randomTimer = 0;
            SouperSecretSettingsClient.say("option.random.prompt", 1);
        }
    }

    protected void tryFixActiveLayer(Action action) {
        if ((action instanceof ListAddAction<?> listAddAction && listAddAction.list() == SouperSecretSettingsClient.soupRenderer.shaderLayers) || (action instanceof ListRemoveAction<?> listRemoveAction && listRemoveAction.list == SouperSecretSettingsClient.soupRenderer.shaderLayers)) {
            SouperSecretSettingsClient.soupRenderer.fixActiveLayer();
        }
    }
}
