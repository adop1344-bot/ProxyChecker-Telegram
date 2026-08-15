package com.letovpn.proxychecker.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.letovpn.proxychecker.model.Proxy
import com.letovpn.proxychecker.model.ProxyStatus
import com.letovpn.proxychecker.model.ProxyType
import com.letovpn.proxychecker.util.ProxyChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class ProxyViewModel(application: Application) : AndroidViewModel(application) {
    private val _proxies = MutableStateFlow<List<Proxy>>(emptyList())
    val proxies: StateFlow<List<Proxy>> = _proxies.asStateFlow()
    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()
    private val proxyChecker = ProxyChecker()

    fun addTestProxies() {
        _proxies.value = listOf(
            Proxy(host = "185.199.108.154", port = 1080, type = ProxyType.SOCKS5, country = "USA"),
            Proxy(host = "45.67.89.10", port = 1080, type = ProxyType.SOCKS5, country = "Netherlands"),
            Proxy(host = "103.45.67.89", port = 4153, type = ProxyType.SOCKS5, country = "Singapore"),
            Proxy(host = "192.168.1.1", port = 3128, type = ProxyType.HTTP, country = "Germany")
        )
    }

    fun clearProxies() { _proxies.value = emptyList() }

    fun checkAllProxies() {
        viewModelScope.launch {
            _isChecking.value = true
            val checked = proxyChecker.checkProxiesBatch(_proxies.value) { _, _ -> }
            _proxies.value = checked
            _isChecking.value = false
        }
    }

    fun openInTelegram(proxy: Proxy) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(proxy.toTelegramUrl()))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }
}
