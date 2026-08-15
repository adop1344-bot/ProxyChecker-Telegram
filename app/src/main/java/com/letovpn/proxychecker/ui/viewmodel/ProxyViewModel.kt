package com.letovpn.proxychecker.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.letovpn.proxychecker.model.Proxy
import com.letovpn.proxychecker.model.ProxySource
import com.letovpn.proxychecker.model.ProxyStatus
import com.letovpn.proxychecker.model.ProxyType
import com.letovpn.proxychecker.model.SourceType
import com.letovpn.proxychecker.util.ProxyChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.URL

class ProxyViewModel(application: Application) : AndroidViewModel(application) {
    private val _proxies = MutableStateFlow<List<Proxy>>(emptyList())
    val proxies: StateFlow<List<Proxy>> = _proxies.asStateFlow()
    private val _sources = MutableStateFlow<List<ProxySource>>(emptyList())
    val sources: StateFlow<List<ProxySource>> = _sources.asStateFlow()
    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()
    private val _checkProgress = MutableStateFlow(0)
    val checkProgress: StateFlow<Int> = _checkProgress.asStateFlow()
    private val _checkTotal = MutableStateFlow(0)
    val checkTotal: StateFlow<Int> = _checkTotal.asStateFlow()
    private val proxyChecker = ProxyChecker()

    init { loadDefaultSources() }

    private fun loadDefaultSources() {
        _sources.value = listOf(
            ProxySource(name = "ProxyScrape SOCKS5", url = "https://api.proxyscrape.com/v2/?request=get&protocol=socks5&timeout=10000&country=all", type = SourceType.TEXT),
            ProxySource(name = "ProxyScrape SOCKS4", url = "https://api.proxyscrape.com/v2/?request=get&protocol=socks4&timeout=10000&country.TEXTn ProxySource(name url "https:///?=http&country=all", type = SourceType.TEXT)
        )
    }

    fun addProxy(proxy: Proxy) { _proxies.value = _proxies.value + proxy }
    fun removeProxy(proxy: Proxy) { _proxies.value = _proxies.value.filter { it.id != proxy.id } }
    fun clearProxies() { _proxies.value = emptyList() }
    fun addSource(source: ProxySource) { _sources.value = _sources.value + source }
    fun removeSource(source: ProxySource) { _sources.value = _sources.value.filter { it.id != source.id } }
    fun toggleSource(source: ProxySource) { _sources.value = _sources.value.map { if (it.id == source.id) it.copy(isEnabled = !it.isEnabled) else it } }

    fun checkAllProxies() {
        viewModelScope.launch {
            _isChecking.value = true
            _checkTotal.value = _proxies.value.size
            _checkProgress.value = 0
            val checked = proxyChecker.checkProxiesBatch(_proxies.value) { checked, _ -> _checkProgress.value = checked }
            _proxies.value = checked
            _isChecking.value = false
        }
    }

    fun checkProxy(proxy: Proxy) {
        viewModelScope.launch {
            val checked = proxyChecker.checkProxyWithCountry(proxy)
            _proxies.value = _proxies.value.map { if (it.id == proxy.id) checked else it }
        }
    }

    fun fetchFromSources() {
        viewModelScope.launch {
            _isChecking.value = true
            val newProxies = mutableListOf<Proxy>()
            _sources.value.filter { it.isEnabled }.forEach { source ->
                try { newProxies.addAll(fetchFromSource(source)) } catch (e: Exception) { e.printStackTrace() }
            }
            _proxies.value = newProxies
            _isChecking.value = false
        }
    }

    private suspend fun fetchFromSource(source: ProxySource): List<Proxy> {
        return try {
            val content = URL(source.url).readText()
            when (source.type) {
                SourceType.TEXT -> parseTextProxies(content)
                SourceType.JSON -> parseJsonProxies(content)
                else -> emptyList()
            }
        } catch (e: Exception) { emptyList() }
    }

    private fun parseTextProxies(content: String): List<Proxy> {
        return content.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            try {
                val parts = line.trim().split(":")
                if (parts.size >= 2) Proxy(host = parts[0], port = parts[1].toInt(), type = ProxyType.SOCKS5, source = "fetched") else null
            } catch (e: Exception) { null }
        }
    }

    private fun parseJsonProxies(content: String): List<Proxy> {
        return try {
            val json = JSONArray(content)
            (0 until json.length()).mapNotNull { i ->
                try {
                    val obj = json.getJSONObject(i)
                    Proxy(host = obj.getString("ip"), port = obj.getInt("port"), type = ProxyType.SOCKS5, country = obj.optString("country", null), source = "fetched")
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) { emptyList() }
    }

    fun openInTelegram(proxy: Proxy) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(proxy.toTelegramUrl()))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }

    fun getWorkingProxies(): List<Proxy> = _proxies.value.filter { it.status == ProxyStatus.WORKING }
}
