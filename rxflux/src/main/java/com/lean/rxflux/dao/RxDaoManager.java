package com.lean.rxflux.dao;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.DefaultDatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.NonNull;

import com.lean.rxflux.dao.exception.DaoException;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * RxDaoManager，负责各RxDAO对象的管理、建表和表更新操作。
 * 同时保证所有的RxDao共享同一个BriteDatabase对象，且内部RxDao不重复。
 *
 * @author Lean
 */
public class RxDaoManager {

    private Map<Class<?>, AbstractRxDao> daos;
    private final String name;
    private final int version;
    private BriteDatabase db;
    private TablesCreatedListener createdListener;
    private TablesUpgradedListener upgradedListener;

    private RxDaoManager(Builder builder) {

        if (builder.name == null) {
            throw new IllegalArgumentException("未指明数据库文件名");
        }

        if (builder.version == -1) {
            throw new IllegalArgumentException("未指明数据库版本号");
        }

        if (builder.daos.isEmpty()) {
            throw new IllegalArgumentException("RxDao为空");
        }

        this.name = builder.name;
        this.version = builder.version;
        this.createdListener = builder.createdListener;
        this.upgradedListener = builder.upgradedListener;
        this.daos = builder.daos;

        OpenHelper openHelper;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            openHelper = new OpenHelper(builder.context, name, builder.cursorFactory, version,
                    builder.errorHandler, builder.foreignKeyConstraints);
        } else {
            openHelper = new OpenHelperApi16(builder.context, name, builder.cursorFactory, version,
                    builder.errorHandler, builder.foreignKeyConstraints);
        }

        SqlBrite brite;
        if (builder.logger != null) {
            brite = SqlBrite.create(builder.logger);
        } else {
            brite = SqlBrite.create();
        }

        db = brite.wrapDatabaseHelper(openHelper,
                builder.scheduler == null ? Schedulers.io() : builder.scheduler);
        db.setLoggingEnabled(builder.logging);

