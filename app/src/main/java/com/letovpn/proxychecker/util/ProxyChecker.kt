package com.letovpn.proxychecker.util

import com.letovpn.proxychecker.model.ProxyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

object ProxyChecker {

    suspend fun checkMany(proxies: List<ProxyItem>): List<ProxyItem> = withContext(Dispatchers.IO) {
        proxies.map { proxy ->
            try {
                val start = System.currentTimeMillis()
                val sock = Socket()
                sock.connect(InetSocketAddress(proxy.host, proxy.port), 3000)
                sock.close()
                val ms = System.currentTimeMillis() - start
                proxy.copy(isWorking = true, latency = ms, isTesting = false)
            } catch (e: Exception) {
                proxy.copy(isWorking = false, latency = -1, isTesting = false)
            }
        }
    }

    suspend fun checkOne(proxy: ProxyItem): ProxyItem = withContext(Dispatchers.IO) {
        try {
            val start = System.currentTimeMillis()
            val sock = Socket()
            sock.connect(InetSocketAddress(proxy.host, proxy.port), 3000)
            sock.close()
            val ms = System.currentTimeMillis() - start
            proxy.copy(isWorking = true, latency = ms, isTesting = false)
        } catch (e: Exception) {
            proxy.copy(isWorking = false, latency = -1, isTesting = false)
        }
    }
}
