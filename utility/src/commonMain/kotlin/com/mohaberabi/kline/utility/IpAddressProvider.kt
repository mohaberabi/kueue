package com.mohaberabi.kline.utility

interface IpAddressProvider {
    suspend fun getIpAddress(): String?
}