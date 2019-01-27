package com.anwesh.uiprojects.onetwentylineview

/**
 * Created by anweshmishra on 27/01/19.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.content.Context
import android.app.Activity

val nodes : Int = 5
val lines : Int = 4
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val sizeFactor : Float = 2.8f
val strokeFactor : Int = 90
val foreColor : Int = Color.parseColor("#673AB7")
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

fun Canvas.drawOneTwentyLine(i : Int, size : Float, sc1 : Float, sc2 : Float, paint : Paint) {
    save()
    translate(i * size, 0f)
    rotate(120f * (1 - sc2))
    drawLine(0f, 0f, size, 0f, paint)
    restore()
}

fun Canvas.drawOTLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    val xGap : Float = (2 * size) / lines
    save()
    translate(gap * (i + 1), h/2)
    for (j in 0..(lines - 1)) {
        val scj1 : Float = scale.divideScale(0, 2)
        val scj2 : Float = scale.divideScale(1, 2)
        save()
        drawOneTwentyLine(j, xGap, scj1, scj2, paint)
        restore()
    }
    restore()
}