package com.lean.rxflux.action;

/**
 * RxAction, 事件模板
 *
 * @author lean
 */
public class RxAction<T> {
    private final String type;
    private final T data;

    public RxAction(String type, T data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public T getData() {
        return data;
    }
}
