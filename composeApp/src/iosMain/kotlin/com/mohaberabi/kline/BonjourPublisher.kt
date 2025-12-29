package com.mohaberabi.kline

import platform.Foundation.NSNetService
import platform.Foundation.NSNetServiceDelegateProtocol
import platform.darwin.NSObject

class BonjourPublisher(
    private val port: Int
) : NSObject(), NSNetServiceDelegateProtocol {

    private var service: NSNetService? = null

    fun start() {
        val s = NSNetService(
            domain = "local.",
            type = "_kline._tcp.",
            name = "Kline Virtual Printer",
            port = port
        )
        s.delegate = this
        s.publish()
        service = s
        println("✅ Bonjour published _kline._tcp on port=$port")
    }

    fun stop() {
        service?.stop()
        service = null
    }
}