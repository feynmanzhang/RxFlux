package com.lean.rxflux.dao;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.sqlbrite.BriteDatabase;

import rx.Observable;
import rx.functions.Func0;

import static com.squareup.sqlbrite.BriteDatabase.Transaction;

/**
 * RxDao的抽象父类,基于sqlbrite。
 * 应用层可以直接继承实现AbstractRxDao来实现自定义RxDao，具体可以参见测试用例写法。
 *
 * @author lean
 * @see <a href = "https://github.com/square/sqlbrite">sqlbrite</a>
 * @see RxDao
 */
abstract class AbstractRxDao {

    protected BriteDatabase db;

    /**
     * 在子类中实现实体建表操作
     *
     * @param db
     */
    public abstract void createTable(SQLiteDatabase db);

    /**
     * 在子类中实现表更新操作操作
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);


    void setSqlBriteDb(BriteDatabase db) {
        this.db = db;
    }

    /**
     * 创建事务
     *
     * @return
     */
    public Transaction newTransaction() {
        return db.newTransaction();
    }

    /**
     * @param table
     * @param contentValues
     * @return
     */
    @CheckResult
    protected Observable<Long> insert(final String table, final ContentValues contentValues) {
        return Observable.defer(new Func0<Observable<Long>>() {
            @Override
            public Observable<Long> call() {
                return Observable.just(db.insert(table, contentValues));
            }
        });
    }

    /**
     * 增加封装sqlbrite的insert()方法，返回一个deferred observable。
     *
     * @param table
     * @param contentValues
     * @param conflictAlgorithm 冲突算法
     * @return 插入的新行ID
     */
    @CheckResult
    protected Observable<Long> insert(final String table,
                                      final ContentValues contentValues,
                                      final int conflictAlgorithm) {
        return Observable.defer(new Func0<Observable<Long>>() {
            @Override
            public Observable<Long> call() {
                return Observable.just(db.insert(table, contentValues, conflictAlgorithm));
            }
        });
    }

    /**
     * 更新操作。
     * 增加封装sqlbrite的update()方法，返回一个deferred observable。
     * @param table
     * @param values
     * @param whereClause where条件
     * @param whereArgs  wehre条件参数
     * @return
     */
    @CheckResult
    protected Observable<Integer> update(@NonNull final String table,
                                         @NonNull final ContentValues values,
                                         @Nullable final String whereClause,
                                         @Nullable final String... whereArgs) {
        return Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                return Observable.just(db.update(table, values, whereClause, whereArgs));
            }
        });
    }

    /**
     * 更新操作。
     * 封装sqlbrite的update()方法，返回一个deferred observable。
     *
     * @param table
     * @param values
     * @param conflictAlgorithm 冲突算法
     * @param whereClause where条件
     * @param whereArgs  wehre条件参数
     * @return
     */
    @CheckResult
    protected Observable<Integer> update(@NonNull final String table,
                                         @NonNull final ContentValues values,
                                         final int conflictAlgorithm,
                                         @Nullable final String whereClause,
                                         @Nullable final String... whereArgs) {
        return Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                return Observable.just(db.update(table, values, conflictAlgorithm, whereClause, whereArgs));
            }
        });
    }

    /**
     * 全表删除
     * @param table
     * @return
     */
    @CheckResult
    protected Observable<Integer> deleteAll(@NonNull final String table) {
        return delete1(table, null);
    }

    /**
     * 删除表数据操作
     * 封装sqlbrite的delete()方法，返回一个deferred observable。
     *
     * @param table
     * @param whereClause
     * @param whereArgs
     * @return
     */
    @CheckResult
    protected Observable<Integer> delete1(@NonNull final String table,
                                          @Nullable final String whereClause,
                                          @Nullable final String... whereArgs) {
        return Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                return Observable.just(db.delete(table, whereClause, whereArgs));
            }
        });
    }

    /**
     * sql执行。
     *
     * 这个方法使用于建表、更改表结构的DDL操作，不适用与SELECT、UPDATE语句等DML操作。
     *
     * @param sql
     */
    protected void execSQL(@NonNull String sql) {
        db.execute(sql);
    }
}