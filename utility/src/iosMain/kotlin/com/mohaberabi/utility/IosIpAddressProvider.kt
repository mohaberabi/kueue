package com.mohaberabi.utility

import com.mohaberabi.kline.utility.IpAddressProvider
import kotlinx.cinterop.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import platform.darwin.*
import platform.posix.AF_INET
import platform.posix.in_addr
import platform.posix.sockaddr_in

class IosIpAddressProvider(
    private val ioDispatcher: CoroutineDispatcher
) : IpAddressProvider {

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getIpAddress(): String? = withContext(ioDispatcher) {
        memScoped {
            val ifAddrsVar = alloc<CPointerVar<ifaddrs>>()
            if (getifaddrs(ifAddrsVar.ptr) != 0) {
                return@memScoped null
            }

            val head = ifAddrsVar.value ?: return@memScoped null
            try {
                var ptr: CPointer<ifaddrs>? = head
                while (ptr != null) {
                    val ifa = ptr.pointed
                    val name = ifa.ifa_name?.toKString()
                    val addr = ifa.ifa_addr
                    if (name == "en0" && addr != null) {
                        val family = addr.pointed.sa_family.toInt()
                        if (family == AF_INET) {
                            val sockaddr = addr.reinterpret<sockaddr_in>().pointed
                            val ip = inet_ntoa(
                                cValue<in_addr> { s_addr = sockaddr.sin_addr.s_addr }
                            )?.toKString()
                            if (!ip.isNullOrBlank()) return@memScoped ip
                        }
                    }

                    ptr = ifa.ifa_next
                }

                null
            } finally {
                freeifaddrs(head)
            }
        }
    }
}