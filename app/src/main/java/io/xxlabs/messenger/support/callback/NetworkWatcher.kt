package io.xxlabs.messenger.support.callback

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import timber.log.Timber

internal class NetworkWatcher {
    companion object {
        fun startWatchingNetwork(
            context: Context,
            onAvailable: (() -> Unit),
            onLost: (() -> Unit),
            onChanged: (() -> Unit),
        ) {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val builder = NetworkRequest.Builder()
            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

            connectivityManager.registerNetworkCallback(builder.build(),
                object : ConnectivityManager.NetworkCallback() {
                    var previousConnectionType: String = "NONE"

                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        Timber.v("Network status changed: Internet is available")
                        Handler(Looper.getMainLooper()).post(onAvailable)
                    }

                    override fun onLost(network: Network) {
                        super.onLost(network)
                        Timber.v("Network status changed: Internet is not available")
                        previousConnectionType = "NONE"
                        Handler(Looper.getMainLooper()).post(onLost)
                    }

                    override fun onUnavailable() {
                        super.onUnavailable()
                        Timber.v("Network status changed: Internet is unnavailable")
                        previousConnectionType = "NONE"
                        Handler(Looper.getMainLooper()).post(onLost)
                    }

//                    override fun onCapabilitiesChanged(
//                        network: Network,
//                        networkCapabilities: NetworkCapabilities
//                    ) {
//                        super.onCapabilitiesChanged(network, networkCapabilities)
//                        val connectionType = getCurrentCapability(networkCapabilities)
//                        Timber.v("[NETWORK]Previous connection type: $previousConnectionType")
//                        Timber.v("[NETWORK]New connection type: $connectionType")
//                        if (previousConnectionType != connectionType) {
//                            previousConnectionType = connectionType
//                            Timber.v("[NETWORK]connection type changed: $networkCapabilities")
//
//                            if (connectionType != "NONE") {
//                                Handler(Looper.getMainLooper()).post(onChanged)
//                            }
//                        } else {
//                            Timber.v("[NETWORK]Refreshed Current Capability $previousConnectionType")
//                        }
//                    }
                }
            )
        }

        private fun getCurrentCapability(networkCapabilities: NetworkCapabilities) = when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                "WIFI"
            }
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                "CELLULAR"
            }
            else -> {
                "NONE"
            }
        }

        /**
         *
         * Indicates whether network connectivity exists
         */
        fun isConnected(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val currentNetwork =
                connectivityManager.activeNetwork ?: return false
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(currentNetwork)
                    ?: return false
            return when {
                activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> true
                else -> false
            }
        }
    }
}