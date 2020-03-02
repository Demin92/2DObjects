package ru.demin.a2dobjects

import android.opengl.GLES32
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import android.opengl.GLES20


class Ellipse {
    private val vertexShaderCode = "attribute vec3 aVertexPosition;" +
            "uniform mat4 uMVPMatrix;" +
            "varying vec4 vColor;" +
            "void main() {" +
            "gl_Position = uMVPMatrix *vec4(aVertexPosition,1.0);" +
            "if (aVertexPosition.z == 1.0) { vColor = vec4(0.5,0.0,0.0,1.0); } else { vColor = vec4(0.0,1.0,0.0,1.0); };" +
            "}"
    private val fragmentShaderCode =
        "precision mediump float;" +
                "varying vec4 vColor; " +
                "void main() {" +
                "gl_FragColor = vColor;" +
                "}"

    private val solidVertex = createVertex(true, 1f)
    private val borderVertex = createVertex(false, 1.0001f)
    private val vertex = solidVertex + borderVertex

    private val vertexBuffer = ByteBuffer.allocateDirect(vertex.size * BYTES_PER_FLOAT).apply {
        order(ByteOrder.nativeOrder())
    }.asFloatBuffer().apply {
        put(vertex)
        position(0)
    }

    private val vertexStride = COORDS_PER_VERTEX * BYTES_PER_FLOAT

    private val positionHandle: Int
    private val mVPMatrixHandle: Int

    init {
        val program = createProgram()
        positionHandle = GLES32.glGetAttribLocation(program, "aVertexPosition")
        GLES32.glEnableVertexAttribArray(positionHandle)
        mVPMatrixHandle = GLES32.glGetUniformLocation(program, "uMVPMatrix")
    }

    private fun createProgram(): Int {
        val vertexShader = loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode)

        val statusVal = IntArray(1)
        GLES20.glGetShaderiv(vertexShader, GLES20.GL_COMPILE_STATUS, statusVal, 0)
        if (statusVal[0] == GLES20.GL_FALSE) {
            val statusStr = GLES20.glGetShaderInfoLog(vertexShader)
            Log.d("Povarity", "statusStr = $statusStr")
        }

        return GLES32.glCreateProgram().apply {
            GLES32.glAttachShader(this, vertexShader)
            GLES32.glAttachShader(this, fragmentShader)
            GLES32.glLinkProgram(this)
            GLES32.glUseProgram(this)
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
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 0, solidVertex.size / COORDS_PER_VERTEX)
        GLES32.glDrawArrays(GLES32.GL_LINE_LOOP, solidVertex.size / COORDS_PER_VERTEX, borderVertex.size / COORDS_PER_VERTEX)
    }

    private fun createVertex(isCenterAdded: Boolean, z: Float): FloatArray {
        val vertex = mutableListOf<Float>().apply {
            if (isCenterAdded) {
                add(0f)
                add(0f)
                add(z)
            }
        }
        for (i in 0 until TRIANGLES_COUNT + 1) {
            val angle = i * (2 * PI / TRIANGLES_COUNT)
            val sin = sin(angle)
            val cos = cos(angle)
            val mult = (A * B) / sqrt((sin * A).pow(2.0) + (cos * B).pow(2.0)).toFloat()

            vertex.add(mult * cos.toFloat())
            vertex.add(mult * sin.toFloat())
            vertex.add(z)

        }
        return vertex.toFloatArray()
    }

    companion object {
        private const val COORDS_PER_VERTEX = 3
        private const val BYTES_PER_FLOAT = 4
        private const val TRIANGLES_COUNT = 100
        private const val A = 1.5f
        private const val B = 2.3f
    }
}