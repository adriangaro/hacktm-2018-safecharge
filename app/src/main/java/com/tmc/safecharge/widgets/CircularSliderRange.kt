package com.tmc.safecharge.widgets


import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.tmc.safecharge.R

class CircularSliderRange : View {

    var thumbStartX: Int = 0
    var thumbStartY: Int = 0

    var thumbEndX: Int = 0
    var thumbEndY: Int = 0

    var circleCenterX: Int = 0
    var circleCenterY: Int = 0
    var circleRadius: Int = 0

    var padding: Int = 0
    var thumbSize: Int = 0
    var drawThumbSize: Int = 0

    var startThumbColor: Int = 0
    var endThumbColor: Int = 0
    var borderColor: Int = 0
    var borderThickness: Int = 0
    var arcDashSize: Int = 0
    var arcColor: Int = 0

    var offsetAngle: Double = 0.0
    
    var startAngle: Double = 0.0
        set(startAngle) {
            field = if(startAngle > borderStartAngle) startAngle else borderStartAngle
        }
    var endAngle: Double = 0.0
        set(endAngle) {
            field = if(endAngle < borderEndAngle) endAngle else borderEndAngle
        }

    var borderStartAngle: Double = 0.0
        set(startAngle) {
            field = if(startAngle < 0) 0.0 else startAngle
        }
    var borderEndAngle: Double = 0.0
        set(endAngle) {
            field = if(endAngle < 0) 0.0 else endAngle
        }

    var isThumbSelected = false
    var isThumbEndSelected = false

    private val paint = Paint()
    private val linePaint = Paint()
    private val arcRectF = RectF()
    private val arcRect = Rect()
    private val borderArcRectF = RectF()
    private val borderArcRect = Rect()
    var onSliderRangeMovedListener: OnSliderRangeMovedListener? = null


    /**
     * Listener interface used to detect when slider oves around.
     */
    interface OnSliderRangeMovedListener {

        /**
         * This ethod is invoked when start thumb is oved, providing position of the start slider thumb.
         *
         * @param pos Value between 0 and 1 representing the current startAngle.<br></br>
         * `pos = (Angle - StartingAngle) / (2 * Pi)`
         */
        fun onStartSliderMoved(pos: Double)

        /**
         * This ethod is invoked when end thumb is oved, providing position of the end slider thumb.
         *
         * @param pos Value between 0 and 1 representing the current startAngle.<br></br>
         * `pos = (Angle - StartingAngle) / (2 * Pi)`
         */
        fun onEndSliderMoved(pos: Double)

        /**
         * This ethod is invoked when start slider is pressed/released.
         *
         * @param event Event represent state of the slider, it can be in two states: Pressed or Released.
         */
        fun onStartSliderEvent(event: ThumbEvent)

        /**
         * This ethod is invoked when end slider is pressed/released.
         *
         * @param event Event represent state of the slider, it can be in two states: Pressed or Released.
         */
        fun onEndSliderEvent(event: ThumbEvent)
    }

