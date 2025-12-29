package com.mohaberabi.kline.decoder.utils

import androidx.compose.ui.graphics.ImageBitmap
import com.mohaberabi.kline.decoder.PlatformBitmap
import kotlin.math.max
import kotlin.math.min

internal suspend fun ImageBitmap.cropWhitespace(
    platformBitmap: PlatformBitmap,
): ImageBitmap {
    val bmp = this
    val black = BLACK_COLOR.toInt()
    val w = bmp.width
    val h = bmp.height
    val row = IntArray(w)

    var top = 0
    run {
        for (y in 0 until h) {
            bmp.readPixels(
                buffer = row,
                startX = 0,
                startY = y,
                width = w,
                height = 1,
                bufferOffset = 0,
                stride = w
            )
            if (row.any { it == black }) {
                top = y
                return@run
            }
        }
        top = 0
    }

    var bottom = h - 1
    run {
        for (y in h - 1 downTo 0) {
            bmp.readPixels(
                buffer = row,
                startX = 0,
                startY = y,
                width = w,
                height = 1,
                bufferOffset = 0,
                stride = w
            )
            if (row.any { it == black }) {
                bottom = y
                return@run
            }
        }
        bottom = h - 1
    }

    val cropTop = max(0, top - 10)
    val cropBottom = min(h - 1, bottom + 10)
    val cropH = max(1, cropBottom - cropTop + 1)

    return platformBitmap.createFromSource(
        source = bmp,
        x = 0,
        y = cropTop,
        width = w,
        height = cropH
    )
}