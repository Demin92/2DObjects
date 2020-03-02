package ru.demin.a2dobjects

import android.content.Context
import android.opengl.GLSurfaceView

class EllipseView(
    context: Context
) : GLSurfaceView(context) {
    init {
        setEGLContextClientVersion(2)
        setRenderer(EllipseRenderer())
        renderMode = RENDERMODE_WHEN_DIRTY
    }
}