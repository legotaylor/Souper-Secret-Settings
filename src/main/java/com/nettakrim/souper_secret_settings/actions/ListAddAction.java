package com.nettakrim.souper_secret_settings.actions;

import java.util.List;

public record ListAddAction<T>(List<T> list, T value, int index) implements Action {
    @Override
    public boolean undo() {
        list.remove(index);
        return true;
    }

    @Override
    public void redo() {
        list.add(index, value);
    }
}
