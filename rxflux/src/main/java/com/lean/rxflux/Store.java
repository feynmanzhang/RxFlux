package com.lean.rxflux;


/**
 * @author lean
 */
public abstract class Store {


    private final Dispatcher dispatcher;

    public Store() {
        dispatcher = Dispatcher.singleton();
    }

    public void register(final Object view) {
        dispatcher.register(view);
    }

    public void unregister(final Object view) {
        dispatcher.unregister(view);
    }

    public void emitStoreChange() {
        dispatcher.post(changeEvent());
    }


    /* abstract */
    public abstract void onAction(Action action);

    public abstract StoreChangeEvent changeEvent();

    /* event interface, IOC */
    public interface StoreChangeEvent {}
}
