package com.nettakrim.souper_secret_settings.actions;

import com.nettakrim.souper_secret_settings.shaders.Toggleable;

public class ToggleAction implements Action {
    protected final Toggleable toggleable;

    public ToggleAction(Toggleable toggleable) {
        this.toggleable = toggleable;
    }

    @Override
    public boolean undo() {
        toggleable.toggle();
        return true;
    }

    @Override
    public void redo() {
        toggleable.toggle();
    }
}
