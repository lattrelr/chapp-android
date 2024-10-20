package com.ryanl.chapp.socket

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.ryanl.chapp.ErrorReporter
import com.ryanl.chapp.persist.HistorySyncJob
import com.ryanl.chapp.util.Subscription
import kotlinx.coroutines.launch

object ConnectionManager {
    // TODO start this after login screen moves to history screen.
    // TODO stop this on logout
    private const val TAG = "ConnectivityManager"
    private var connectivityManager: ConnectivityManager? = null

    private fun updateNetworkState(state: Boolean) {
        kotlinx.coroutines.MainScope().launch {
            HistorySyncJob.onConnectionChanged(state)
            AuthenticationManager.onConnectionChanged(state)
            if (!state) {
                WebsocketClient.closeSocket()
            }
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.e(TAG, "The default network is now: $network")
            updateNetworkState(true)
            kotlinx.coroutines.MainScope().launch {
                ErrorReporter.clearError(ErrorReporter.ErrorTypes.NO_INTERNET)
            }
        }

        override fun onLost(network: Network) {
            Log.e(TAG, "The application no longer has a default network")
            updateNetworkState(false)
            kotlinx.coroutines.MainScope().launch {
                ErrorReporter.setError(ErrorReporter.ErrorTypes.NO_INTERNET)
            }
        }
    }

    fun start() {
        connectivityManager?.registerDefaultNetworkCallback(networkCallback)
    }

    fun stop() {
        connectivityManager?.unregisterNetworkCallback(networkCallback)
        kotlinx.coroutines.MainScope().launch {
            ErrorReporter.clearError(ErrorReporter.ErrorTypes.NO_INTERNET)
        }
    }

    fun open(context: Context) {
        connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        if (connectivityManager == null) {
            Log.e(TAG, "connManager is null!")
        }
    }
}