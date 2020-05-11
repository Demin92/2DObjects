package ru.demin.a2dobjects

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import java.util.Timer
import java.util.TimerTask

class EllipseView(
    context: Context
) : GLSurfaceView(context) {
    private val renderer = EllipseRenderer()

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
//        runRotation()
        setupTouchControl()
    }

    private fun runRotation() {
        val timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                renderer.rotation.randStep()
                this@EllipseView.requestRender()
            }
        }
        timer.scheduleAtFixedRate(task, 100, 16)
    }

    private fun setupTouchControl() {
        var previousX = 0f
        var previousY = 0f
        setOnTouchListener { _, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    previousX = event.x
                    previousY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = previousX  - event.x
                    val dy = previousY  - event.y
                    renderer.rotation.xAngle -= (dy * TOUCH_SCALE_FACTOR)
                    renderer.rotation.yAngle -= (dx * TOUCH_SCALE_FACTOR)
                    this@EllipseView.requestRender()
                    previousX = event.x
                    previousY = event.y
                }
            }
            true
        }

    }

    companion object {
        private const val TOUCH_SCALE_FACTOR = 0.2f
    }
}