package com.nettakrim.souper_secret_settings.actions;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

public interface Action {
    void backup();

    void undo();

    void redo();

    boolean mergeWith(Action other);

    default void addToHistory() {
        SouperSecretSettingsClient.actions.addToHistory(this);
    }
}
