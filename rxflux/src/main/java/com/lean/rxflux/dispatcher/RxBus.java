package com.lean.rxflux.dispatcher;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * 利用rxjava实现eventbus，不支持Sticky事件
 *
 * @author Lean
 */
public class RxBus {

    private static volatile RxBus singleton;

    private final Subject<Object, Object> bus;

    private RxBus() {
        bus = new SerializedSubject<>(PublishSubject.create());
    }

    public static RxBus getSingleton() {
        if (singleton == null) {
            synchronized (RxBus.class) {
                if (singleton == null) {
                    singleton = new RxBus();
                }
            }
        }
        return singleton;
    }

    public Observable<Object> get() {
        return bus;
    }

    /** 事件发送 */
    public void post (Object o) {
        bus.onNext(o);
    }

    /** 根据eventType类型返回特定类型(eventType)的被观察者 */
    public <T> Observable<T> toObservable (Class<T> eventType) {
        return bus.ofType(eventType);
    }

}
