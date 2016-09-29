package com.lean.rxflux.store;


/**
 * ReStore状态改变事件定义
 */
public class RxStoreChange {

    String storeEvent;

    public RxStoreChange() {
    }

    public RxStoreChange(String storeEvent) {
        this.storeEvent = storeEvent;
    }

    public String getRxActionType() {
        return storeEvent;
    }

}
