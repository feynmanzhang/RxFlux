package com.lean.rxflux.dao.exception;

/**
 * 定义Dao层异常
 *
 * @author Lean
 */
public class DaoException extends RuntimeException {

    public DaoException(String message) {
        super(message);
    }

    public DaoException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
