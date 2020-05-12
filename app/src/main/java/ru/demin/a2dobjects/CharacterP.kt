package ru.demin.a2dobjects

import android.opengl.GLES32
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.opengl.GLES20
import java.lang.Math.pow
import java.nio.IntBuffer
import java.util.Collections.addAll
import kotlin.math.pow

class CharacterP(private val koef: Float = 1f) {
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

    private val vertexColor = createVertexColor(vertex.size / COORDS_PER_VERTEX)
    private val colorBuffer = ByteBuffer.allocateDirect(vertexColor.size * BYTES_PER_FLOAT).apply {
        order(ByteOrder.nativeOrder())
    }.asFloatBuffer().apply {
        put(vertexColor)
        position(0)
    }

    private val indexes = createIndexArray(vertex.size / COORDS_PER_VERTEX)
    private val indexBuffer = IntBuffer.allocate(indexes.size).apply {
        put(indexes)
        position(0)
    }

    private val vertexStride = COORDS_PER_VERTEX * BYTES_PER_FLOAT
    private val colorStride = COLOR_PER_VERTEX * BYTES_PER_FLOAT

    private val positionHandle: Int
    private val colorHandle: Int
    private val mVPMatrixHandle: Int
    private val program = createProgram()

    init {
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
//        GLES32.glDrawElements(GLES32.GL_LINES, indexes.size, GLES32.GL_UNSIGNED_INT, indexBuffer)
//        GLES32.glDrawArrays(GLES32.GL_LINES, 0, vertex.size / COORDS_PER_VERTEX)

    }

    private fun createVertex(): FloatArray {
        val vertex = mutableListOf<Float>()
        vertex.apply {
            addAll(createVertex(1f))
            addAll(createVertex(-1f))
        }
        return vertex.map { it*koef }.toFloatArray()
    }

    private fun createVertex(z: Float): List<Float> {
        val vertex = mutableListOf<Float>()

        val innerControlPoint = listOf(
            0f, 1f,
            0.8f, 1.66f,
            0.8f, 2.33f,
            0f, 3f
        )
        val innerCurve = createBezierCurve(innerControlPoint, z)

        val outerControlPoints = listOf(
            1f, 0f,
            1.66f, 1f,
            1.66f, 3f,
            1f, 4f
        )
        val outerCurve = createBezierCurve(outerControlPoints, z)

        vertex.run {
            addAll(
                listOf(
                    0f, 0f, z,
                    0f, -2f, z,
                    -1f, -2f, z,
                    -1f, 4f, z
                )
            )
            addAll(outerCurve)
            addAll(innerCurve)
        }
        return vertex
    }

    private fun createBezierCurve(controlPoints: List<Float>, z: Float): List<Float> {
        val points = mutableListOf<Float>()
        val stepSize = 1.0 / BEZIER_CURVE_STEPS_COUNT
        var centroidX = 0f
        var centroidY = 0f
        controlPoints.forEachIndexed { index, value ->
            if (index % 2 == 0) centroidX += value else centroidY += value
        }

        for (t in 0..BEZIER_CURVE_STEPS_COUNT) {
            val cur = t * stepSize

            val x = controlPoints[0] * (1 - cur).pow(3.0) + controlPoints[2] * 3 * cur * (1 - cur).pow(2.0) +
                    controlPoints[4] * 3 * cur * cur * (1 - cur) + controlPoints[6] * cur.pow(3.0)

            val y = controlPoints[1] * (1 - cur).pow(3.0) + controlPoints[3] * 3 * cur * (1 - cur).pow(2.0) +
                    controlPoints[5] * 3 * cur * cur * (1 - cur) + controlPoints[7] * cur.pow(3.0)

            points.add(x.toFloat())
            points.add(y.toFloat())
            points.add(z)
        }
        return points
    }

    private fun createVertexColor(vertexSize: Int): FloatArray {
        val vertex = mutableListOf<Float>()
        for (i in 0 until vertexSize / 2) {
            vertex.addAll(listOf(0f, 0f, 1f, 1f))
        }
        for (i in 0 until vertexSize / 2) {
            vertex.addAll(listOf(0f, 1f, 1f, 1f))
        }
        return vertex.toFloatArray()
    }

    private fun createSurfaceIndexArray(offset: Int): List<Int> {
        val index = mutableListOf<Int>()
        val count = BEZIER_CURVE_STEPS_COUNT + 1
        for (i in 0 until BEZIER_CURVE_STEPS_COUNT) {
            index.addAll(listOf(i + 4, i + 4 + count, i + 4 + count + 1))
            index.addAll(listOf(i + 4, i + 4 + count + 1, i + 4 + 1))
        }

        index.addAll(
            listOf(
                0, 4, count + 4,
                3, 4 + BEZIER_CURVE_STEPS_COUNT, 4 + count + BEZIER_CURVE_STEPS_COUNT,
                3, 1, 4 + BEZIER_CURVE_STEPS_COUNT + count,
                1, 2, 3
            )
        )
        val result = index.map { it + offset }
        return result
    }

    private fun createIndexArray(vertexSize: Int): IntArray {
        val index = mutableListOf<Int>().apply {
            val step = vertexSize / 2

            addAll(createSurfaceIndexArray(0))
            addAll(createSurfaceIndexArray(step))

            for (i in 4 until BEZIER_CURVE_STEPS_COUNT + 4) {
                addAll(addSurface(i, i + 1))
            }

            for (i in 4 + BEZIER_CURVE_STEPS_COUNT + 1 until 2 * BEZIER_CURVE_STEPS_COUNT + 4 + 1) {
                addAll(addSurface(i, i + 1))
            }
            addAll(addSurface(4 + BEZIER_CURVE_STEPS_COUNT + 1, 4 + 2 * BEZIER_CURVE_STEPS_COUNT + 1))
            addAll(addSurface(0, 4))
            addAll(addSurface(0, 1))
            addAll(addSurface(1, 2))
            addAll(addSurface(2, 3))
            addAll(addSurface(3, 4 + BEZIER_CURVE_STEPS_COUNT))
        }

        return index.toIntArray()
    }

    private fun addSurface(start: Int, end: Int): List<Int> {
        val step = vertex.size / COORDS_PER_VERTEX / 2
        return mutableListOf<Int>().apply {
            addAll(listOf(start, end, start + step))
            addAll(listOf(end, start + step, end + step))
        }
    }

    companion object {
        private const val COORDS_PER_VERTEX = 3
        private const val COLOR_PER_VERTEX = 4
        private const val BYTES_PER_FLOAT = 4
        private const val BEZIER_CURVE_STEPS_COUNT = 100
    }
}