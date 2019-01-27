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
val delay : Long = 20

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

fun Paint.setStrokeStyle(w : Float, h : Float) {
    strokeWidth = Math.min(w, h) / strokeFactor
    color = foreColor
    strokeCap = Paint.Cap.ROUND
}
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
    paint.setStrokeStyle(w, h)
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

class OneTwentyLineView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, lines, lines)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class OTLNode(var i : Int = 0, val state : State = State()) {

        private var next : OTLNode? = null
        private var prev : OTLNode? = null

        init {

        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = OTLNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawOTLNode(i, state.scale, paint)
            prev?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : OTLNode {
            var curr : OTLNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class OneTwentyLine(var i : Int) {

        private var curr : OTLNode = OTLNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : OneTwentyLineView) {

        private val animator : Animator = Animator(view)
        private val otl : OneTwentyLine = OneTwentyLine(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            otl.draw(canvas, paint)
            animator.animate {
                otl.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            otl.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : OneTwentyLineView {
            val view : OneTwentyLineView = OneTwentyLineView(activity)
            activity.setContentView(view)
            return view 
        }
    }
}