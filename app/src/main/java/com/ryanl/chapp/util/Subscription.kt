package com.ryanl.chapp.util

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class Subscription<K,P>(val name: String) {
    private val subscriberMap: MutableMap<K, suspend (P) -> Unit> = mutableMapOf()
    private val subscriberSet: MutableSet<suspend (P) -> Unit> = mutableSetOf()
    private val subscriberMutex = Mutex()
    private val TAG = "Subscription[$name]"

    suspend fun subscribe(key: K, cb: suspend (P) -> Unit) {
        Log.d(TAG, "Subscribed")
        subscriberMutex.withLock {
            subscriberMap[key] = cb
        }
    }

    suspend fun subscribe(cb: suspend (P) -> Unit) {
        Log.d(TAG, "Subscribed")
        subscriberMutex.withLock {
            subscriberSet.add(cb)
        }
    }

    suspend fun unsubscribe(key: K) {
        Log.d(TAG, "Unsubscribed")
        subscriberMutex.withLock {
            subscriberMap.remove(key)
        }
    }

    suspend fun unsubscribe(cb: suspend (P) -> Unit) {
        Log.d(TAG, "Unsubscribed")
        subscriberMutex.withLock {
            subscriberSet.remove(cb)
        }
    }

    protected suspend fun notifyAll(msg: P) {
        subscriberMutex.withLock {
            subscriberSet.forEach { cb ->
                cb(msg)
            }
        }
    }

    protected suspend fun notifyOne(key: K, msg: P) {
        subscriberMutex.withLock {
            subscriberMap[key]?.let { cb ->
                Log.d(TAG, "Send to $key")
                cb(msg)
            }
        }
    }
}