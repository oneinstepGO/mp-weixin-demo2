package com.oneinstep.ddd.common.util.lock;

import cn.hutool.core.lang.Pair;

import java.util.function.Supplier;

public interface IDistributedLock<T> {

    T lock(String lockKey, Supplier<T> supplier);

    Pair<Boolean, T> tryLock(String lockKey, Supplier<T> supplier);
}
