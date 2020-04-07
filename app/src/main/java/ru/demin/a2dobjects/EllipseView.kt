package ru.demin.a2dobjects

import android.content.Context
import android.opengl.GLSurfaceView
import java.util.Timer
import java.util.TimerTask

class EllipseView(
    context: Context
) : GLSurfaceView(context) {
    private val renderer = EllipseRenderer()

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        runRotation()
    }

    private fun runRotation() {
        val timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                renderer.rotation.randStep()
                this@EllipseView.invalidate()
            }
        }
        timer.scheduleAtFixedRate(task, 100, 16)
    }
}