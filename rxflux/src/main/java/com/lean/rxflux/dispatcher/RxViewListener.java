package com.lean.rxflux.dispatcher;

import android.support.annotation.NonNull;

import com.lean.rxflux.store.RxStoreChange;

/**
 *
 * @author Lean
 */
public interface RxViewListener {

    void onRxStoreChanged(@NonNull RxStoreChange change);
}
