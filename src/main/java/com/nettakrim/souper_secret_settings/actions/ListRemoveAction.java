package com.nettakrim.souper_secret_settings.actions;

import java.util.List;

public class ListRemoveAction<T> implements Action {
    protected final List<T> list;
    protected final int index;
    protected T value;

    public ListRemoveAction(List<T> list, int index) {
        this.list = list;
        this.index = index;
        this.value = list.get(index);
    }

    @Override
    public boolean undo() {
        list.add(index, value);
        return true;
    }

    @Override
    public void redo() {
        list.remove(index);
    }
}
