package com.mohaberabi.klines.media.model

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle

class IosAppMediaPlayer : AppMediaPlayer {
    private var player: AVAudioPlayer? = null

    @OptIn(ExperimentalForeignApi::class)
    override fun play(source: PlatformMediaResource) {
        ensureSession()
        val url = NSBundle.mainBundle.URLForResource(
            name = source.name,
            withExtension = source.type
        ) ?: return
        player = AVAudioPlayer(contentsOfURL = url, error = null).also { it.play() }
    }

    override fun stop() {
        clear()
    }


    private fun clear() {
        player?.stop()
        player = null
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ensureSession() {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, error = null)
        session.setActive(true, error = null)
    }
}