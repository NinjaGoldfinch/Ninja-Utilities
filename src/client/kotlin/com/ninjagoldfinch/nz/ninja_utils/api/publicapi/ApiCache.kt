package com.ninjagoldfinch.nz.ninja_utils.api.publicapi

class ApiCache<T>(private val ttlMs: Long) {
    private var data: T? = null
    private var fetchTime: Long = 0

    fun get(): T? {
        if (data != null && System.currentTimeMillis() - fetchTime < ttlMs) {
            return data
        }
        return null
    }

    fun put(value: T) {
        data = value
        fetchTime = System.currentTimeMillis()
    }

    fun invalidate() {
        data = null
        fetchTime = 0
    }

    val isFresh: Boolean get() = data != null && System.currentTimeMillis() - fetchTime < ttlMs
}
