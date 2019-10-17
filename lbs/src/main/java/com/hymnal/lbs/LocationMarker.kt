package com.hymnal.lbs

import android.graphics.Bitmap

data class LocationMarker(
    val key: String,
    val latitude: Double,
    val longitude: Double,
    val icon: Bitmap? = null,
    val animTime: Int = 0,
    val rotate: Float = 0f
) {
}
