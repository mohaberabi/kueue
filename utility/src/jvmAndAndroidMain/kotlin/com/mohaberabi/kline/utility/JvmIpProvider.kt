package com.mohaberabi.kline.utility

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.net.NetworkInterface

/**
 * returns the current ip addresses for all of the network interfaces and gets the first potential one
 * wlan0 (WIFI)
 * eth0 (Ethernet Cable)
 * lo (Loopback)
 * rmnet0 (mobile)
 * tun0 (vpn)
 */
class JvmIpProvider(
    private val ioDispatcher: CoroutineDispatcher
) : IpAddressProvider {

    override suspend fun getIpAddress(): String? = withContext(ioDispatcher) {
        runCatching {
            /**
             * [NetworkInterface] gets all the current network interfaces to list
             * flatten their [NetworkInterface.inetAddresses] as each interface might have multiple
             * map each [NetworkInterface.inetAddresses] to the potential ip address by checking
             * if it has string representation [java.net.InetAddress.getHostAddress]
             * if it is of type 4 numbers separated by dots(IPV4)
             * if it is not locally for device [java.net.InetAddress.isLoopbackAddress]
             * if it is not a temporary address [java.net.InetAddress.isLinkLocalAddress]
             * then get the first one of them
             */
            NetworkInterface.getNetworkInterfaces().toList()
                .flatMap { it.inetAddresses.toList() }
                .mapNotNull { addr ->
                    val ip = addr.hostAddress ?: return@mapNotNull null
                    val isIpv4 = ip.count { it == '.' } == 3
                    val isLoopback = addr.isLoopbackAddress
                    val isLinkLocal = addr.isLinkLocalAddress
                    if (isIpv4 && !isLoopback && !isLinkLocal) ip else null
                }
                .distinct()
                .sorted()
        }.getOrElse { emptyList() }.firstOrNull()
    }
}