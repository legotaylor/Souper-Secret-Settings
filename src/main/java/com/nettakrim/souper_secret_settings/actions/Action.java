package com.nettakrim.souper_secret_settings.actions;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

public interface Action {
    void undo();

    void redo();

    default boolean mergeWith(Action other) {
        return false;
    }

    default void addToHistory() {
        SouperSecretSettingsClient.actions.addToHistory(this);
    }
}
