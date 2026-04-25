package com.streamatico.polymarketviewer.data.preferences

enum class DnsOverHttpsProvider(val storageValue: String) {
    GOOGLE("google"),
    CLOUDFLARE("cloudflare");

    companion object {
        val DEFAULT = GOOGLE

        fun fromStorageValue(value: String?): DnsOverHttpsProvider {
            return entries.firstOrNull { it.storageValue == value } ?: DEFAULT
        }
    }
}

