package com.mohaberabi.kline.media

import android.content.Context
import android.media.MediaPlayer
import com.mohaberabi.klines.media.model.AppMediaPlayer
import com.mohaberabi.klines.media.model.PlatformMediaResource

class AndroidAppMediaPlayer(
    private val context: Context
) : AppMediaPlayer {
    private var mediaPlayer: MediaPlayer? = null


    override fun play(source: PlatformMediaResource) {
        clear()
        mediaPlayer = MediaPlayer.create(context, source.resourceId)
        mediaPlayer?.start()
    }

    override fun stop() {
        clear()
    }

    private fun clear() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }


}