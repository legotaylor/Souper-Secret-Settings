package com.nettakrim.souper_secret_settings.actions;

import java.util.List;

public record ListAddAction<T>(List<T> list, T value) implements Action {
    @Override
    public boolean undo() {
        list.remove(value);
        return true;
    }

    @Override
    public void redo() {
        list.add(value);
    }
}
