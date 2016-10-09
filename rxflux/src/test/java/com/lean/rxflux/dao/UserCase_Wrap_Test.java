package com.lean.rxflux.dao;

import android.content.Context;

import com.lean.rxflux.dao.entity.OrmUser;
import com.lean.rxflux.dao.entity.User;
import com.squareup.sqlbrite.BriteDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;


import java.util.List;

import rx.Subscriber;
import rx.exceptions.OnErrorNotImplementedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 使用RxDao的情景测试用例
 *
 * @author Lean
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class UserCase_Wrap_Test {

    private RxDaoManager manager;

    @Before
    public void setUp() throws Exception {
        Context c = RuntimeEnvironment.application;
        manager = RxDaoManager.with(c)
                .databaseName("test.db")
                .version(1)
                .add(new RxDao<>(OrmUser.class))
                .build();
    }

    @After
    public void tearDown() throws Exception {
        manager.close();
    }


    @Test
    public void testCURD() throws Exception {
        assertTrue(manager != null);
        RxDao<OrmUser> userDao = (RxDao<OrmUser>) manager.getRxDao(OrmUser.class);

        {
            /** query all test */
            List<OrmUser> list = userDao.queryAll();
            assertEquals(list.size(), 0);
        }
        {
            /** insert test */
            for (int i = 0; i < 10; i++) {
                final String name = "Name" + i;
                int age = i;
                double weight = i + 10 / 3;
                byte[] blob = Integer.toString(i).getBytes("UTF-8");

                userDao.insert(new OrmUser(i, name, age, weight, blob))
                        .subscribe(new Subscriber<Long>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                                fail("RxDao的insert()操作不符合预期");
                            }

                            @Override
                            public void onNext(Long aLong) {
                            }
                        });
            }
        }

        {
            /** query all test */
            List<OrmUser> list = userDao.queryAll();
            assertEquals(list.size(), 10);
        }

        {
            /** query test */
            for (int i = 0; i < 10; i++) {
                final String name = "Name" + i;
                List<OrmUser> list = userDao.query("id = ?", i + "");
                assertTrue(list.size() == 1);
                assertTrue(list.get(0).getName().equalsIgnoreCase(name));
                assertTrue(list.get(0).getId() == i);
            }
        }

        final int uId = 11;
        final String uName = "name11";
        final int uAge = 11;
        final double uWeight = 7;
        OrmUser uUser = new OrmUser(uId, uName, uAge, uWeight, Integer.toString(uId).getBytes("UTF-8"));
        {
            /** update test */
            userDao.update(uUser, "id = ?", "11")
                    .subscribe(new Subscriber<Integer>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            fail("RxDao的update()操作不符合预期");
                        }

                        @Override
                        public void onNext(Integer integer) {
                            assertTrue(0 == integer.intValue());
                        }
                    });

            userDao.update(uUser, "id = ?", "1")
                    .subscribe(new Subscriber<Integer>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            fail("RxDao的update()操作不符合预期");
                        }

                        @Override
                        public void onNext(Integer integer) {
                            assertTrue(1 == integer.intValue());
                        }
                    });
        }


        {
            /** query one test */
            OrmUser qUser = userDao.queryOne("id = ?", uUser.getId() + "");
            assertNotNull(qUser);
            assertTrue(qUser.getName().equalsIgnoreCase(uName));
        }

        {
            /** delete test */
            userDao.delete("id = ?", "2")
                    .subscribe(new Subscriber<Integer>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            fail("RxDao的delete()操作不符合预期");
                        }

                        @Override
                        public void onNext(Integer integer) {
                            assertTrue(1 == integer.intValue());
                        }
                    });

            userDao.deleteAll()
                    .subscribe(new Subscriber<Integer>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            fail("RxDao的deleteAll()操作不符合预期");
                        }

                        @Override
                        public void onNext(Integer integer) {
                            assertTrue(9 == integer.intValue());
                        }
                    });
        }
    }


    @Test
    public void testSetSqlBriteDb() throws Exception {
        assertTrue(manager != null);
        RxDao<OrmUser> userDao = (RxDao<OrmUser>) manager.getRxDao(OrmUser.class);
        assertNotNull(userDao.db);
        assertTrue(userDao.db instanceof BriteDatabase);
    }


    @Test(expected = OnErrorNotImplementedException.class)
    public void testTranscation() throws Exception {
        assertTrue(manager != null);

        RxDao<OrmUser> userDao = (RxDao<OrmUser>) manager.getRxDao(OrmUser.class);

        for (int i = 0; i < 2; i++) {
            final String name = "Name" + i;
            int age = i;
            double weight = i + 10 / 3;
            byte[] blob = Integer.toString(i).getBytes("UTF-8");

            userDao.insert(new OrmUser(i, name, age, weight, blob))
                    .subscribe(new Subscriber<Long>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            fail("RxDao的insert()操作不符合预期");
                        }

                        @Override
                        public void onNext(Long aLong) {
                        }
                    });
        }

        /** query all test */
        List<OrmUser> list = userDao.queryAll();
        assertEquals(list.size(), 2);


        BriteDatabase.Transaction transaction = manager.getDatabase().newTransaction();
        try {
                {
                    /** insert test */
                    for (int i = 0; i < 2; i++) {
                        final String name = "Name" + i;
                        int age = i;
                        double weight = i + 10 / 3;
                        byte[] blob = Integer.toString(i).getBytes("UTF-8");

                        userDao.insert(new OrmUser(i, name, age, weight, blob))
                                .subscribe(new Subscriber<Long>() {
                                    @Override
                                    public void onCompleted() {
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        fail("RxDao的insert()操作不符合预期");
                                    }

                                    @Override
                                    public void onNext(Long aLong) {
                                    }
                                });


                        RxDao<User> badDao = new RxDao<>(User.class);
                        badDao.insert(new User()).subscribe();  // roll back
                    }
                }
            transaction.markSuccessful();
        } finally {
            transaction.end();

            {
                /** query all test */
                List<OrmUser> list2 = userDao.queryAll();
                assertEquals(list2.size(), 2);
            }
        }

    }
}