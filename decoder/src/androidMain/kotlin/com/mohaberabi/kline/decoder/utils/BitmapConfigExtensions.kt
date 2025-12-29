package com.mohaberabi.kline.decoder.utils

import android.graphics.Bitmap
import com.mohaberabi.kline.decoder.PlatformBitmapConfig


internal fun PlatformBitmapConfig.toBitmapConfig() = when (this) {
    PlatformBitmapConfig.ALPHA_8 -> Bitmap.Config.ALPHA_8
    PlatformBitmapConfig.RGB_565 -> Bitmap.Config.RGB_565
    PlatformBitmapConfig.ARGB_4444 -> Bitmap.Config.ARGB_4444
    PlatformBitmapConfig.ARGB_8888 -> Bitmap.Config.ARGB_8888
    PlatformBitmapConfig.RGBA_F16 -> Bitmap.Config.RGBA_F16
    PlatformBitmapConfig.HARDWARE -> Bitmap.Config.HARDWARE
    PlatformBitmapConfig.RGBA_1010102 -> Bitmap.Config.RGBA_1010102
}