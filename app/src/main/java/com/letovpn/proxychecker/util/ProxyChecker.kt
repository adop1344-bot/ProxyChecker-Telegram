package com.letovpn.proxychecker.util

import com.letovpn.proxychecker.model.Proxy
import com.letovpn.proxychecker.model.ProxyStatus
import com.letovpn.proxychecker.model.ProxyType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL

class ProxyChecker {

    companion object {
        private const val TIMEOUT_MS = 5000L
        private const val MAX_CONCURRENCY = 500
    }

    suspend fun checkProxiesBatch(
        proxies: List<Proxy>,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): List<Proxy> = withContext(Dispatchers.IO) {
        proxies.map { proxy ->
            async {
                val checked = checkProxyWithCountry(proxy)
                onProgress(proxies.indexOf(proxy) + 1, proxies.size)
                checked
            }
        }.awaitAll()
    }

    suspend fun checkProxyWithCountry(proxy: Proxy): Proxy {
        val checked = checkProxy(proxy)
        if (checked.status == ProxyStatus.WORKING && checked.country == null) {
            return checked.copy(country = detectCountry(checked.host))
        }
        return checked
    }

    private suspend fun checkProxy(proxy: Proxy): Proxy {
        return withContext(Dispatchers.IO) {
            try {
                val start = System.currentTimeMillis()
                when (proxy.type) {
                    ProxyType.SOCKS5, ProxyType.SOCKS4 -> testSocksProxy(proxy)
                    ProxyType.HTTP -> testHttpProxy(proxy)
                    ProxyType.MTProto -> testMtProtoProxy(proxy)
                }
                val latency = System.currentTimeMillis() - start
                proxy.copy(status = ProxyStatus.WORKING, latency = latency)
            } catch (e: Exception) {
                proxy.copy(status = ProxyStatus.DEAD, latency = -1)
            }
        }
    }

    private fun testSocksProxy(proxy: Proxy): Boolean {
        val socket = Socket()
        try {
            socket.connect(InetSocketAddress(proxy.host, proxy.port), TIMEOUT_MS.toInt())
            return true
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun testHttpProxy(proxy: Proxy): Boolean {
        val url = URL("http://httpbin.org/ip")
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT_MS.toInt()
        connection.readTimeout = TIMEOUT_MS.toInt()
        connection.useCaches = false
        try {
            connection.inputStream.read()
            return connection.responseCode == 200
        } finally {
            connection.disconnect()
        }
    }

    private fun testMtProtoProxy(proxy: Proxy): Boolean {
        val socket = Socket()
        try {
            socket.connect(InetSocketAddress(proxy.host, proxy.port), TIMEOUT_MS.toInt())
            return true
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private suspend fun detectCountry(host: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("http://ip-api.com/json/$host?fields=country,countryCode")
                val json = url.readText()
                val obj = JSONObject(json)
                obj.getString("country")
            } catch (e: Exception) {
                null
            }
        }
    }
}
