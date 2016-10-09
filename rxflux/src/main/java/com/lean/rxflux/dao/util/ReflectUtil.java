package com.lean.rxflux.dao.util;

import android.support.annotation.NonNull;

import com.lean.rxflux.dao.annotation.Column;
import com.lean.rxflux.dao.exception.DaoException;

import java.lang.reflect.Field;
import java.util.Date;


/**
 * 反射操作工具类
 *
 * @author Lean
 */
public class ReflectUtil {

    /**
     * 获取字段值值
     *
     * @param t
     * @param f
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static <T> Object getFiledValue(@NonNull T t, @NonNull Field f) throws DaoException {
        boolean access = f.isAccessible();
        if (!access) {
            f.setAccessible(true);
        }

        Object obj = null;
        try {
            obj = f.get(t);
        } catch (Exception e) {
            throw new DaoException("对象映射字段\"" + f.getName() + "\"异常", e);
        }

        if (!access) {
            f.setAccessible(false);
        }
        return obj;
    }

    /**
     * 判断是否为可存储对象
     *
     * @param t
     * @param <T>
     * @return
     */
    public static <T> boolean isModelObject(@NonNull T t) {
        Class<?> c = t.getClass();
        Field[] fields = c.getDeclaredFields();
        for (Field f : fields) {
            if (f.isAnnotationPresent(Column.class)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 是否为常用的Java内置数据类型
     *
     * @param c
     * @return
     */
    public static boolean isInternalClass(Class<?> c) {
        if (c.isPrimitive() ||
                CharSequence.class.isAssignableFrom(c) ||
                Number.class.isAssignableFrom(c) ||
                Character.class == c ||
                Boolean.class == c ||
                Date.class == c) {
            return true;
        }
        return false;
    }
}
