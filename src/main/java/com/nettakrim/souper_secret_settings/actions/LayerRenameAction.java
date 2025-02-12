package com.nettakrim.souper_secret_settings.actions;

import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;

public class LayerRenameAction implements Action {
    protected final ShaderLayer layer;
    protected String name;

    public LayerRenameAction(ShaderLayer layer) {
        this.layer = layer;
        this.name = layer.name;
    }

    @Override
    public boolean undo() {
        if (layer.name.equals(name)) {
            return false;
        }
        swap();
        return true;
    }

    @Override
    public void redo() {
        swap();
    }

    protected void swap() {
        String temp = layer.name;
        layer.name = name;
        name = temp;
    }

    @Override
    public boolean mergeWith(Action other) {
        return ((LayerRenameAction)other).layer == layer;
    }
}
