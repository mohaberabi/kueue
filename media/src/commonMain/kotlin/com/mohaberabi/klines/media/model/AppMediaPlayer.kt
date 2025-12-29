package com.mohaberabi.klines.media.model


interface AppMediaPlayer {
    fun play(source: PlatformMediaResource)
    fun stop()
}