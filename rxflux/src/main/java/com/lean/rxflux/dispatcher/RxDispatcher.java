package com.lean.rxflux.dispatcher;


import com.lean.rxflux.store.RxStore;
import com.lean.rxflux.store.RxStoreChange;
import com.lean.rxflux.action.RxAction;

import java.util.HashMap;
import java.util.Map;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Dispather, 分发器，单例
 *
 * @author lean
 */
public class RxDispatcher {

    private static RxDispatcher singleton = new RxDispatcher();
    private final RxBus bus = RxBus.getSingleton();
    private Map<String, Subscription> rxStoreMap = new HashMap<>();
    private Map<String, Subscription> rxViewMap = new HashMap<>();

    private RxDispatcher() {}

    public static RxDispatcher singleton() {
        return singleton;
    }

    public <T extends RxStore> void subscribeRxStore(final T store) {
        final String tag = store.getClass().getSimpleName();
        Subscription subscription = rxStoreMap.get(tag);
        if (subscription == null || subscription.isUnsubscribed()) {
            rxStoreMap.put(tag, bus.get().onBackpressureBuffer().filter(new Func1<Object, Boolean>() {
                @Override
                public Boolean call(Object o) {
                    return o instanceof RxAction;
                }
            }).subscribe(new Action1<Object>() {
                @Override
                public void call(Object o) {
                    store.onAction((RxAction) o);
                }
            }));
        }
    }

    public void unsubscribeRxStore(final Object store) {
        String tag = store.getClass().getSimpleName();
        Subscription subscription = rxStoreMap.get(tag);
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            rxStoreMap.remove(tag);
        }
    }

    public <T extends RxViewListener> void subscribeRxView(final T view) {
        final String tag = view.getClass().getSimpleName();
        Subscription subscription = rxViewMap.get(tag);
        if (subscription == null || subscription.isUnsubscribed()) {
            rxStoreMap.put(tag, bus.get()
                    .onBackpressureBuffer()
                    .filter(new Func1<Object, Boolean>() {
                @Override
                public Boolean call(Object o) {
                    return o instanceof RxStoreChange;
                }
            }).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object o) {
                            view.onRxStoreChanged((RxStoreChange) o);
                        }
                    }));
        }
    }

    public <T extends RxViewListener> void unsubscribeRxView(final T object) {
        String tag = object.getClass().getSimpleName();
        Subscription subscription = rxStoreMap.get(tag);
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            rxStoreMap.remove(tag);
        }
    }

    public synchronized void unsubscribeAll() {
        for (Subscription subscription : rxStoreMap.values()) {
            subscription.unsubscribe();
        }

        for (Subscription subscription : rxViewMap.values()) {
            subscription.unsubscribe();
        }

        rxStoreMap.clear();
        rxViewMap.clear();
    }

    public void postRxAction(RxAction rxAction) {
        if (null == rxAction) {
            throw new IllegalArgumentException("RxAction must not be empty.");
        }

        bus.post(rxAction);
    }

    public void postRxStoreChange(final RxStoreChange storeChange) {
        bus.post(storeChange);
    }
}


