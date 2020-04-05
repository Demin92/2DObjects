package ru.demin.a2dobjects

import android.opengl.GLES32
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.opengl.GLES20
import java.lang.Math.PI
import java.nio.IntBuffer
import kotlin.math.cos
import kotlin.math.sin


class Sphere {
    private val vertexShaderCode = "attribute vec3 aVertexPosition;" +
            "attribute vec4 aVertexColor;" +
            "uniform mat4 uMVPMatrix;" +
            "varying vec4 vColor;" +
            "void main() {" +
            "vColor = aVertexColor;" +
            "gl_Position = uMVPMatrix *vec4(aVertexPosition,1.0);" +
            "}"
    private val fragmentShaderCode =
        "precision mediump float;" +
                "varying vec4 vColor;" +
                "void main() {" +
                "gl_FragColor = vColor;" +
                "}"

    private val vertex = createVertex()
    private val vertexBuffer = ByteBuffer.allocateDirect(vertex.size * BYTES_PER_FLOAT).apply {
        order(ByteOrder.nativeOrder())
    }.asFloatBuffer().apply {
        put(vertex)
        position(0)
    }

    private val vertexColor = createVertexColor()
    private val colorBuffer = ByteBuffer.allocateDirect(vertexColor.size * BYTES_PER_FLOAT).apply {
        order(ByteOrder.nativeOrder())
    }.asFloatBuffer().apply {
        put(vertexColor)
        position(0)
    }

    private val indexes = createIndexArray()
    private val indexBuffer = IntBuffer.allocate(indexes.size).apply {
        put(indexes)
        position(0)
    }

    private val vertexStride = COORDS_PER_VERTEX * BYTES_PER_FLOAT
    private val colorStride = COLOR_PER_VERTEX * BYTES_PER_FLOAT

    private val positionHandle: Int
    private val colorHandle: Int
    private val mVPMatrixHandle: Int

    init {
        val program = createProgram()
        positionHandle = GLES32.glGetAttribLocation(program, "aVertexPosition")
        GLES32.glEnableVertexAttribArray(positionHandle)
        colorHandle = GLES32.glGetAttribLocation(program, "aVertexColor")
        GLES32.glEnableVertexAttribArray(colorHandle)
        mVPMatrixHandle = GLES32.glGetUniformLocation(program, "uMVPMatrix")
    }

    private fun createProgram(): Int {
        val vertexShader = loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode)

        checkShaderProgram(vertexShader, "vertex")
        checkShaderProgram(fragmentShader, "fragment")

        return GLES32.glCreateProgram().apply {
            GLES32.glAttachShader(this, vertexShader)
            GLES32.glAttachShader(this, fragmentShader)
            GLES32.glLinkProgram(this)
            GLES32.glUseProgram(this)
        }
    }

    private fun checkShaderProgram(shader: Int, shaderName: String) {
        val statusVal = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, statusVal, 0)
        if (statusVal[0] == GLES20.GL_FALSE) {
            val statusStr = GLES20.glGetShaderInfoLog(shader)
            Log.d("Povarity", "$shaderName statusStr = $statusStr")
        } else {
            Log.d("Povarity", "$shaderName dobro")
        }
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES32.glUniformMatrix4fv(mVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES32.glVertexAttribPointer(
            positionHandle,
            COORDS_PER_VERTEX,
            GLES32.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )
        GLES32.glVertexAttribPointer(
            colorHandle,
            COLOR_PER_VERTEX,
            GLES32.GL_FLOAT,
            false,
            colorStride,
            colorBuffer
        )
        GLES32.glDrawElements(GLES32.GL_LINES, indexes.size, GLES32.GL_UNSIGNED_INT, indexBuffer)
    }

    private fun createVertex(): FloatArray {
        val vertex = mutableListOf<Float>()
        for (lat in 0..LATITUDE_COUNT) {
            val yAngle = ((PI) / LATITUDE_COUNT) * lat
            for (lon in 0..LONGITUDE_COUNT) {
                val xAngle = ((2 * PI) / LONGITUDE_COUNT) * lon
                vertex.add((RADIUS * cos(xAngle) * sin(yAngle)).toFloat())
                vertex.add(RADIUS * cos(yAngle).toFloat())
                vertex.add((RADIUS * sin(xAngle) * sin(yAngle)).toFloat())
            }
        }
        return vertex.toFloatArray()
    }

    private fun createVertexColor(): FloatArray {
        val vertex = mutableListOf<Float>()
        for (lat in 0..LATITUDE_COUNT) {
            for (lon in 0..LONGITUDE_COUNT) {
                vertex.addAll(
                    listOf(
                        lat * 1f / LATITUDE_COUNT,
                        (LATITUDE_COUNT - lat) * 1f / LATITUDE_COUNT,
                        0f,
                        1f
                    )
                )
            }
        }
        return vertex.toFloatArray()
    }

    private fun createIndexArray(): IntArray {
        val index = mutableListOf<Int>()
        for (lat in 0 until LATITUDE_COUNT) {
            for (lon in 0 until LONGITUDE_COUNT) {
                val p0 = lat * (LONGITUDE_COUNT + 1) + lon
                val p1 = p0 + LONGITUDE_COUNT + 1
                index.addAll(listOf(p0, p1, p0 + 1))
                index.addAll(listOf(p1, p1 + 1, p0 + 1))
            }
        }
        return index.toIntArray()
    }

    companion object {
        private const val COORDS_PER_VERTEX = 3
        private const val COLOR_PER_VERTEX = 4
        private const val BYTES_PER_FLOAT = 4
        private const val LATITUDE_COUNT = 30
        private const val LONGITUDE_COUNT = 30
        private const val RADIUS = 2
    }
}