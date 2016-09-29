package com.lean.rxflux.action;

import com.lean.rxflux.dispatcher.RxDispatcher;

/**
 * Action creator,事件统一入口，统一处理 View event\Notify event\Network event等。
 *
 * @author lean
 */
public abstract class RxActionsCreator {

    private final RxDispatcher dispatcher;

    public RxActionsCreator() {
        dispatcher = RxDispatcher.singleton();
    }

    /** 获取事件分发器 */
    public RxDispatcher getDispatcher() {
        return dispatcher;
    }

    /** 事件分发 */
    public void postAction(RxAction action) {
        getDispatcher().postRxAction(action);
    }
}
