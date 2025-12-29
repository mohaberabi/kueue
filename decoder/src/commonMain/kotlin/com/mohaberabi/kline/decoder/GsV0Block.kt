package com.mohaberabi.kline.decoder

data class GsV0Block(
    val widthBytes: Int,
    val height: Int,
    val dataOffset: Int,
    val dataLength: Int
)