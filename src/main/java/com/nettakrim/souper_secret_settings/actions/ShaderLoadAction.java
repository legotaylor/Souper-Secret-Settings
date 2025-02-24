package com.nettakrim.souper_secret_settings.actions;

import com.nettakrim.souper_secret_settings.data.LayerCodecs;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;

public class ShaderLoadAction implements Action {
    protected ShaderLayer layer;
    protected LayerCodecs backup;

    public ShaderLoadAction(ShaderLayer layer) {
        this.layer = layer;
        backup = LayerCodecs.from(layer);
    }

    @Override
    public boolean undo() {
        swap();
        return true;
    }

    @Override
    public void redo() {
        swap();
    }

    protected void swap() {
        LayerCodecs temp = LayerCodecs.from(layer);
        layer.clear();
        backup.apply(layer);
        backup = temp;
    }

    @Override
    public boolean mergeWith(Action other) {
        // its safe to do a somewhat simple check here because a layer rename action will happen in between shader load actions if they are different
        return ((ShaderLoadAction)other).layer == layer;
    }
}
