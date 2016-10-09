package com.lean.rxflux.dao;

import android.content.Context;

import com.lean.rxflux.dao.entity.User;
import com.lean.rxflux.dao.exception.DaoException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;


/**
 * RxDaoManager 測試
 * @author Lean
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RxDaoManagerTest {

    Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.application;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoDatabaseName() throws Exception {
        RxDaoManager.with(context).build();
        Assert.fail("Exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoVersion() {
        RxDaoManager.with(context).databaseName("foo").build();
        Assert.fail("Exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoDaosAdded() {
        RxDaoManager.with(context).databaseName("foo").version(1).build();
        Assert.fail("Exception expected");
    }

    @Test(expected = DaoException.class)
    public void testDuplicateDaoAdded() {
        RxDaoManager.with(context).databaseName("foo").version(1)
                .add(User.class, new UserDao())
                .add(User.class, new UserDao())
                .build();
    }

    @Test
    public void testBuild() {
        UserDao userDao = new UserDao();
        RxDaoManager rxDaoManager = RxDaoManager.with(context).databaseName("foo").version(1).add(User.class, userDao).build();
        Assert.assertEquals(rxDaoManager.getName(), "foo");
        Assert.assertEquals(rxDaoManager.getVersion(), 1);
        Assert.assertEquals(rxDaoManager.getRxDao(User.class), userDao);
        Assert.assertNotNull(rxDaoManager.getDatabase());
    }

}
