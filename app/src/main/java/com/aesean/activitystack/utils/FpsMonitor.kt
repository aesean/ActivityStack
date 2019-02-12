package com.aesean.activitystack.utils

import android.util.Log

class FpsMonitor(val tag: String = "FpsMonitor") {
    private var lastLog = -1L
    private val logInterval = 1000L
    private var sumTime = 0L
    private var start = -1L

    var fps = 0L
        private set

    var fpsInfo = ""
        private set
    var tpf: Float = 0f
        private set

    fun frameStart() {
        if (start != -1L) {
            throw IllegalStateException("Do you forget to call frameEnd? ")
        }
        start = System.currentTimeMillis()
        if (lastLog == -1L) {
            lastLog = start
        }
    }

    fun frameEnd(): String {
        if (start == -1L) {
            throw IllegalStateException("Do you forget to call frameStart? ")
        }
        val end = System.currentTimeMillis()
        sumTime += (end - start)
        fps += 1
        if (end - lastLog >= logInterval) {
            tpf = sumTime * 1f / fps
            fpsInfo = "$fps FPS\t\t${String.format("%.2f", tpf)} ms/f"
            log(fpsInfo)
            sumTime = 0
            fps = 0
            lastLog = end
        }
        start = -1L
        return fpsInfo
    }

    private fun log(s: String) {
        Log.d(tag, s)
    }
}