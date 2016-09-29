package com.lean.rxflux.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.CallSuper;


import com.lean.rxflux.store.RxStore;
import com.lean.rxflux.dispatcher.RxViewListener;
import com.lean.rxflux.action.RxActionsCreator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Activity的抽象基类
 *
 * @author lean
 */
public abstract class RxBaseActivity<T1 extends RxActionsCreator, T2 extends RxStore> extends Activity implements RxViewListener {

    private T1 actionsCreator;
    private T2 store;

    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Type type = getClass().getGenericSuperclass();
        ParameterizedType paramType = (ParameterizedType) type;
        try {
            actionsCreator = ((Class<T1>) paramType.getActualTypeArguments()[0]).newInstance();
            store = ((Class<T2>) paramType.getActualTypeArguments()[1]).newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("The instance for ActionsCreator(or Store)  cannot be created");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("The default constructor of ActionsCreator(or Store) is not visible");
        }

        // register
        actionsCreator.getDispatcher().subscribeRxStore(store);
    }

    @Override
    @CallSuper
    protected void onResume() {
        super.onResume();
        store.register(this);
    }

    @Override
    @CallSuper
    protected void onPause() {
        super.onPause();
        store.unregister(this);
    }


    @Override
    @CallSuper
    protected void onDestroy() {
        super.onDestroy();

        //unregister
        actionsCreator.getDispatcher().unsubscribeRxStore(store);
    }

    public T1 getActionsCreator() {
        return actionsCreator;
    }

    public T2 getStore() {
        return store;
    }
}
