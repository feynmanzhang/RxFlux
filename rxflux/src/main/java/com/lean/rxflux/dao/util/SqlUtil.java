package com.lean.rxflux.dao.util;


import com.lean.rxflux.dao.annotation.Column;
import com.lean.rxflux.dao.annotation.PK;
import com.lean.rxflux.dao.exception.DaoException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * sql辅助类
 *
 * @author Lean
 */
public class SqlUtil {

    /**
     * 生成建表sql
     * @param tableName 建表表名
     * @param c model class
     * @return
     */
    public static String CREATE_TABLE(String tableName, Class<?> c) {
        if (tableName == null){
            throw new IllegalArgumentException("表名不能为空");
        }

        Field[] fields = c.getDeclaredFields();
        List<String> cList = new ArrayList<>();
        for (Field f : fields) {
            String cStatment = null;
            if (f.isAnnotationPresent(Column.class)) {
                Class<?> fType = f.getType();
                String fName = f.getAnnotation(Column.class).value();
                if (null == fName || fName.isEmpty()) {
                    cStatment = f.getName().toLowerCase() + defColumnType(fType);
                } else {
                    cStatment = fName + defColumnType(fType);
               }
            }

            if (f.isAnnotationPresent(PK.class)) {
                if (null == cStatment || 0 == cStatment.length()) {
                    throw new DaoException("@PK注解不能取代@Column,需要在主键上添加@Column注解");
                }
                cStatment += " NOT NULL PRIMARY KEY";
            }

            if (null != cStatment && 0 != cStatment.length()) {
                cList.add(cStatment);
            }
        }

        if (cList.size() == 0) {
            throw new DaoException("建表字段为空, 需要为实体添加@Column注解: " + c.toString());
        }

        StringBuilder builder = new StringBuilder("CREATE TABLE ");
        builder.append(tableName);
        builder.append(" ( ");
        for (int i = 0; i < cList.size(); i++) {
            builder.append(cList.get(i));
            if (i != (cList.size() - 1))
                builder.append(", ");
        }
        builder.append(" )");

        return builder.toString();
    }


    /**
     * 建表字段类型映射

     * 其中java.util.Date类型在内部映射为long类型，即时间戳格式存储。
     *
     * @param c
     * @return
     */
    private static String defColumnType(Class<?> c) {
        if (CharSequence.class.isAssignableFrom(c) || char.class == c || Character.class == c ||
                byte.class == c || Byte.class == c) {
            return " TEXT";
        } else if (boolean.class == c || int.class == c || long.class == c || short.class == c || Date.class == c
                || Boolean.class == c || Integer.class == c || Long.class == c || Short.class == c)  {
            return " INTEGER";
        } else if (float.class == c || double.class == c || Double.class == c || Float.class == c) {
            return " REAL";
        } else if (byte[].class == c) {
            return " BLOB";
        }

        // 其他类型或数组类型的,使用TEXT字段建表类型。
        return " TEXT";
    }


    /**
     * 生成删表sql
     * @param tableName
     * @return
     */
    public static String DROP_TABLE(String tableName) {
        if (tableName == null){
            throw new IllegalArgumentException("表名不能为空");
        }

        return "DROP TABLE " + tableName;
    }


    /**
     * 生成删表sql
     * @param tableName
     * @return
     */
    public static String DROP_TABLE_IF_EXISTS(String tableName) {
        if (tableName == null){
            throw new IllegalArgumentException("表名不能为空");
        }

        return "DROP TABLE IF EXISTS " + tableName;
    }

}
