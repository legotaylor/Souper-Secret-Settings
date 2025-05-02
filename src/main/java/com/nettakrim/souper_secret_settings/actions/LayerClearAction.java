package com.nettakrim.souper_secret_settings.actions;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;

public class LayerClearAction implements Action {
    private ListRemoveAction<?> removeAction;

    @Override
    public boolean undo() {
        removeAction.list.clear();
        removeAction.undo();
        SouperSecretSettingsClient.soupRenderer.activeLayer = (ShaderLayer)removeAction.value;
        return true;
    }

    @Override
    public void redo() {
        removeAction.redo();
        SouperSecretSettingsClient.soupRenderer.loadDefault();
    }

    @Override
    public boolean mergeWith(Action other) {
        // if last action was also resetting the layer (mergeWith ensures class matches), it can be merged
        // because LayerClearAction removes the last action, something about the execution order means the hover amounts on the undo buttons don't update properly
        SouperSecretSettingsClient.actions.onChange();
        return true;
    }

    @Override
    public void addToHistory() {
        removeAction = (ListRemoveAction<?>)SouperSecretSettingsClient.actions.history.pollLast();
        Action.super.addToHistory();
    }
}
