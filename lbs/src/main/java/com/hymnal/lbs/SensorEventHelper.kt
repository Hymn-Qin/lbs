package com.hymnal.lbs

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

import com.amap.api.maps.model.Marker
import kotlin.math.abs


class SensorEventHelper(private val context: Context) : SensorEventListener {

    private val mSensorManager: SensorManager = context
        .getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val mSensor: Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
    private var lastTime: Long = 0
    private val TIME_SENSOR = 100
    var rotation: Float = 0f
        private set
    private var mMarker: Marker? = null

    fun registerSensorListener(listener: SensorEventListener) {
        mSensorManager.registerListener(
            listener, mSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    fun unRegisterSensorListener(listener: SensorEventListener) {
        mSensorManager.unregisterListener(listener, mSensor)
        setCurrentMarker(null)
    }

    fun setCurrentMarker(marker: Marker?) {
        mMarker = marker
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent) {
        if (System.currentTimeMillis() - lastTime < TIME_SENSOR) {
            return
        }
        when (event.sensor.type) {
            Sensor.TYPE_ORIENTATION -> {
                var x = event.values[0]
                x += getScreenRotationOnPhone(context).toFloat()
                x %= 360.0f
                if (x > 180.0f)
                    x -= 360.0f
                else if (x < -180.0f)
                    x += 360.0f

                if (abs(rotation - x) >= 3.0f) {
                    rotation = if (java.lang.Float.isNaN(x)) 0f else x
                    if (mMarker != null) {
                        mMarker!!.rotateAngle = 360 - rotation
                    }
                    lastTime = System.currentTimeMillis()
                }

            }
        }

    }
}
