package com.mista.weather.testutil

import com.mista.weather.session.CacheWrapper

class FakeCacheWrapper<T>(initial: T? = null) : CacheWrapper<T> {
    override var value: T? = initial

    override fun clear() {
        value = null
    }
}
