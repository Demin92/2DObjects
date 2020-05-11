package ru.demin.a2dobjects

import android.opengl.GLES32
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.opengl.GLES20
import java.nio.IntBuffer


class CharacterV {
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
    private val program: Int

    init {
        program = createProgram()
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
        GLES32.glUseProgram(program)
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
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, indexes.size, GLES32.GL_UNSIGNED_INT, indexBuffer)
    }

    private fun createVertex(): FloatArray {
        val vertex = listOf(
            //front face
            -2f, 2f, 1f,
            -1f, 2f, 1f,
            0f, 0f, 1f,
            1f, 2f, 1f,
            2f, 2f, 1f,
            0f, -2f, 1f,
            //back face
            -2f, 2f, -1f,
            -1f, 2f, -1f,
            0f, 0f, -1f,
            1f, 2f, -1f,
            2f, 2f, -1f,
            0f, -2f, -1f
        )
        return vertex.toFloatArray()
    }

    private fun createVertexColor(): FloatArray {
        val vertex = listOf(
            //front face
            0f, 0f, 1f, 1f,
            0f, 0f, 1f, 1f,
            0f, 0f, 1f, 1f,
            0f, 0f, 1f, 1f,
            0f, 0f, 1f, 1f,
            0f, 0f, 1f, 1f,
            //back face
            0f, 1f, 1f, 1f,
            0f, 1f, 1f, 1f,
            0f, 1f, 1f, 1f,
            0f, 1f, 1f, 1f,
            0f, 1f, 1f, 1f,
            0f, 1f, 1f, 1f
        )
        return vertex.toFloatArray()
    }

    private fun createIndexArray(): IntArray {
        val index = listOf(
            0, 1, 2, 0, 2, 5, 2, 3, 4, 2, 4, 5,//front face
            6, 7, 8, 6, 8, 11, 8, 9, 10, 8, 10, 11,//back face
            0,1,6,1,6,7,//top left
            3,4,9,4,9,10,//top right
            1,2,7,2,7,8,//left inner
            2,3,8,3,8,9,//right inner
            0,5,6,5,6,11,//left outer
            5,4,11,4,11,10//right outer
        )
        return index.toIntArray()
    }

    companion object {
        private const val COORDS_PER_VERTEX = 3
        private const val COLOR_PER_VERTEX = 4
        private const val BYTES_PER_FLOAT = 4
    }
}