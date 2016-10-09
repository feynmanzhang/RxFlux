package com.lean.rxflux.dao;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.lean.rxflux.dao.entity.SaveMode;
import com.lean.rxflux.dao.exception.DaoException;
import com.lean.rxflux.dao.util.ReflectUtil;
import com.squareup.sqlbrite.BriteDatabase;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 封装常用的Dao操作
 *
 * @author Lean
 */
public class RxDaoHelper {

    private RxDaoHelper() {
    }

    /**
     * 静默式自动存储，即不返回响应。
     * <p/>
     * 自动存储运行于独立异步io()线程上。
     *
     * @param rxDaoManager
     * @param observable
     * @param mode 保存模式,若为OVERWRITE则在保存数据前删除表数据（其中，删除表数据由参数whereClause和whereArgs过滤）。
     * @param whereClause where子句。过滤SaveMode为OVERWRITE时删除的数据。
     * @param whereArgs where字句参数
     */
    public static <T> void autoSavePeaceful(@NonNull final RxDaoManager rxDaoManager,
                                            @NonNull final Observable<T> observable,
                                            @NonNull final SaveMode mode,
                                            @Nullable final String whereClause,
                                            @Nullable final String... whereArgs) {
        autoSave(rxDaoManager, observable, true, mode, whereClause, whereArgs).subscribe(new Subscriber<T>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable throwable) {
                throw new DaoException("自动保存异常", throwable);
            }

            @Override
            public void onNext(T t) {
                // do nothing
            }
        });
    }

    /**
     * 自动存储网络请求返回的实体对象，即用于处理retrofit2返回值的自动存储。
     *
     * @param rxDaoManager RxDaoManager
     * @param observable   网络请求返回的实体对象
     * @param bAsyn        是否异步.若为true,则是独立io线程;若为false,则和Observable保持同线程。
     *                     例如在RestClient场景下需要设置为ture，以防止数据返回顺序异常。
     * @param mode 保存模式,若为OVERWRITE则在保存数据前删除表数据（其中，删除表数据由参数whereClause和whereArgs过滤）。
     * @param whereClause where子句。过滤SaveMode为OVERWRITE时删除的数据。
     * @param whereArgs where字句参数
     * @return
     */
    public static <T> Observable<T> autoSave(@NonNull final RxDaoManager rxDaoManager,
                                             @NonNull final Observable<T> observable,
                                             @NonNull final boolean bAsyn,
                                             @NonNull final SaveMode mode,
                                             @Nullable final String whereClause,
                                             @Nullable final String... whereArgs) {
        return observable.subscribeOn(Schedulers.io())
                .observeOn(bAsyn ? Schedulers.io() : Schedulers.immediate())
                .map(new Func1<T, T>() {
                    @Override
                    public T call(T obj) {
                        if (null == obj) {
                            throw new IllegalArgumentException("实体对象为空，不处理对象存储");
                        }
                        BriteDatabase db = rxDaoManager.getDatabase();
                        if (null == db) {
                            throw new DaoException("RxDaoManager获取BriteDataBase异常，可能是RxDaoManager未初始化导致");
                        }

                        long t1 = System.currentTimeMillis();

                        BriteDatabase.Transaction transaction = db.newTransaction();
                        try {
                            doAutoSave(rxDaoManager, obj, mode, whereClause, whereArgs);
                            transaction.markSuccessful();
                        } finally {
                            transaction.end();
                        }

                        System.out.println(System.currentTimeMillis() - t1);
                        Log.i("存储更新使用時間: ", "" + (System.currentTimeMillis() - t1));

                        return obj;
                    }
                });

    }

    /**
     * 自动存储实体对象。
     * <p/>
     * 对于Collection类型且无@Column注解的字段会Collection字段对进行一次迭代处理。
     *
     * @param rxDaoManager RxDaoManager
     * @param t            实体对象
     * @param mode 保存模式,若为OVERWRITE则在保存数据前删除表数据（其中，删除表数据由参数whereClause和whereArgs过滤）。
     * @param whereClause where子句。过滤SaveMode为OVERWRITE时删除的数据。
     * @param whereArgs where字句参数
     */
    private static <T> void doAutoSave(@NonNull final RxDaoManager rxDaoManager,
                                       @NonNull T t,
                                       @NonNull SaveMode mode,
                                       @Nullable final String whereClause,
                                       @Nullable final String... whereArgs) {

        if (ReflectUtil.isModelObject(t)) { // 1. 可直接存储的实体
            doSave(rxDaoManager, t, mode, whereClause, whereArgs);
        } else if (Collection.class.isAssignableFrom(t.getClass())) { // 2. 可存储实体的列表集合
            // 若SaveMode为OVERWRITE,则需要执行一次清表操作。
            Iterator it = ((Collection) t).iterator();
            int i = 0;
            while (it.hasNext()) {
                if (0 == i) {
                    Object obj = it.next();
                    if (ReflectUtil.isInternalClass(t.getClass()) && !ReflectUtil.isModelObject(obj)) {
                        break;
                    }
                    doSave(rxDaoManager, obj, mode, whereClause, whereArgs);
                } else {
                    doSave(rxDaoManager, it.next(), SaveMode.APPEND, whereClause, whereArgs);
                }
                i++;
            }
        } else { // 3. 其他数据格式的对象，内部含有可存储实体
            Class<?> c = t.getClass();
            Field[] fields = c.getDeclaredFields();
            for (Field f : fields) {
                if (ReflectUtil.isInternalClass(c)) {
                    continue;
                }
                Object object = ReflectUtil.getFiledValue(t, f);
                if (Collection.class.isAssignableFrom(f.getType())) {
                    // 若SaveMode为OVERWRITE,则需要执行一次清表操作。
                    Iterator it = ((Collection) object).iterator();
                    int i = 0;
                    while (it.hasNext()) {
                        if (0 == i) {
                            Object obj = it.next();
                            if (ReflectUtil.isInternalClass(c) && !ReflectUtil.isModelObject(obj)) {
                                break;
                            }
                            doSave(rxDaoManager, obj, mode, whereClause, whereArgs);
                        } else {
                            doSave(rxDaoManager, it.next(), SaveMode.APPEND, whereClause, whereArgs);
                        }
                        i++;
                    }
                } else if(c.isArray()) { // 不处理多维数组
                    int i = 0;
                    for (Object obj : (Object[]) object) {
                        if ( 0 == i) {
                            if (ReflectUtil.isInternalClass(c) && !ReflectUtil.isModelObject(obj)) {
                                break;
                            }
                            doSave(rxDaoManager, obj, mode, whereClause, whereArgs);
                        } else {
                            doSave(rxDaoManager, obj, SaveMode.APPEND, whereClause, whereArgs);
                        }
                        i++;
                    }
                } else  {
                    if (ReflectUtil.isModelObject(object)) {
                        doSave(rxDaoManager, object, mode, whereClause, whereArgs);
                    }
                }
            }
        }
    }

    /**
     * 存储实体对象，对于Collection类型且无@Column注解的字段会进行迭代处理。
     *
     * @param rxDaoManager RxDaoManager
     * @param t            实体对象
     * @param mode 保存模式,若为OVERWRITE则在保存数据前删除表数据（其中，删除表数据由参数whereClause和whereArgs过滤）。
     * @param whereClause where子句。过滤SaveMode为OVERWRITE时删除的数据。
     * @param whereArgs where字句参数
     */
    private static <T> void doSave(@NonNull final RxDaoManager rxDaoManager,
                                   @NonNull T t,
                                   @NonNull SaveMode mode,
                                   @Nullable final String whereClause,
                                   @Nullable final String... whereArgs) {

        AbstractRxDao abstractRxDao = rxDaoManager.getRxDao(t.getClass());
        if (null == abstractRxDao) {
            throw new DaoException("未发现实体类型\"" + t.getClass().toString() + "\"对应的RxDao");
        } else {
            RxDao rxDao;
            try {
                rxDao = (RxDao) abstractRxDao;
            } catch (ClassCastException e) {
                throw new DaoException("非BaseRxDao类型的AbstractRxDao不适用RxDaoUtil的辅助方法.", e);
            }
            String tableName = rxDao.getTableName();
            try {
                if (SaveMode.OVERWRITE == mode) {
                    // clean table
                    rxDaoManager.getDatabase().delete(tableName, whereClause, whereArgs);
                }

                rxDaoManager.getDatabase().insert(tableName, rxDao.marshal(t), SQLiteDatabase.CONFLICT_REPLACE);
            } catch (Exception e) {
                throw new DaoException("构建ContentValues对象时字段映射错误", e);
            }
        }
    }


}
