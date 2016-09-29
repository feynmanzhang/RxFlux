package com.lean.rxflux.store;

import com.lean.rxflux.dispatcher.RxViewListener;
import com.lean.rxflux.action.RxAction;
import com.lean.rxflux.dispatcher.RxDispatcher;

/**
 * Store，不同于MVC的Model，
 *
 * @author lean
 */
public abstract class RxStore{

    private final RxDispatcher dispatcher;

    public RxStore() {
        dispatcher = RxDispatcher.singleton();
    }

    /** 绑定视图 */
    public void register(final RxViewListener view) {
        dispatcher.subscribeRxView(view);
    }

    /** 解绑视图 */
    public void unregister(final RxViewListener view) {
        dispatcher.unsubscribeRxView(view);
    }

    /**
     * 根据Action实现各个业务逻辑。
     * 抽象方法，在子类中重写。
     */
    public abstract void onAction(RxAction action);

    /** store状态改变时，调用此方法通知视图刷新*/
    public void emitStoreChange(RxStoreChange change) {
        dispatcher.postRxStoreChange(change);
    }
}
