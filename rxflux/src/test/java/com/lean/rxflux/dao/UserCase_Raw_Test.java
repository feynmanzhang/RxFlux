package com.lean.rxflux.dao;

import android.content.Context;

import com.lean.rxflux.dao.entity.User;
import com.squareup.sqlbrite.BriteDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

/**
 * 使用AbstrctRxDao的情景测试用例
 *
 * @author Lean
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class UserCase_Raw_Test {

    private RxDaoManager manager;
    private UserDao userDao;

    @Before
    public void setUp() throws Exception {
        Context c = RuntimeEnvironment.application;
        userDao = new UserDao();
        manager = RxDaoManager.with(c).databaseName("test.db").version(1).add(User.class, userDao).build();
    }

    @After
    public void tearDown() throws Exception {
        manager.close();
    }


    @Test
    public void testAbstractRxDaoCURD() throws Exception {
        for (int i = 0; i < 10; i++) {
            String name = "Name" + i;
            int age = i;
            double weight = i + 10 / 3;
            byte[] blob = Integer.toString(i).getBytes("UTF-8");

            User u = userDao.insert(name, age, weight, blob);
            assertEquals(name, u.name);
            assertEquals(age, u.age);
            assertEquals(weight, u.weight, 0.1);
            assertEquals(blob, u.blob);

            User qUser = userDao.getById(u.id);

            assertEquals(qUser.age, u.age);
            assertArrayEquals(qUser.blob, u.blob);
            assertEquals(qUser.id, u.id);
            assertEquals(qUser.name, u.name);
            assertEquals(qUser.weight, u.weight, 0.1);
        }
    }


    @Test
    public void testSetSqlBriteDb() throws Exception {
        assertNotNull(userDao.db);
        assertTrue(userDao.db instanceof BriteDatabase);
    }
}