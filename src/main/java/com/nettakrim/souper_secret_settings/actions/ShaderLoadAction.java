package com.nettakrim.souper_secret_settings.actions;

import com.nettakrim.souper_secret_settings.data.LayerCodecs;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;

public class ShaderLoadAction implements Action {
    protected ShaderLayer layer;
    protected String id;
    protected LayerCodecs backup;

    public ShaderLoadAction(ShaderLayer layer, String id) {
        this.layer = layer;
        this.id = id;
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
        return ((ShaderLoadAction)other).layer == layer && id != null && id.equals(((ShaderLoadAction)other).id);
    }
}
