package com.mohaberabi.klines.media.model

import java.io.BufferedInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

class JvmAppAudiPlayer : AppMediaPlayer {
    private var clip: Clip? = null
    override fun play(source: PlatformMediaResource) {
        stop()
        val stream = BufferedInputStream(
            javaClass.getResourceAsStream(source.path) ?: return
        )
        val audioInput = AudioSystem.getAudioInputStream(stream)
        clip = AudioSystem.getClip().apply {
            open(audioInput)
            start()
        }
    }

    override fun stop() {
        clip?.stop()
        clip?.close()
        clip = null
    }
}