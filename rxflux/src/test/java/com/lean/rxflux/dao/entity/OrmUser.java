package com.lean.rxflux.dao.entity;

import com.lean.rxflux.dao.annotation.Column;
import com.lean.rxflux.dao.annotation.PK;

import java.util.Arrays;


/**
 * Orm 测试实体
 *
 * @author Lean
 */
public class OrmUser {

    @PK
    @Column
    public long id;

    @Column
    public String name;

    @Column
    public int age;

    @Column
    public double weight;

    @Column
    public byte[] blob;

    public OrmUser() {
    }

    public OrmUser(long id, String name, int age, double weight, byte[] blob) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.blob = blob;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setBlob(byte[] blob) {
        this.blob = blob;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public double getWeight() {
        return weight;
    }

    public byte[] getBlob() {
        return blob;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrmUser user = (OrmUser) o;

        if (id != user.id) return false;
        if (age != user.age) return false;
        if (Double.compare(user.weight, weight) != 0) return false;
        if (!name.equals(user.name)) return false;
        return Arrays.equals(blob, user.blob);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (id ^ (id >>> 32));
        result = 31 * result + name.hashCode();
        result = 31 * result + age;
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + Arrays.hashCode(blob);
        return result;
    }
}
