package com.letovpn.proxychecker.util

import com.letovpn.proxychecker.model.ProxyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

object ProxyChecker {

    suspend fun checkAll(proxies: List<ProxyItem>): List<ProxyItem> = withContext(Dispatchers.IO) {
        proxies.map { p ->
            async {
                try {
                    val start = System.currentTimeMillis()
                    Socket().use { it.connect(InetSocketAddress(p.host, p.port), 3000) }
                    val ms = System.currentTimeMillis() - start
                    p.copy(isWorking = true, latency = ms, isTesting = false)
                } catch (e: Exception) {
                    p.copy(isWorking = false, latency = -1, isTesting = false)
                }
            }
        }.awaitAll()
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
            val url = java.net.URL("http://ip-api.com/json/$host?fields=country")
            val json = url.readText()
            val obj = org.json.JSONObject(json)
            obj.optString("country", "")
        } catch (e: Exception) { "" }
    }
}
