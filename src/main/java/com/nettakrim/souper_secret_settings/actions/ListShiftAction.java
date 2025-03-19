package com.nettakrim.souper_secret_settings.actions;

import java.util.List;

public class ListShiftAction<T> implements Action {
    protected final List<T> list;
    protected final int index;
    protected int direction;

    public ListShiftAction(List<T> list, int index, int direction) {
        this.list = list;
        this.index = index;
        this.direction = direction;
    }

    @Override
    public boolean undo() {
        if (direction == 0) {
            return false;
        }

        T value = list.remove(index+direction);
        list.add(index, value);
        return true;
    }

    @Override
    public void redo() {
        T value = list.remove(index);
        list.add(index+direction, value);
    }

    @Override
    public boolean mergeWith(Action other) {
        ListShiftAction<?> action = (ListShiftAction<?>)other;
        if (action.list != list || action.index-direction != index) {
            return false;
        }

        direction += action.direction;
        return true;
    }
}
