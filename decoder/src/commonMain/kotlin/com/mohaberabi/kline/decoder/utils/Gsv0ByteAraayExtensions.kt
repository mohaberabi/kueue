package com.mohaberabi.kline.decoder.utils

import com.mohaberabi.kline.decoder.GsV0Block


internal fun ByteArray.extractGsV0Blocks(): List<GsV0Block> {
    val payload = this
    val blocks = mutableListOf<GsV0Block>()
    var i = 0
    fun u8(idx: Int) = payload[idx].toInt() and 0xFF
    while (i <= payload.size - 8) {
        val isGsV0 =
            payload[i] == 0x1D.toByte() &&
                    payload[i + 1] == 0x76.toByte() &&
                    payload[i + 2] == 0x30.toByte()

        if (!isGsV0) {
            i++
            continue
        }

        val widthBytes = u8(i + 4) or (u8(i + 5) shl 8)
        val height = u8(i + 6) or (u8(i + 7) shl 8)
        val dataOffset = i + 8
        val dataLength = widthBytes * height
        if (widthBytes <= 0 || height <= 0) {
            i++
            continue
        }
        if (dataOffset + dataLength > payload.size) {
            break
        }
        blocks += GsV0Block(widthBytes, height, dataOffset, dataLength)
        i = dataOffset + dataLength
    }

    return blocks
}