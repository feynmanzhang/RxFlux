package com.lean.rxflux.dao.entity;

/**
 * 数据持久化模式。
 *
 * APPEND：追加存储。在使用这种模式应注意设定主键@PrmaryKey，避免数据重复存储。
 * OVERWRITE: 清空表后存储
 *
 * @author Lean
 */
public enum SaveMode {
    APPEND, OVERWRITE
}
