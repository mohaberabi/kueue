package com.mohaberabi.kline.decoder;

enum class PlatformBitmapConfig(
    val key: Int,
) {

    ALPHA_8(1),


    RGB_565(3),

    ARGB_4444(4),

    ARGB_8888(5),

    RGBA_F16(6),

    HARDWARE(7),


    RGBA_1010102(8);

}