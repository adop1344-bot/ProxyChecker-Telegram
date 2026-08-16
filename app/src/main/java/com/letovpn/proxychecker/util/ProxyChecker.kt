package com.letovpn.proxychecker.util

import com.letovpn.proxychecker.model.ProxyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL

object ProxyChecker {

    suspend fun checkAll(proxies: List<ProxyItem>): List<ProxyItem> = withContext(Dispatchers.IO) {
        proxies.map { p ->
            async {
                try {
                    val start = System.currentTimeMillis()
                    when (p.type) {
                        "HTTP" -> checkHttp(p)
                        else -> checkSocks(p) // SOCKS4, SOCKS5, MTProto
                    }
                    val ms = System.currentTimeMillis() - start
                    p.copy(isWorking = true, latency = ms, isTesting = false)
                } catch (e: Exception) {
                    p.copy(isWorking = false, latency = -1, isTesting = false)
                }
            }
        }.awaitAll()
    }

    private fun checkSocks(p: ProxyItem) {
        Socket().use { it.connect(InetSocketAddress(p.host, p.port), 3000) }
    }

    private fun checkHttp(p: ProxyItem) {
        val conn = URL("http://httpbin.org/ip").openConnection() as HttpURLConnection
        conn.connectTimeout = 3000
        conn.readTimeout = 3000
        conn.inputStream.read()
        conn.disconnect()
    }

    suspend fun checkOne(p: ProxyItem): ProxyItem = withContext(Dispatchers.IO) {
        try {
            val start = System.currentTimeMillis()
            Socket().use { it.connect(InetSocketAddress(p.host, p.port), 3000) }
            val ms = System.currentTimeMillis() - start
            p.copy(isWorking = true, latency = ms, isTesting = false)
        } catch (e: Exception) {
            p.copy(isWorking = false, latency = -1, isTesting = false)
        }
    }

    suspend fun detectCountry(host: String): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://ip-api.com/json/$host?fields=country")
            val json = url.readText()
            val obj = org.json.JSONObject(json)
            obj.optString("country", "")
        } catch (e: Exception) { "" }
    }
}
