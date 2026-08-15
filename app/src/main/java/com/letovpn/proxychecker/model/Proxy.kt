package com.letovpn.proxychecker.model

data class Proxy(
    val id: String = System.currentTimeMillis().toString(),
    val host: String,
    val port: Int,
    val type: ProxyType = ProxyType.SOCKS5,
    val username: String? = null,
    val password: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val latency: Long = -1,
    val status: ProxyStatus = ProxyStatus.UNKNOWN,
    val source: String = "manual",
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toTelegramUrl(): String {
        val auth = if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) "$username:$password@" else ""
        return when (type) {
            ProxyType.SOCKS5 -> "tg://socks?server=$host&port=$port${if (auth.isNotEmpty()) "&user=$username&pass=$password" else ""}"
            ProxyType.SOCKS4 -> "tg://socks?server=$host&port=$port"
            ProxyType.HTTP -> "tg://proxy?server=$host&port=$port${if (auth.isNotEmpty()) "&user=$username&pass=$password" else ""}"
            ProxyType.MTProto -> "tg://proxy?server=$host&port=$port"
        }
    }

    fun toDisplayString(): String {
        val auth = if (!username.isNullOrEmpty()) "$username:***@" else ""
        return "$auth$host:$port"
    }
}

enum class ProxyType { SOCKS5, SOCKS4, HTTP, MTProto }
enum class ProxyStatus { UNKNOWN, TESTING, WORKING, DEAD }

data class ProxySource(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val url: String,
    val type: SourceType = SourceType.TEXT,
    val isEnabled: Boolean = true,
    val lastFetch: Long = 0,
    val fetchInterval: Long = 3600000
)

enum class SourceType { JSON, TEXT, API }
