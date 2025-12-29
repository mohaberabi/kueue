package com.mohaberabi.kline.decoder

import androidx.compose.ui.graphics.ImageBitmap


interface PlatformBitmap {
    suspend fun create(
        width: Int,
        height: Int,
        config: PlatformBitmapConfig = PlatformBitmapConfig.ARGB_8888
    ): ImageBitmap

    suspend fun create(width: Int, height: Int): ImageBitmap

    /**
     * Draws pixels onto an existing bitmap.
     * @param imageBitmap the bitmap that we are currently drawing on (the paper)
     * @param pixels the pixel colors to draw (usually black & white for printers)
     * @param offset the starting index inside the pixels array
     * @param stride how many pixels represent one full row in the pixels array
     * @param x the horizontal position on the bitmap where drawing starts
     * @param y the vertical position on the bitmap where drawing starts
     * @param width how many pixels to draw per row
     * @param height how many rows to draw
     *
     * @return the same bitmap after pixels are applied
     */
    suspend fun setPixelsFor(
        imageBitmap: ImageBitmap,
        pixels: IntArray,
        offset: Int,
        stride: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ): ImageBitmap

    suspend fun createFromSource(
        source: ImageBitmap,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ): ImageBitmap
}