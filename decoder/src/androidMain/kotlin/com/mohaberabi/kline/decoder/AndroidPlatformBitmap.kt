package com.mohaberabi.kline.decoder

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import com.mohaberabi.kline.decoder.utils.toBitmapConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AndroidPlatformBitmap(
    private val defaultDispatcher: CoroutineDispatcher
) : PlatformBitmap {
    override suspend fun create(
        width: Int,
        height: Int,
        config: PlatformBitmapConfig
    ): ImageBitmap = withContext(defaultDispatcher) {
        createBitmap(
            width = width,
            height = height,
            config = config.toBitmapConfig()
        ).asImageBitmap()
    }

    override suspend fun create(
        width: Int,
        height: Int
    ): ImageBitmap = createBitmap(
        width = width,
        height = height,
        config = Bitmap.Config.ARGB_8888
    ).asImageBitmap()

    override suspend fun setPixelsFor(
        imageBitmap: ImageBitmap,
        pixels: IntArray,
        offset: Int,
        stride: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ): ImageBitmap = withContext(defaultDispatcher) {
        val bitmap = imageBitmap.asAndroidBitmap()
        bitmap.setPixels(pixels, offset, stride, x, y, width, height)
        bitmap.asImageBitmap()
    }

    override suspend fun createFromSource(
        source: ImageBitmap,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ): ImageBitmap = withContext(defaultDispatcher) {
        val sourceBitmap = source.asAndroidBitmap()
        val bitmap = Bitmap.createBitmap(sourceBitmap, x, y, width, height)
        bitmap.asImageBitmap()
    }
}