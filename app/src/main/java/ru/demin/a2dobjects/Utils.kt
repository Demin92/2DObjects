package ru.demin.a2dobjects

import android.opengl.GLES32

fun loadShader(shaderType: Int, shaderCode: String): Int {
    return GLES32.glCreateShader(shaderType).apply {
        GLES32.glShaderSource(this, shaderCode)
        GLES32.glCompileShader(this)
    }
}