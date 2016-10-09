package com.lean.rxflux.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lean.rxflux.dao.annotation.Column;
import com.lean.rxflux.dao.exception.DaoException;
import com.lean.rxflux.dao.util.GsonUtil;
import com.lean.rxflux.dao.util.SqlUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;


import rx.Observable;
import rx.functions.Func1;

/**
 * 基础数据库访问接口。
 * 1. 在{@code AbstractRxDao}上再封装一层，以简化应用层调用；
 * 2. 反射方式实现对象映射，若数据访问单一且对性能有高要求，可以自定义继承{@code AbstractRxDao}；
 *
 *
 *
 * @author Lean
 */
public class RxDao<T> extends AbstractRxDao {

    /** 运行时泛型类型 */
    private Class<T> tClass;

    private RxDao() {
    }

    public RxDao(Class<T> c) {
        this.tClass = c;
    }

    public Class<T> getEntityType() {
        return tClass;
    }

    public String getTableName() {
        return tClass.getSimpleName().toLowerCase();
    }

    /**
     * 构建默认的建表过程,默认建表仅支持字段和主键，不支持其他如默认值、索引等约束,且默认表名字段名为小写。
     * <p/>
     * 若需要建表约束,则可通过继承{@code AbstractRxDao} 或重载createTable()方法来实现.
     *
     * @param db
     */
    @Override
    public void createTable(SQLiteDatabase db) throws DaoException {
        try {
            db.execSQL(SqlUtil.CREATE_TABLE(getTableName(), tClass));
        } catch (SQLException e) {
            throw new DaoException("建表异常", e);
        }
    }


    /**
     * 默认数据库更新：删除旧版本数据表，然后重建。
     * <p/>
     * 若需要自定义方式实现表更新,则可通过{@code AbstractRxDao} 或重载onUpgrade()方法来实现.
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            try {
                db.execSQL(SqlUtil.DROP_TABLE_IF_EXISTS(getTableName()));
            } catch (SQLException e) {
                throw new DaoException("删除表时发生异常", e);
            }
            createTable(db);
        }
    }

    /**
     * 全表查询
     *
     * @return
     */
    public List<T> queryAll() {
        return db.createQuery(getTableName(), "SELECT * FROM " + getTableName())
                .mapToList(new Func1<Cursor, T>() {
                    @Override
                    public T call(Cursor cursor) {
                        return map(cursor);
                    }
                })
                .toBlocking()
                .first();
    }

    /**
     * 普通查询
     *
     * @param whereClause where子句
     * @param whereArgs 参数
     * @return
     */
    public List<T> query(@NonNull final String whereClause, final String... whereArgs) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ")
                .append(getTableName())
                .append(" WHERE ")
                .append(whereClause);

