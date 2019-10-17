package com.hymnal.lbs


import com.amap.api.maps.model.animation.Animation

import java.io.Serializable

/**
 * 位置点
 */
data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val key: String = "$latitude$longitude",
    val name: String = "",
    val address: String = "",
    val type: Int = 0,
    val accuracy: Float = 0f,
    val provider: String? = null,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val satellites: Int = 0,
    val country: String? = null,
    val province: String? = null,
    val city: String? = null,
    val cityCode: String? = null,
    val district: String? = null,
    val adCode: String? = null,
    val poiName: String? = null,
    val time: Long = 0,
    val errorCode: Int = 0,
    val errorInfo: String? = null,
    val locationDetail: String? = null,
    val wifiEnable: Boolean = false,
    val gpsStatus: Int = 0,
    val gpsSateLites: Int = 0,
    val rotation: Float = 0f,
    val animation: Animation? = null
) : Serializable {


    override fun toString() =
        """
            key:$key
            name:$name
            address:$address
            latitude:$latitude
            longitude:$longitude
            rotation:$rotation
            type:$type
            accuracy:$accuracy
            provider:$provider
            speed:$speed
            bearing:$bearing
            satellites:$satellites
            country:$country
            province:$province
            city:$city
            cityCode:$cityCode
            district:$district
            adCode:$adCode
            poiName:$poiName
            time:$time
            errorCode:$errorCode
            errorInfo:$errorInfo
            locationDetail:$locationDetail
        """.trimIndent()
}
