package com.letovpn.proxychecker.util

import com.letovpn.proxychecker.model.ProxyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TdlibChecker {

    private var loaded = false

    init {
        try {
            System.loadLibrary("tdjni")
            loaded = true
        } catch (e: UnsatisfiedLinkError) {
            loaded = false
        }
    }

    suspend fun checkProxy(proxy: ProxyItem): ProxyItem = withContext(Dispatchers.IO) {
        if (!loaded) {
            // Fallback: simple socket check
            try {
                val start = System.currentTimeMillis()
                java.net.Socket().use { it.connect(java.net.InetSocketAddress(proxy.host, proxy.port), 3000) }
                val ms = System.currentTimeMillis() - start
                return@withContext proxy.copy(isWorking = true, latency = ms, isTesting = false)
            } catch (e: Exception) {
                return@withContext proxy.copy(isWorking = false, latency = -1, isTesting = false)
            }
        }
        try {
            // TDLib check would go here
            // For now fallback to socket
            val start = System.currentTimeMillis()
            java.net.Socket().use { it.connect(java.net.InetSocketAddress(proxy.host, proxy.port), 3000) }
            val ms = System.currentTimeMillis() - start
            proxy.copy(isWorking = true, latency = ms, isTesting = false)
        } catch (e: Exception) {
            proxy.copy(isWorking = false, latency = -1, isTesting = false)
        }
    }

    fun isAvailable(): Boolean = loaded
}
