package com.letovpn.proxychecker.model

data class ProxyItem(
    val id: String = java.util.UUID.randomUUID().toString().take(8),
    val host: String,
    val port: Int,
    val secret: String = "",
    val country: String = "",
    val latency: Long = -1,
    val isWorking: Boolean = false,
    val isTesting: Boolean = false
) {
    fun toTelegramUrl(): String {
        var url = "tg://proxy?server=$host&port=$port"
        if (secret.isNotBlank()) url += "&secret=$secret"
        return url
    }
    fun display(): String = "$host:$port"
}
