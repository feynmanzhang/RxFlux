package com.lean.rxflux;

/**
 * @author lean
 */
public abstract class ActionsCreator {

    private final Dispatcher dispatcher;

    public ActionsCreator() {
        dispatcher = Dispatcher.singleton();
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public void postAction(Action action) {
        getDispatcher().dispatch(action);
    }
}
