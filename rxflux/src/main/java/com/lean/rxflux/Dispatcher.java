package com.lean.rxflux;


import org.greenrobot.eventbus.EventBus;

/**
 * @author lean
 */
public class Dispatcher {

    private static Dispatcher sinleton = new Dispatcher();

    private Dispatcher() {}

    public static Dispatcher singleton() {
        return sinleton;
    }

    private final EventBus bus = EventBus.getDefault();

    public void register(final Object store) {
        bus.register(store);
    }

    public void unregister(final Object store) {
        bus.unregister(store);
    }

    public void dispatch(Action action) {
        if (null == action) {
            throw new IllegalArgumentException("Action must not be empty");
        }

        bus.post(action);
    }

    public void post(Object object) {
        bus.post(object);
    }
}
