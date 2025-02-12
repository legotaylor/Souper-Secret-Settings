package com.nettakrim.souper_secret_settings.actions;

public class ArrSetAction<T> implements Action {
    protected final T[] arr;
    protected final int i;
    protected T value;

    public ArrSetAction(T[] arr, int i) {
        this.arr = arr;
        this.i = i;
        value = arr[i];
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
        T temp = arr[i];
        arr[i] = value;
        value = temp;
    }

    @Override
    public boolean mergeWith(Action other) {
        ArrSetAction<?> action = (ArrSetAction<?>)other;
        return action.arr == arr && action.i == i;
    }
}
