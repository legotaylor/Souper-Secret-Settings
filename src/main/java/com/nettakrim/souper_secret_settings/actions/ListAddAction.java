package com.nettakrim.souper_secret_settings.actions;

import java.util.List;

public record ListAddAction<T>(List<T> list, T value) implements Action {
    @Override
    public void undo() {
        list.remove(value);
    }

    @Override
    public void redo() {
        list.add(value);
    }
}
