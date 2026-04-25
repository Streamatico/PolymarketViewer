package com.streamatico.polymarketviewer.data.network

import android.content.Context
import android.util.Log
import com.streamatico.polymarketviewer.BuildConfig
import com.streamatico.polymarketviewer.data.preferences.DnsOverHttpsProvider
import com.streamatico.polymarketviewer.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Cache
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import java.io.File
import java.net.InetAddress

private const val GOOGLE_DOH_URL = "https://dns.google/dns-query"
private const val CLOUDFLARE_DOH_URL = "https://cloudflare-dns.com/dns-query"
private const val GOOGLE_DOH_CACHE_DIRECTORY_NAME = "doh_google_http_cache"
private const val CLOUDFLARE_DOH_CACHE_DIRECTORY_NAME = "doh_cloudflare_http_cache"
private const val DOH_CACHE_SIZE_BYTES = 50L * 1024L * 1024L
private const val GOOGLE_DNS_PRIMARY_IP = "8.8.8.8"
private const val GOOGLE_DNS_SECONDARY_IP = "8.8.4.4"
private const val CLOUDFLARE_DNS_PRIMARY_IP = "1.1.1.1"
private const val CLOUDFLARE_DNS_SECONDARY_IP = "1.0.0.1"
private const val TAG = "PolymarketDns"

internal class PolymarketDnsResolver(
    context: Context,
    userPreferencesRepository: UserPreferencesRepository,
    appScope: CoroutineScope
) : Dns {
    @Volatile
    private var activeDns: Dns = Dns.SYSTEM

    private val googleDohDns = createDnsOverHttps(
        context = context,
        cacheDirectoryName = GOOGLE_DOH_CACHE_DIRECTORY_NAME,
        dohUrl = GOOGLE_DOH_URL,
        bootstrapDnsIps = listOf(GOOGLE_DNS_PRIMARY_IP, GOOGLE_DNS_SECONDARY_IP)
    )

    private val cloudflareDohDns = createDnsOverHttps(
        context = context,
        cacheDirectoryName = CLOUDFLARE_DOH_CACHE_DIRECTORY_NAME,
        dohUrl = CLOUDFLARE_DOH_URL,
        bootstrapDnsIps = listOf(CLOUDFLARE_DNS_PRIMARY_IP, CLOUDFLARE_DNS_SECONDARY_IP)
    )

    init {
        appScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { preferences ->
                activeDns = if (!preferences.dohEnabled) {
                    Dns.SYSTEM
                } else {
                    when (preferences.dohProvider) {
                        DnsOverHttpsProvider.GOOGLE -> googleDohDns
                        DnsOverHttpsProvider.CLOUDFLARE -> cloudflareDohDns
                    }
                }

                if (BuildConfig.DEBUG) {
                    val mode = if (preferences.dohEnabled) {
                        preferences.dohProvider.storageValue
                    } else {
                        "system"
                    }
                    Log.d(TAG, "Polymarket DNS updated: $mode")
                }
            }
        }
    }

    override fun lookup(hostname: String): List<InetAddress> = activeDns.lookup(hostname)
}

private fun createDnsOverHttps(
    context: Context,
    cacheDirectoryName: String,
    dohUrl: String,
    bootstrapDnsIps: List<String>
): Dns {
    val bootstrapClient = OkHttpClient.Builder()
        .cache(
            Cache(
                directory = File(context.cacheDir, cacheDirectoryName),
                maxSize = DOH_CACHE_SIZE_BYTES
            )
        )
        .build()

    return DnsOverHttps.Builder()
        .client(bootstrapClient)
        .url(dohUrl.toHttpUrl())
        .bootstrapDnsHosts(*bootstrapDnsIps.map(InetAddress::getByName).toTypedArray())
        .build()
}


