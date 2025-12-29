package com.mohaberabi.kline.features.home.viewmodel

import com.mohaberabi.kline.decoder.EscGsv0Decoder
import com.mohaberabi.kline.decoder.EscTextDecoder
import com.mohaberabi.kline.decoder.utils.containsRaster


class HandleReceivedPrintJobUseCase(
    private val escTextDecoder: EscTextDecoder,
    private val escGsv0Decoder: EscGsv0Decoder,
) {


    suspend operator fun invoke(
        receivedBytes: ByteArray
    ): PrintJobContent {
        val containsRaster = receivedBytes.containsRaster()
        return if (containsRaster) {
            val bitmap = escGsv0Decoder.decode(receivedBytes) ?: return PrintJobContent.None
            PrintJobContent.RasterImage(bitmap)
        } else {
            val text = escTextDecoder.tryToDecodeText(receivedBytes)
            PrintJobContent.PLainText(text)
        }

    }
}