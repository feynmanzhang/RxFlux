package com.lean.rxflux.dao.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PRIMARY KEY.
 *
 * 主键标注,不能替代@Column.字段都需要标注@Column
 *
 * @author Lean
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.FIELD)
public @interface PK {
}