        return db.createQuery(getTableName(), sb.toString(), whereArgs)
                .mapToList(new Func1<Cursor, T>() {
                    @Override
                    public T call(Cursor cursor) {
                        return map(cursor);
                    }
                })
                .toBlocking()
                .first();
    }

    /**
     * 单行查询
     *
     * @param whereClause where子句
     * @param whereArgs 参数
     * @return
     */
    public T queryOne(@NonNull final String whereClause, final String... whereArgs) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ")
                .append(getTableName())
                .append(" WHERE ")
                .append(whereClause);

        return db.createQuery(getTableName(), sb.toString(), whereArgs)
                .mapToOne(new Func1<Cursor, T>() {
                    @Override
                    public T call(Cursor cursor) {
                        return map(cursor);
                    }
                })
                .toBlocking()
                .first();
    }

    /**
     * 对象映射Object Mapping。
     * <p/>
     * java.util.Date类型在内部映射为long类型，即timestamp格式。
     *
     * @param
     * @return
     */
    public T map(Cursor cursor) throws DaoException {
        T obj;
        try {
            obj = tClass.newInstance();
            assign(obj, cursor);
        } catch (Exception e) {
            throw new DaoException("对象映射错误", e);
        }

        return obj;
    }

    /**
     * 通过Cursor返回数据赋值obj对象
     *
     * @param obj
     * @param cursor
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private void assign(T obj, Cursor cursor) throws IllegalAccessException, IllegalArgumentException {
        if (null == obj) {
            throw new IllegalArgumentException("赋值对象为空");
        }
        if (null == cursor) {
            throw new IllegalArgumentException("Cursor对象为空");
        }

        Field[] fields = tClass.getDeclaredFields();
        for (Field f : fields) {
            Class<?> type = f.getType();

            Annotation annotation = f.getAnnotation(Column.class);
            if (null == annotation) {
                continue;
            }
            String value = ((Column) annotation).value();
            String fname = (null == value || value.isEmpty()) ? f.getName().toLowerCase() : value;

            boolean access = f.isAccessible();
            if (!access) {
                f.setAccessible(true);
            }
            if (CharSequence.class.isAssignableFrom(type)) {
                f.set(obj, cursor.getString(cursor.getColumnIndexOrThrow(fname)));
            } else if (char.class == type || Character.class == type) {
                f.set(obj, cursor.getString(cursor.getColumnIndexOrThrow(fname)).charAt(0));
            } else if (byte.class == type || Byte.class == type) {
                f.set(obj, cursor.getString(cursor.getColumnIndexOrThrow(fname)).getBytes(Charset.forName("UTF-8"))[0]);
            } else if (int.class == type || Integer.class == type) {
                f.set(obj, cursor.getInt(cursor.getColumnIndexOrThrow(fname)));
            } else if (long.class == type || Long.class == type) {
                f.set(obj, cursor.getLong(cursor.getColumnIndexOrThrow(fname)));
            } else if (short.class == type || Short.class == type) {
                f.set(obj, cursor.getShort(cursor.getColumnIndexOrThrow(fname)));
            } else if (float.class == type || Float.class == type) {
                f.set(obj, cursor.getDouble(cursor.getColumnIndexOrThrow(fname)));
            } else if (double.class == type || Double.class == type) {
                f.set(obj, cursor.getDouble(cursor.getColumnIndexOrThrow(fname)));
            } else if (boolean.class == type || Boolean.class == type) {
                f.set(obj, cursor.getInt(cursor.getColumnIndexOrThrow(fname)));
            } else if (byte[].class == type) {
                f.set(obj, cursor.getBlob(cursor.getColumnIndexOrThrow(fname)));
            } else if (Date.class == type) {
                f.set(obj, new Date(cursor.getLong(cursor.getColumnIndexOrThrow(fname))));
            } else {
                // 其他类型或数组
                f.set(obj, GsonUtil.fromJsonString(cursor.getString(cursor.getColumnIndexOrThrow(fname)), type));
            }
            if (!access) {
                f.setAccessible(false);
            }
        }
    }

    /**
     * 通过obj对象构建ContentValues对象，即map()方法的逆方法
     *
     * @param obj
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public ContentValues marshal(T obj) throws IllegalAccessException, IllegalArgumentException {
        if (null == obj) {
            throw new IllegalArgumentException("赋值对象为空");
        }
        ContentValues cv = new ContentValues();
        Field[] fields = tClass.getDeclaredFields();
        for (Field f : fields) {
            Annotation annotation = f.getAnnotation(Column.class);
            if (null == annotation) {
                continue;
            }

            String value = ((Column) annotation).value();
            String fname = (null == value || value.isEmpty()) ? f.getName().toLowerCase() : value;

            Class<?> type = f.getType();
            boolean access = f.isAccessible();
            if (!access) {
                f.setAccessible(true);
            }
            if (CharSequence.class.isAssignableFrom(type)) {
                cv.put(fname, (String) f.get(obj));
            } else if (char.class == type || Character.class == type) {
                cv.put(fname, f.get(obj).toString());
            } else if (byte.class == type || Byte.class == type) {
                cv.put(fname, f.get(obj).toString());
            } else if (int.class == type || Integer.class == type) {
                cv.put(fname, f.getInt(obj));
            } else if (long.class == type || Long.class == type) {
                cv.put(fname, f.getLong(obj));
            } else if (short.class == type || Short.class == type) {
                cv.put(fname, f.getShort(obj));
            } else if (short.class == type || Short.class == type) {
                cv.put(fname, f.getDouble(obj));
            } else if (double.class == type || Double.class == type) {
                cv.put(fname, f.getDouble(obj));
            } else if (boolean.class == type || Boolean.class == type) {
                cv.put(fname, f.getBoolean(obj));
            } else if (byte[].class == type) {
                cv.put(fname, (byte[]) f.get(obj));
            } else if (Date.class == type) {
                cv.put(fname, ((Date) f.get(obj)).getTime());
            } else {
                cv.put(fname, GsonUtil.toJsonString(f.get(obj), type));
            }
            if (!access) {
                f.setAccessible(false);
            }
        }

        return cv;
    }

    /**
     * 新增数据。若主键冲突，则以覆盖的方式写入。
     *
     * @param obj
     * @return 插入的新行ID
     */
    @CheckResult
    public Observable<Long> insert(T obj) {
        ContentValues cv;
        try {
            cv = marshal(obj);
        } catch (Exception e) {
            throw new DaoException("构建ContentValues对象错误", e);
        }

        return insert(getTableName(), cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * 删除数据
     *
     * @param whereClause where子句
     * @param whereArgs 参数
     * @return 删除行数
     */
    @CheckResult
    public Observable<Integer> delete(@Nullable final String whereClause,
                                      @Nullable final String... whereArgs) {
        return delete1(getTableName(), whereClause, whereArgs);
    }

    /**
     * 删除数据
     * @return 删除行数
     */
    @CheckResult
    public Observable<Integer> deleteAll() {
        return delete1(getTableName(), null);
    }

    /**
     * 更新数据。
     * 这里使用冲突覆盖的方式实现更新。
     *
     * @param obj
     * @param whereClause
     * @param whereArgs
     * @return 更新行数
     */
    @CheckResult
    protected Observable<Integer> update(T obj,
                                         @Nullable final String whereClause,
                                         @Nullable final String... whereArgs) {
        ContentValues cv;
        try {
            cv = marshal(obj);
        } catch (Exception e) {
            throw new DaoException("构建ContentValues对象错误", e);
        }
        return update(getTableName(), cv, SQLiteDatabase.CONFLICT_REPLACE, whereClause, whereArgs);
    }
}
