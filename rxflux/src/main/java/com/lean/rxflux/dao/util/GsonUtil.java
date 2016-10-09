package com.lean.rxflux.dao.util;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 *
 * @author Lean
 */
public class GsonUtil {

    private static final Gson gson = new Gson();

    private GsonUtil() {
    }

    /**
     * Object 转 JSON
     * @param object
     * @return
     */
    public static String toJsonString(Object object) {
        return gson.toJson(object);
    }

    /**
     * Object 转 JSON
     * @param object
     * @param typeOfT
     * @return
     */
    public static String toJsonString(Object object, Type typeOfT) {
        return gson.toJson(object, typeOfT);
    }

    /**
     * JSON 转 Object
     * @param json
     * @return
     */
    public static Object fromJsonString(String json) {
        return gson.fromJson(json, Object.class);
    }

    /**
     * JSON 转 Object
     * @param json
     * @param typeOfT
     * @param <T>
     * @return
     */
    public static <T> T fromJsonString(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    public static void main(String[] args) {
        String[] a = new String[]{"1","2","3"};
        String s = toJsonString(a);
        System.out.println(s);
        String[] aa = fromJsonString(s, String[].class);

        String ss = toJsonString(aa);
        System.out.println(ss);
    }

}
