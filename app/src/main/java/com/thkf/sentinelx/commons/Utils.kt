package com.thkf.sentinelx.commons

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.FILTER_BITMAP_FLAG
import android.net.ConnectivityManager
import android.view.WindowManager
import java.util.*




fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = cm.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}

fun timeDifference(date: Date?): String {

    if(date == null) return ""

    val diff = Date().time - date.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val months = days / 30
    val years = months / 12

    if(years > 0) return "$years year(s) ago"
    if(months > 0) return "$months month(s) ago"
    if(days > 0) return "$days day(s) ago"
    if(hours > 0) return "$hours hour(s) ago"
    if(minutes > 0) return "$minutes minute(s) ago"
    return "$seconds second(s) ago"
}

fun scaleBitmap(bitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
    val scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

    val scaleX = newWidth / bitmap.width.toFloat()
    val scaleY = newHeight / bitmap.height.toFloat()
    val pivotX = 0f
    val pivotY = 0f

    val scaleMatrix = Matrix()
    scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY)

    val canvas = Canvas(scaledBitmap)
    canvas.matrix = scaleMatrix
    canvas.drawBitmap(bitmap, 0f, 0f, Paint(FILTER_BITMAP_FLAG))

    return scaledBitmap
}

fun hideSoftKeyboard(activity: Activity) {
    activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
}

fun getPixelsFromDp(context: Context, dp: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (dp * scale + 0.5f).toInt()
}