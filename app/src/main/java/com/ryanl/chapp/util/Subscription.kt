package com.ryanl.chapp.util

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Subscription<K,P>(val name: String) {
    private val subscriberMap: MutableMap<K, suspend (P) -> Unit> = mutableMapOf()
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
            subscriberMap[cb as K] = cb
        }
    }

    suspend fun unsubscribe(key: K) {
        Log.d(TAG, "Unsubscribed")
        subscriberMutex.withLock {
            subscriberMap.remove(key)
        }
    }

    suspend fun notify(cb: (Map<K, suspend (P) -> Unit>) -> Unit) {
        subscriberMutex.withLock {
            cb(subscriberMap)
        }
    }

    suspend fun notifyAll(msg: P) {
        subscriberMutex.withLock {
            subscriberMap.forEach { (key, cb) ->
                cb(msg)
            }
        }
    }

    suspend fun notifyOne(key: K, msg: P) {
        subscriberMutex.withLock {
            subscriberMap[key]?.let { cb ->
                Log.d(TAG, "Send to $key")
                cb(msg)
            }
        }
    }
}