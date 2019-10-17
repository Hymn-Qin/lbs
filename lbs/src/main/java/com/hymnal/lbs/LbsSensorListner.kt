package com.hymnal.lbs


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.abs

abstract class LbsSensorListner(private val context: Context) : SensorEventListener {

    private var lastTime: Long = 0
    private val TIME_SENSOR = 100
    private var mAngle: Float = 0f

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

                if (abs(mAngle - x) >= 3.0f) {
                    mAngle = if (java.lang.Float.isNaN(x)) 0f else x
                    onSensorLisner(360 - mAngle)
                    lastTime = System.currentTimeMillis()
                }

            }
        }

    }

    abstract fun onSensorLisner(v: Float)
}
