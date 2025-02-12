package com.nettakrim.souper_secret_settings.actions;

import java.util.List;

public class ListRemoveAction<T> implements Action {
    public final List<T> list;
    public final int index;
    protected T value;

    public ListRemoveAction(List<T> list, int index) {
        this.list = list;
        this.index = index;
        this.value = list.get(index);
    }

    @Override
    public void undo() {
        list.add(index, value);
    }

    @Override
    public void redo() {
        list.remove(index);
    }
}
