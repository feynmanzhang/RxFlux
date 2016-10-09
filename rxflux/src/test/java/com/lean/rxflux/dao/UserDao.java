package com.lean.rxflux.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lean.rxflux.dao.entity.User;

import rx.functions.Func1;


/**
 *
 * @author Lean
 */
public class UserDao extends AbstractRxDao {

    private final String TABLE = "test";
    private final String COL_ID = "id";
    private final String COL_NAME = "name";
    private final String COL_AGE = "age";
    private final String COL_WEIGHT = "weight";
    private final String COL_BLOB = "blob";

    @Override
    public void createTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE test (" +
                " id INTEGER PRIMARY KEY autoincrement," +
                " name TEXT NOT NULL," +
                " age INTEGER NOT NULL," +
                " weight DOUBLE NOT NULL," +
                " blob BLOB)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            String sql = "ALTER TABLE test ADD COLUMN weight DOUBLE NOT NULL";
            db.execSQL(sql);
        }
    }

    /**
     * Insert {@link User}
     *
     */
    public User insert(String name, int age, double weight, byte[] blob) {

        ContentValues cv = new ContentValues(4);
        cv.put(COL_NAME, name);
        cv.put(COL_AGE, age);
        cv.put(COL_WEIGHT, weight);
        cv.put(COL_BLOB, blob);

        long id = insert(TABLE, cv).toBlocking().first();

        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setWeight(weight);
        u.setBlob(blob);

        return u;
    }

    public User getById(long id) {

        return db.createQuery(TABLE,"SELECT * FROM " + TABLE + " WHERE " + COL_ID + "=?", Long.toString(id))
                .mapToOne(new Func1<Cursor, User>() {
                    @Override
                    public User call(Cursor c) {
                        User user = new User();
                        user.setId(c.getLong(0));
                        user.setAge(c.getInt(2));
                        user.setBlob(c.getBlob(4));
                        user.setName(c.getString(1));
                        user.setWeight(c.getDouble(3));
                        return user;
                    }
                })
                .toBlocking()
                .first();
    }
}
