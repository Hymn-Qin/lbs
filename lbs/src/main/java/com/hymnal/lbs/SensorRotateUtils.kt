package com.hymnal.lbs

import android.content.Context
import android.view.Surface
import android.view.WindowManager

fun getScreenRotationOnPhone(context: Context): Int {
    val display = (context
        .getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay

    when (display.rotation) {
        Surface.ROTATION_0 -> return 0

        Surface.ROTATION_90 -> return 90

        Surface.ROTATION_180 -> return 180

        Surface.ROTATION_270 -> return -90
    }
    return 0
}