        for (AbstractRxDao dao : this.daos.values()) {
            dao.setSqlBriteDb(db);
        }
    }


    /**
     * 获取BriteDatabase数据库对象
     *
     * @return
     */
    public BriteDatabase getDatabase() {
        return db;
    }

    /**
     * 获取数据库版本号
     *
     * @return
     */
    public int getVersion() {
        return version;
    }


    /**
     * 通过实体查询对应的RxDAO
     *
     * @param mo
     * @return
     */
    public AbstractRxDao getRxDao(Class<?> mo) {
        return daos.get(mo);
    }

    /**
     * 获取数据库名
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 删除数据库
     */
    public void delete(Context c) {
        c.deleteDatabase(getName());
    }

    /**
     * 关闭数据库连接
     */
    public void close() throws IOException {
        db.close();
    }

    /**
     * SqlOpenHelper, 负责建表和表更新
     */
    private class OpenHelper extends SQLiteOpenHelper {

        protected boolean foreignKeyConstraints;

        public OpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version, DatabaseErrorHandler errorHandler, boolean foreignKeyConstraints) {
            super(context, name, factory, version, errorHandler);
            this.foreignKeyConstraints = foreignKeyConstraints;
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            if (Build.VERSION.SDK_INT < 16) {
                if (foreignKeyConstraints) {
                    db.execSQL("PRAGMA foreign_keys=ON;");
                }
            }
        }

        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            for (AbstractRxDao d : daos.values()) {
                d.createTable(db);
            }

            if (createdListener != null) {
                createdListener.onTablesCreated(db);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            for (AbstractRxDao d : daos.values()) {
                d.onUpgrade(db, oldVersion, newVersion);
            }

            if (upgradedListener != null) {
                upgradedListener.onTablesUpgraded(db, oldVersion, newVersion);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private class OpenHelperApi16 extends OpenHelper {
        public OpenHelperApi16(Context context, String name, SQLiteDatabase.CursorFactory factory,
                               int version, DatabaseErrorHandler errorHandler, boolean foreignKeyConstraints) {
            super(context, name, factory, version, errorHandler, foreignKeyConstraints);
        }

        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);
            if (foreignKeyConstraints) {
                db.setForeignKeyConstraintsEnabled(true);
            }
        }
    }


    /**
     * RxDaoManager Builder，辅助RxDaoManager实例化
     *
     * @param context
     * @return
     */
    public static Builder with(Context context) {
        return new Builder(context);
    }

    public static class Builder {
        //        private Set<AbstractRxDao> daos = new HashSet<>();
        private Map<Class<?>, AbstractRxDao> daos = new HashMap<>();
        private String name;
        private int version = -1;
        private final Context context;
        private SQLiteDatabase.CursorFactory cursorFactory = null;
        private DatabaseErrorHandler errorHandler = new DefaultDatabaseErrorHandler();
        private TablesCreatedListener createdListener = null;
        private TablesUpgradedListener upgradedListener = null;
        private boolean logging = false;
        private SqlBrite.Logger logger = null;
        private Scheduler scheduler = null;
        private boolean foreignKeyConstraints = false;

        private Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        public Builder databaseName(@NonNull String name) {
            if (name == null || name.length() == 0) {
                throw new NullPointerException("name == null");
            }
            this.name = name;
            return this;
        }

        public Builder version(int version) {
            this.version = version;
            return this;
        }


        /**
         * 注册建表监听
         */
        public Builder onTablesCreated(@NonNull TablesCreatedListener createdListener) {
            if (createdListener == null) {
                throw new NullPointerException("tablesCreatedListener == null");
            }

            this.createdListener = createdListener;
            return this;
        }

        /**
         * 注册表更新监听
         */
        public Builder onTablesUpgraded(@NonNull TablesUpgradedListener tablesUpgradedListener) {
            if (tablesUpgradedListener == null) {
                throw new NullPointerException("tablesUpgradedListener == null");
            }
            this.upgradedListener = tablesUpgradedListener;
            return this;
        }

        /**
         * 添加RxDao
         * @param c 实体类类型
         * @param dao RxDao<T> 或者是 AbstractRxDao
         * @return
         */
        public Builder add(@NonNull Class<?> c, @NonNull AbstractRxDao dao) {
            if (dao == null) {
                throw new NullPointerException("dao == null");
            }
            if (null != this.daos.get(c)) {
                throw new DaoException("不允许重复添加Dao");
            }
            this.daos.put(c, dao);
            return this;
        }

        /**
         * 添加RxDao
         * @param dao RxDao<T>
         * @return
         */
        public Builder add(@NonNull RxDao dao) {
            if (dao == null) {
                throw new NullPointerException("dao == null");
            }
            Class<?> c = dao.getEntityType();
            if (null == c) {
                throw new IllegalArgumentException("RxDao初始化未指定实体类型");
            }
            if (null != this.daos.get(c)) {
                throw new DaoException("不允许重复添加Dao");
            }
            this.daos.put(c, dao);
            return this;
        }

        /**
         * 启停日志
         */
        public Builder logging(boolean logging) {
            this.logging = logging;
            return this;
        }

        /**
         * 设置日志组件
         */
        public Builder logger(@NonNull SqlBrite.Logger logger) {
            if (logger == null) {
                throw new NullPointerException("Logger == null");
            }
            this.logger = logger;
            logging(true);
            return this;
        }

        /**
         * 设置sqlbrite的线程调度器，默认为{@link Schedulers#io()}
         *
         * @param scheduler
         * @return
         */
        public Builder scheduler(Scheduler scheduler) {
            if (null != scheduler) {
                this.scheduler = scheduler;
            }
            return this;
        }

        /**
         * RxDaoManager实例构建
         */
        public RxDaoManager build() {
            return new RxDaoManager(this);
        }
    }

    /**
     * 建表监听
     */
    public interface TablesCreatedListener {
        void onTablesCreated(SQLiteDatabase db);
    }

    /**
     * 表更新监听
     */
    public interface TablesUpgradedListener {
        void onTablesUpgraded(SQLiteDatabase db, int oldVersion, int newVersion);
    }
}