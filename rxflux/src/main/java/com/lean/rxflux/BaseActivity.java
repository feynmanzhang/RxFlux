package com.lean.rxflux;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.CallSuper;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author lean
 */
public class BaseActivity<T1 extends ActionsCreator, T2 extends Store> extends Activity {

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
        actionsCreator.getDispatcher().register(store);
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

        // unregister
        actionsCreator.getDispatcher().unregister(store);
    }


    @Override
    @CallSuper
    protected void onDestroy() {
        super.onDestroy();

    }

    public T1 getActionsCreator() {
        return actionsCreator;
    }

    public T2 getStore() {
        return store;
    }
}
