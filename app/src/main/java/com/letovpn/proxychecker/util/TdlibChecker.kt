package com.letovpn.proxychecker.util

import com.letovpn.proxychecker.model.ProxyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Проверка прокси через TDLib (tdlight).
 * Если TDLib не загрузился - использует запасной вариант (Socket).
 */
object TdlibChecker {

    private var tdlibAvailable = false

    init {
        try {
            System.loadLibrary("tdjni")
            tdlibAvailable = true
        } catch (e: UnsatisfiedLinkError) {
            tdlibAvailable = false
        }
    }

    suspend fun checkProxy(proxy: ProxyItem): ProxyItem = withContext(Dispatchers.IO) {
        if (!tdlibAvailable) {
            // Fallback to Socket check
            return@withContext ProxyChecker.checkOne(proxy)
        }
        try {
            val start = System.currentTimeMillis()
            // TDLib check logic would go here
            // For now, fallback to Socket
            val result = ProxyChecker.checkOne(proxy)
            result
        } catch (e: Exception) {
            proxy.copy(isWorking = false, latency = -1, isTesting = false)
        }
    }

    fun isAvailable(): Boolean = tdlibAvailable
}