    private enum class Thumb {
        START, END
    }

    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr)
    }

    // common initializer method
    @SuppressLint("ObsoleteSdkInt")
    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircularSliderRange, defStyleAttr, 0)

        // read all available attributes
        offsetAngle = a.getFloat(R.styleable.CircularSliderRange_offset_angle, 0f).toDouble()
        borderStartAngle = a.getFloat(R.styleable.CircularSliderRange_border_start_angle, 0f).toDouble()
        borderEndAngle = a.getFloat(R.styleable.CircularSliderRange_border_end_angle, 360f).toDouble()
        startAngle = a.getFloat(R.styleable.CircularSliderRange_start_angle, 60f).toDouble()
        endAngle = a.getFloat(R.styleable.CircularSliderRange_end_angle, 90f).toDouble()
        thumbSize = a.getDimensionPixelSize(R.styleable.CircularSliderRange_thumb_size, 50)
        drawThumbSize = a.getDimensionPixelSize(R.styleable.CircularSliderRange_draw_thumb_size, thumbSize)
        startThumbColor = a.getColor(R.styleable.CircularSliderRange_start_thumb_color, Color.GRAY)
        endThumbColor = a.getColor(R.styleable.CircularSliderRange_end_thumb_color, Color.GRAY)
        borderThickness = a.getDimensionPixelSize(R.styleable.CircularSliderRange_border_thickness, 20)
        arcDashSize = a.getDimensionPixelSize(R.styleable.CircularSliderRange_arc_dash_size, 60)
        arcColor = a.getColor(R.styleable.CircularSliderRange_arc_color, 0)
        borderColor = a.getColor(R.styleable.CircularSliderRange_border_color, Color.RED)

        // assign padding - check for version because of RTL layout compatibility
        padding = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val all = paddingLeft + paddingRight + paddingBottom + paddingTop + paddingEnd + paddingStart
            all / 6
        } else {
            (paddingLeft + paddingRight + paddingBottom + paddingTop) / 4
        }
        a.recycle()

        if (isInEditMode)
            return
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
    /* ***** Setters ***** */

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // use smaller dimension for calculations (depends on parent size)

        // save circle coordinates and radius in fields
        circleCenterX = w / 2
        circleCenterY = w / 2
        circleRadius = w / 2 - padding - drawThumbSize / 2

        // works well for now, should we call something else here?
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.rotate(offsetAngle.toFloat(), circleCenterX.toFloat(), circleCenterY.toFloat())
        // outer circle (ring)
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderThickness.toFloat()
        paint.isAntiAlias = true
        borderArcRect.set(circleCenterX - circleRadius, circleCenterY + circleRadius, circleCenterX + circleRadius, circleCenterY - circleRadius)
        borderArcRectF.set(borderArcRect)
        borderArcRectF.sort()

        canvas.drawArc(borderArcRectF, borderStartAngle.toFloat(), ((360 + borderEndAngle - borderStartAngle) % 360).toFloat(), false, paint)

        // find thumb start position
        thumbStartX = (circleCenterX + circleRadius * Math.cos(fromDrawingAngle(startAngle))).toInt()
        thumbStartY = (circleCenterY - circleRadius * Math.sin(fromDrawingAngle(startAngle))).toInt()

        //find thumb end position
        thumbEndX = (circleCenterX + circleRadius * Math.cos(fromDrawingAngle(endAngle))).toInt()
        thumbEndY = (circleCenterY - circleRadius * Math.sin(fromDrawingAngle(endAngle))).toInt()

        linePaint.color = if (arcColor == 0) Color.RED else arcColor
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = arcDashSize.toFloat()
        linePaint.isAntiAlias = true
        linePaint.textSize = 50f

        arcRect.set(circleCenterX - circleRadius, circleCenterY + circleRadius, circleCenterX + circleRadius, circleCenterY - circleRadius)
        arcRectF.set(arcRect)
        arcRectF.sort()


        canvas.drawArc(arcRectF, startAngle.toFloat(), ((360 + endAngle - startAngle) % 360).toFloat(), false, linePaint)

            // draw colored circle
        paint.color = startThumbColor
        paint.style = Paint.Style.FILL
        canvas.drawCircle(thumbStartX.toFloat(), thumbStartY.toFloat(), (drawThumbSize / 2).toFloat(), paint)

        paint.style = Paint.Style.FILL
        paint.color = endThumbColor
        canvas.drawCircle(thumbEndX.toFloat(), thumbEndY.toFloat(), (drawThumbSize / 2).toFloat(), paint)


    }

    /**
     * Invoked when slider starts oving or is currently oving. This ethod calculates and sets position and startAngle of the thumb.
     *
     * @param touchX Where is the touch identifier now on X axis
     * @param touchY Where is the touch identifier now on Y axis
     */
    private fun updateSliderState(touchX: Int, touchY: Int, thumb: Thumb) {
        val distanceX = touchX - circleCenterX
        val distanceY = circleCenterY - touchY

        val c = Math.sqrt(Math.pow(distanceX.toDouble(), 2.0) + Math.pow(distanceY.toDouble(), 2.0))
        var angle = Math.acos(distanceX / c)
        if (distanceY < 0)
            angle = -angle

        angle = toDrawingAngle(angle)
        var perc = 0.0
        if (thumb == Thumb.START) {
            startAngle = angle
            if(startAngle > endAngle) {
                startAngle = borderStartAngle
            }
            perc = (startAngle - borderStartAngle) / (borderEndAngle - borderStartAngle)
        } else {
            endAngle = angle
            if(startAngle > endAngle) {
                endAngle = borderEndAngle
            }
            perc = (endAngle - borderStartAngle) / (borderEndAngle - borderStartAngle)
        }

        if (onSliderRangeMovedListener != null) {
            if (thumb == Thumb.START) {
                onSliderRangeMovedListener!!.onStartSliderMoved(perc)
            } else {
                onSliderRangeMovedListener!!.onEndSliderMoved(perc)
            }
        }
    }

    private fun toDrawingAngle(angleInRadians: Double): Double {
        var fixedAngle = Math.toDegrees(angleInRadians)
        fixedAngle = if (angleInRadians > 0)
            360 - fixedAngle
        else
            -fixedAngle
        return fixedAngle
    }

    private fun fromDrawingAngle(angleInDegrees: Double): Double {
        val radians = Math.toRadians(angleInDegrees)
        return -radians
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val angle = Math.toRadians(offsetAngle)

        val x1 = ev.x.toInt() - circleCenterX
        val y1 = ev.y.toInt() - circleCenterY

        val x2 = x1 * Math.cos(angle) - y1 * Math.sin(angle)
        val y2 = x1 * Math.sin(angle) + y1 * Math.cos(angle)

        val x = (x2 + circleCenterX).toInt()
        val y = (y2 + circleCenterY).toInt()

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                // start oving the thumb (this is the first touch)
                val isThumbStartPressed = (x < thumbStartX + thumbSize
                        && x > thumbStartX - thumbSize
                        && y < thumbStartY + thumbSize
                        && y > thumbStartY - thumbSize)

                val isThumbEndPressed = (x < thumbEndX + thumbSize
                        && x > thumbEndX - thumbSize
                        && y < thumbEndY + thumbSize
                        && y > thumbEndY - thumbSize)

                if (isThumbStartPressed) {
                    isThumbSelected = true
                    updateSliderState(x, y, Thumb.START)
                } else if (isThumbEndPressed) {
                    isThumbEndSelected = true
                    updateSliderState(x, y, Thumb.END)
                }

                if (onSliderRangeMovedListener != null) {
                    if (isThumbSelected)
                        onSliderRangeMovedListener!!.onStartSliderEvent(ThumbEvent.THUMB_PRESSED)
                    if (isThumbEndSelected)
                        onSliderRangeMovedListener!!.onEndSliderEvent(ThumbEvent.THUMB_PRESSED)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                // still oving the thumb (this is not the first touch)
                if (isThumbSelected) {
                    updateSliderState(x, y, Thumb.START)
                } else if (isThumbEndSelected) {
                    updateSliderState(x, y, Thumb.END)
                }
            }

            MotionEvent.ACTION_UP -> {
                if (onSliderRangeMovedListener != null) {
                    if (isThumbSelected)
                        onSliderRangeMovedListener!!.onStartSliderEvent(ThumbEvent.THUMB_RELEASED)
                    if (isThumbEndSelected)
                        onSliderRangeMovedListener!!.onEndSliderEvent(ThumbEvent.THUMB_RELEASED)
                }

                // finished oving (this is the last touch)
                isThumbSelected = false
                isThumbEndSelected = false
            }
        }

        invalidate()
        return true
    }

    fun min(): Double {
        return (startAngle - borderStartAngle) / (borderEndAngle - borderStartAngle)
    }

    fun max(): Double {
        return (endAngle - borderStartAngle) / (borderEndAngle - borderStartAngle)
    }

    companion object {
        private const val THUMB_SIZE_NOT_DEFINED = -1
    }

}
