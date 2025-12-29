package com.mohaberabi.kline.decoder

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import com.mohaberabi.kline.decoder.utils.WHITE_COLOR
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo

class SkiaPlatformBitmap(
    private val defaultDispatcher: CoroutineDispatcher
) : PlatformBitmap {
    override suspend fun create(
        width: Int,
        height: Int,
        config: PlatformBitmapConfig
    ): ImageBitmap = withContext(defaultDispatcher) {
        val info = ImageInfo(
            width = width,
            height = height,
            colorType = config.toSkiaColorType(),
            alphaType = ColorAlphaType.PREMUL
        )
        val bytes = ByteArray(width * height * 4)
        fillSolidBGRA(bytes, argb = WHITE_COLOR.toInt())
        val skia = Bitmap()
        skia.installPixels(info = info, pixels = bytes, rowBytes = width * 4)
        skia.asComposeImageBitmap()
    }

    override suspend fun create(
        width: Int,
        height: Int
    ): ImageBitmap = create(width, height, PlatformBitmapConfig.ARGB_8888)

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
        val dst = imageBitmap.asSkiaBitmap()
        val dstInfo = dst.imageInfo
        val rowBytes = dstInfo.width * 4
        val full = dst.readPixels(dstInfo = dstInfo, dstRowBytes = rowBytes, srcX = 0, srcY = 0)
            ?: return@withContext imageBitmap
        for (yy in 0 until height) {
            val srcIndex = offset + yy * stride
            var di = ((y + yy) * dstInfo.width + x) * 4
            for (xx in 0 until width) {
                val c = pixels[srcIndex + xx]
                val a = (c ushr 24) and 0xFF
                val r = (c ushr 16) and 0xFF
                val g = (c ushr 8) and 0xFF
                val b = (c) and 0xFF
                full[di++] = b.toByte()
                full[di++] = g.toByte()
                full[di++] = r.toByte()
                full[di++] = a.toByte()
            }
        }

        dst.installPixels(info = dstInfo, pixels = full, rowBytes = rowBytes)
        dst.asComposeImageBitmap()

    }

    override suspend fun createFromSource(
        source: ImageBitmap,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ): ImageBitmap = withContext(defaultDispatcher) {
        val src = source.asSkiaBitmap()
        val outInfo = ImageInfo(
            width = width,
            height = height,
            colorType = ColorType.BGRA_8888,
            alphaType = ColorAlphaType.PREMUL
        )

        val outBytes = ByteArray(width * height * 4)
        fillSolidBGRA(outBytes, argb = WHITE_COLOR.toInt())
        val oneRowInfo = ImageInfo(
            width = width,
            height = 1,
            colorType = ColorType.BGRA_8888,
            alphaType = ColorAlphaType.PREMUL
        )
        val oneRowRowBytes = width * 4
        for (yy in 0 until height) {
            val row = src.readPixels(
                dstInfo = oneRowInfo,
                dstRowBytes = oneRowRowBytes,
                srcX = x,
                srcY = y + yy
            ) ?: continue

            val dstOff = yy * oneRowRowBytes
            row.copyInto(
                outBytes,
                destinationOffset = dstOff,
                startIndex = 0,
                endIndex = minOf(row.size, oneRowRowBytes)
            )
        }

        val out = Bitmap()
        out.installPixels(info = outInfo, pixels = outBytes, rowBytes = oneRowRowBytes)
        out.asComposeImageBitmap()
    }

    private fun fillSolidBGRA(bytes: ByteArray, argb: Int) {
        val a = (argb ushr 24) and 0xFF
        val r = (argb ushr 16) and 0xFF
        val g = (argb ushr 8) and 0xFF
        val b = (argb) and 0xFF
        var i = 0
        while (i < bytes.size) {
            bytes[i++] = b.toByte()
            bytes[i++] = g.toByte()
            bytes[i++] = r.toByte()
            bytes[i++] = a.toByte()
        }
    }


}


internal fun PlatformBitmapConfig.toSkiaColorType(): ColorType = ColorType.BGRA_8888
