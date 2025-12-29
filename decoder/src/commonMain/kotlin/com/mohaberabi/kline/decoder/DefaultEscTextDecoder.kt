package com.mohaberabi.kline.decoder

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DefaultEscTextDecoder(
    private val defaultDispatcher: CoroutineDispatcher,
) : EscTextDecoder {

    override suspend fun tryToDecodeText(
        bytes: ByteArray,
    ): String = withContext(defaultDispatcher) {

        // We build the final readable text here
        val sb = StringBuilder()

        // Pointer that walks over the raw byte array
        var i = 0

        // Helper: read byte as unsigned (0..255 instead of -128..127)
        fun u8(idx: Int) = bytes[idx].toInt() and 0xFF

        // Walk through ALL bytes one by one
        while (i < bytes.size) {

            // Current byte (unsigned)
            val b = u8(i)

            when (b) {

                // =========================
                // ESC (0x1B) → formatting / control command
                // =========================
                0x1B -> {
                    // ESC always has at least one more byte (the command)
                    if (i + 1 >= bytes.size) break

                    val cmd = u8(i + 1)

                    /*
                     * ESC commands do NOT represent visible text.
                     * They control formatting (bold, align, font, etc).
                     *
                     * So we SKIP them.
                     *
                     * Different ESC commands have different parameter lengths.
                     * We skip exactly how many bytes that command consumes.
                     */
                    i += when (cmd) {
                        0x40 -> 2 // ESC @    → initialize printer
                        0x61 -> 3 // ESC a n  → alignment
                        0x45 -> 3 // ESC E n  → bold on/off
                        0x2D -> 3 // ESC - n  → underline
                        0x21 -> 3 // ESC ! n  → font size/style
                        else -> 2 // Unknown ESC → skip ESC + command only
                    }
                }

                // =========================
                // GS (0x1D) → advanced printer commands
                // =========================
                0x1D -> {
                    if (i + 1 >= bytes.size) break

                    val cmd = u8(i + 1)

                    i += when (cmd) {

                        // -------------------------
                        // GS V → paper cut
                        // -------------------------
                        0x56 -> {
                            /*
                             * GS V 0      → full cut      (3 bytes total)
                             * GS V 41 n   → partial cut  (4 bytes total)
                             *
                             * We skip the whole cut command.
                             */
                            4
                        }

                        // -------------------------
                        // GS v → raster image (bitmap)
                        // -------------------------
                        0x76 -> {
                            /*
                             * GS v 0 m xL xH yL yH d...
                             *
                             * This is IMAGE DATA, not text.
                             * We cannot decode it as text.
                             *
                             * So we skip EVERYTHING after this.
                             */
                            bytes.size
                        }

                        // -------------------------
                        // Any other GS command
                        // -------------------------
                        else -> 2
                    }
                }

                // =========================
                // LF (0x0A) → new line
                // =========================
                0x0A -> {
                    sb.append('\n')
                    i++
                }

                // =========================
                // CR (0x0D) → carriage return
                // =========================
                0x0D -> {
                    // Usually ignored in receipts
                    i++
                }

                // =========================
                // Printable ASCII characters
                // =========================
                in 0x20..0x7E -> {
                    /*
                     * This is REAL visible text.
                     * We convert byte → char and append it.
                     */
                    sb.append(b.toChar())
                    i++
                }

                // =========================
                // Anything else → ignore
                // =========================
                else -> i++
            }
        }

        // Return final human-readable receipt text
        return@withContext sb.toString().trim()
    }
}