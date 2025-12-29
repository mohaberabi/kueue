package com.mohaberabi.kline.decoder.utils


fun ByteArray.containsRaster(): Boolean {
    val payload = this
    for (i in 0 until payload.size - 2) {
        if (payload[i] == 0x1D.toByte() &&
            payload[i + 1] == 0x76.toByte() &&
            payload[i + 2] == 0x30.toByte()
        ) return true
    }
    return false
}
