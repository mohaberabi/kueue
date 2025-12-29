package com.mohaberabi.kline.decoder

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap
import com.mohaberabi.kline.decoder.utils.BLACK_COLOR
import com.mohaberabi.kline.decoder.utils.WHITE_COLOR
import com.mohaberabi.kline.decoder.utils.cropWhitespace
import com.mohaberabi.kline.decoder.utils.extractGsV0Blocks
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

/**
 * ---------------------
 * This class takes raw ESC/POS printer bytes and tries to extract + decode ONLY
 * the "GS v 0" raster-image command blocks into an ImageBitmap you can show in UI.
 *
 * Think of GS v 0 bytes as:
 *   "Here is a black/white bitmap. Print it."
 *
 * So what we do here is basically:
 *   1) Find all GS v 0 blocks inside the payload
 *   2) Decode each block into an ImageBitmap (pixels)
 *   3) Stitch all blocks vertically (because receipts are long)
 *   4) If result looks almost blank, try inverting (some printers invert)
 *   5) Crop big white margins so the receipt is visible immediately
 *   @property platformBitmap Platform abstraction to create + write pixels (Android Bitmap / iOS&JVM Skia)
 *   @property defaultDispatcher We decode off the UI thread (pixel loops can be heavy)
 *
 */
class DefaultEscGsv0Decoder(
    private val platformBitmap: PlatformBitmap,
    private val defaultDispatcher: CoroutineDispatcher,
) : EscGsv0Decoder {

    /**
     * Decode a full ESC/POS payload into an ImageBitmap (receipt image).
     * We run everything inside defaultDispatcher because:
     * - decoding is CPU work (lots of loops)
     * - we don’t want to block UI / Main thread
     */

    override suspend fun decode(
        bytes: ByteArray,
    ): ImageBitmap? = withContext(defaultDispatcher) {
        // 1) Find all GS v 0 blocks inside the payload.
        // A single print job can contain multiple raster blocks (common).
        val blocks = bytes.extractGsV0Blocks()
        // No GS v 0 found => we cannot render anything
        if (blocks.isEmpty()) return@withContext null
        // 2) Decode blocks normally (non-inverted).
        val decodedNormal =
            blocks.mapNotNull { decodeGsV0Block(it, invert = false, payload = bytes) }
        // Stitch them into one long receipt image.
        val stitchedNormal = stitchVertical(decodedNormal) ?: return@withContext null
        // 3) If the stitched image is suspiciously almost white,
        // it might be an inverted payload (black/white reversed).
        val blackRatio = estimateBlackRatio(stitchedNormal)
        // 4) If it’s almost blank, try decoding inverted.
        val finalBitmap =
            if (blackRatio < 0.001f) {
                val decodedInv =
                    blocks.mapNotNull {
                        decodeGsV0Block(
                            it,
                            invert = true,
                            payload = bytes
                        )
                    }
                stitchVertical(decodedInv) ?: stitchedNormal
            } else {
                stitchedNormal
            }

        return@withContext finalBitmap.cropWhitespace(platformBitmap)

    }

    /**
     * Decode ONE GS v 0 block into an ImageBitmap.
     *
     * GS v 0 format reminder:
     * - width is stored in BYTES (not pixels)
     * - each byte = 8 horizontal pixels (bits)
     *
     * So:
     *   widthPixels = widthBytes * 8
     *
     * Data layout:
     * - The image data is row by row (top -> bottom)
     * - Each row has widthBytes bytes
     * - Each byte has 8 pixels (bit 7 is left-most, bit 0 right-most)
     */
    private suspend fun decodeGsV0Block(
        block: GsV0Block,
        payload: ByteArray,
        invert: Boolean
    ): ImageBitmap? {
        val widthPx = block.widthBytes * 8
        val height = block.height
        // Defensive bounds check:
        // block.dataOffset..block.dataOffset+dataLength must fit in payload.
        val requiredEnd = block.dataOffset + block.dataLength
        if (block.dataOffset < 0 || requiredEnd > payload.size) return null
        // Create an empty bitmap on the platform (Android/iOS) with white background.
        val bmp = platformBitmap.create(widthPx, height, PlatformBitmapConfig.ARGB_8888)
        // We'll build an ARGB pixel array first, then "commit" it into the bitmap.
        // Start white, then paint black dots where bits say "1".
        val pixels = IntArray(widthPx * height) { WHITE_COLOR.toInt() }
        // idx points into the raw ESC/POS payload where this block's image bytes start.
        var idx = block.dataOffset
        // For each row (y):
        for (y in 0 until height) {
            // rowBase is the start index of that row in the pixels array.
            val rowBase = y * widthPx
            // For each byte in the row:
            for (xb in 0 until block.widthBytes) {
                // Read the next byte of raster data (0..255).
                val byte = payload[idx++].toInt() and 0xFF
                // This byte controls 8 pixels horizontally.
                val xBase = xb * 8
                // For each bit (7..0):
                // bit 7 = left-most pixel of the byte.
                for (bit in 0 until 8) {
                    val isBitSet = ((byte shr (7 - bit)) and 1) == 1
                    // If invert=false : 1 means black
                    // If invert=true  : 0 means black (swap)
                    val black = if (!invert) isBitSet else !isBitSet
                    if (black) {
                        pixels[rowBase + xBase + bit] = BLACK_COLOR.toInt()
                    }
                }
            }
        }
        // Write the pixel array into the platform bitmap and return it.
        return platformBitmap.setPixelsFor(
            imageBitmap = bmp,
            pixels = pixels,
            offset = 0,
            stride = widthPx,
            x = 0,
            y = 0,
            width = widthPx,
            height = height
        )
    }

    /**
     * Estimate how "black" the bitmap is.
     *
     * Why?
     * - Some payloads appear inverted (white/black swapped).
     * - If the image is almost all white => likely inverted OR empty/blank.
     *
     * We do sampling instead of scanning all pixels to keep it fast.
     */
    private fun estimateBlackRatio(bmp: ImageBitmap): Float {
        val w = bmp.width
        val h = bmp.height
        val row = IntArray(w)
        var black = 0L
        var total = 0L
        for (y in 0 until h step max(1, h / 200)) {
            bmp.readPixels(
                buffer = row,
                startX = 0,
                startY = y,
                width = w,
                height = 1,
                bufferOffset = 0,
                stride = w
            )
            for (x in 0 until w step max(1, w / 400)) {
                total++
                if (row[x] == BLACK_COLOR.toInt()) black++
            }
        }

        return if (total == 0L) 0f else black.toFloat() / total.toFloat()
    }

    /**
     * Stitch multiple ImageBitmaps vertically into one long receipt.
     *
     * Example:
     *   [block1]
     *   [block2]
     *   [block3]
     *
     * This is needed because ESC/POS often sends the receipt image in multiple
     * GS v 0 blocks (especially when it’s long).
     */
    private suspend fun stitchVertical(bitmaps: List<ImageBitmap>): ImageBitmap? {
        if (bitmaps.isEmpty()) return null
        if (bitmaps.size == 1) return bitmaps.first()
        // Output width = max width of all blocks (some printers pad lines)
        val width = bitmaps.maxOf { it.width }
        // Output height = sum of heights of all blocks
        val height = bitmaps.sumOf { it.height }
        // Create output bitmap
        val out = platformBitmap.create(
            width = width,
            height = height,
            config = PlatformBitmapConfig.ARGB_8888
        )
        // yOffset tells us where to place the next block.
        var yOffset = 0
        // Copy block pixels row-by-row into the output.
        for (b in bitmaps) {
            val row = IntArray(b.width)
            for (yy in 0 until b.height) {
                // Read 1 row from block bitmap
                b.readPixels(
                    buffer = row,
                    startX = 0,
                    startY = yy,
                    width = b.width,
                    height = 1,
                    bufferOffset = 0,
                    stride = b.width
                )
                // Write that row into output bitmap at y = (yOffset + yy)
                platformBitmap.setPixelsFor(
                    imageBitmap = out,
                    pixels = row,
                    offset = 0,
                    stride = b.width,
                    x = 0,
                    y = yOffset + yy,
                    width = b.width,
                    height = 1
                )
            }
            // After finishing this block, next block starts below it.
            yOffset += b.height
        }
        return out
    }

}

