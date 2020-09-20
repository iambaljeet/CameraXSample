package com.app.cameraxsample.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View

private const val TAG = "CameraButton"
class CameraButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var cameraButtonEvents: CameraButtonEvents? = null

    private var isLongPressed = false
    private var mLongPressed: Runnable = Runnable {
        isLongPressed = true
        cameraButtonEvents?.startVideoCapture()
    }
    private val mHandler = Handler()

    init {
        isClickable = true
    }

    private var radiusCameraButton = 0.0f
    private var buttonState = ButtonType.BUTTON_TYPE_IDLE

    private val paintCameraButton = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private enum class ButtonType {
        BUTTON_TYPE_CLICKED, BUTTON_TYPE_IDLE
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        radiusCameraButton = (width.coerceAtMost(height) / 2.0).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val finalRadius = if(buttonState == ButtonType.BUTTON_TYPE_IDLE) radiusCameraButton else (width.coerceAtMost(
            height
        ) / 2.2).toFloat()
        canvas.drawCircle(
            (width / 2).toFloat(),
            (height / 2).toFloat(),
            finalRadius,
            paintCameraButton
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when (event?.action) {
            ACTION_UP -> {
                mHandler.removeCallbacks(mLongPressed)
                if (isLongPressed) {
                    isLongPressed = false
                    cameraButtonEvents?.stopVideoCapture()
                } else {
                    cameraButtonEvents?.capturePicture()
                }
                Log.d(TAG, "ACTION_UP")
                buttonState = ButtonType.BUTTON_TYPE_IDLE
                invalidate()
                true
            }
            ACTION_DOWN -> {
                mHandler.postDelayed(mLongPressed, 800)
                Log.d(TAG, "ACTION_DOWN")
                buttonState = ButtonType.BUTTON_TYPE_CLICKED
                performClick()
                invalidate()
                true
            }
            else -> false
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (context is CameraButtonEvents) {
            cameraButtonEvents = context as CameraButtonEvents
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cameraButtonEvents = null
    }
}

interface CameraButtonEvents {
    fun capturePicture()
    fun startVideoCapture()
    fun stopVideoCapture()
}