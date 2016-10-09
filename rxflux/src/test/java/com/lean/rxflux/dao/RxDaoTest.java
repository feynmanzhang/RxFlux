package com.lean.rxflux.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lean.rxflux.dao.entity.OrmUser;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.QueryObservable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import rx.Subscriber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Lean
 */
@PrepareForTest({BriteDatabase.class, Cursor.class, QueryObservable.class})
@RunWith(PowerMockRunner.class)
public class RxDaoTest {

    BriteDatabase db;

    @Before
    public void setUp() throws Exception {
        db = PowerMockito.mock(BriteDatabase.class);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetTableName() throws Exception {
        RxDao<OrmUser> rxDao = new RxDao<OrmUser>(OrmUser.class);
        rxDao.setSqlBriteDb(db);
        assertEquals("ormuser", rxDao.getTableName());
    }

    @Test
    public void testMap() throws Exception {
        
    }

    @Test
    public void testMarshal() throws Exception {

    }

    @Test
    public void testInsert() throws Exception {
        RxDao spy = PowerMockito.spy(new RxDao<OrmUser>(OrmUser.class));
        spy.setSqlBriteDb(db);

        OrmUser ormUser = new OrmUser();
        ContentValues cv = PowerMockito.mock(ContentValues.class);
        PowerMockito.doReturn(cv).when(spy).marshal(ormUser);

        PowerMockito.when(db.insert("ormuser", cv, SQLiteDatabase.CONFLICT_REPLACE)).thenReturn(1l);
        spy.insert(ormUser)
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        fail("Unexcepted insert.");
                    }

                    @Override
                    public void onNext(Long aLong) {
                        assertEquals(1l, aLong.longValue());
                    }
                });
    }

    @Test
    public void testDelete() throws Exception {
        RxDao rxDao = new RxDao<OrmUser>(OrmUser.class);
        rxDao.setSqlBriteDb(db);
        PowerMockito.when(db.delete("ormuser", "", "")).thenReturn(1);
        rxDao.delete("", "")
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        fail("Unexcepted delete.");
                    }

                    @Override
                    public void onNext(Integer integer) {
                        assertEquals(1, integer.intValue());
                    }
                });

        PowerMockito.when(db.delete("ormuser", "age = ?", "30")).thenReturn(2);
        rxDao.delete("age = ?", "30")
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        fail("Unexcepted delete.");
                    }

                    @Override
                    public void onNext(Integer integer) {
                        assertEquals(2, integer.intValue());
                    }
                });
    }

    @Test
    public void testUpdate() throws Exception {
        RxDao spy = PowerMockito.spy(new RxDao<OrmUser>(OrmUser.class));
        spy.setSqlBriteDb(db);

        OrmUser ormUser = new OrmUser();
        ContentValues cv = PowerMockito.mock(ContentValues.class);
        PowerMockito.doReturn(cv).when(spy).marshal(ormUser);

        PowerMockito.when(db.update("ormuser", cv, SQLiteDatabase.CONFLICT_REPLACE, "", "")).thenReturn(1);
        spy.update(ormUser, "", "")
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        fail("Unexcepted udpate.");
                    }

                    @Override
                    public void onNext(Integer integer) {
                        assertEquals(1, integer.intValue());
                    }
                });

        PowerMockito.when(db.update("ormuser", cv, SQLiteDatabase.CONFLICT_REPLACE, "age = ?", "20")).thenReturn(3);
        spy.update(ormUser, "age = ?", "20")
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        fail("Unexcepted udpate.");
                    }

                    @Override
                    public void onNext(Integer integer) {
                        assertEquals(3, integer.intValue());
                    }
                });
    }
}