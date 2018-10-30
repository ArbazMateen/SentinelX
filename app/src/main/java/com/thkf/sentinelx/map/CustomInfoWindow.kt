package com.thkf.sentinelx.map

import android.content.Context
import android.graphics.Point
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker


class MapWrapperLayout : RelativeLayout {

    private var map: GoogleMap? = null
    private var bottomOffsetPixels: Int = 0
    private var marker: Marker? = null
    private var infoWindow: View? = null

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs, defStyle)

    fun init(map: GoogleMap, bottomOffsetPixels: Int) {
        this.map = map
        this.bottomOffsetPixels = bottomOffsetPixels
    }

    fun setMarkerWithInfoWindow(marker: Marker, infoWindow: View) {
        this.marker = marker
        this.infoWindow = infoWindow
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        var ret = false
        if (marker != null && marker?.isInfoWindowShown!! && map != null && infoWindow != null) {
            val point: Point = map?.projection!!.toScreenLocation(marker?.position)
            val copyEv = MotionEvent.obtain(ev)
            copyEv.offsetLocation(
                    (-point.x + infoWindow?.width!! / 2).toFloat(),
                    (-point.y + infoWindow?.height!! + bottomOffsetPixels).toFloat())

            ret = infoWindow?.dispatchTouchEvent(copyEv)!!
        }
        return ret || super.dispatchTouchEvent(ev)
    }

}


abstract class OnInfoWindowElemTouchListener(private val view: View) : View.OnTouchListener {
    private val handler = Handler()

    private var marker: Marker? = null
    private var pressed = false

    private val confirmClickRunnable = Runnable {
        if (endPress()) {
            onClickConfirmed(view, marker)
        }
    }

    fun setMarker(marker: Marker) {
        this.marker = marker
    }

    override fun onTouch(vv: View, event: MotionEvent): Boolean {
        if (0 <= event.x && event.x <= view.width && 0 <= event.y && event.y <= view.height) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> startPress()
                MotionEvent.ACTION_UP -> handler.postDelayed(confirmClickRunnable, 150)
                MotionEvent.ACTION_CANCEL -> endPress()
                else -> {
                }
            }
        } else {
            endPress()
        }
        return false
    }

    private fun startPress() {
        if (!pressed) {
            pressed = true
            handler.removeCallbacks(confirmClickRunnable)
            if (marker != null)
                marker!!.showInfoWindow()
        }
    }

    private fun endPress(): Boolean {
        return if (pressed) {
            this.pressed = false
            handler.removeCallbacks(confirmClickRunnable)
            if (marker != null)
                marker!!.showInfoWindow()
            true
        } else
            false
    }

    /**
     * This is called after a successful click
     */
    protected abstract fun onClickConfirmed(v: View, marker: Marker?)
}