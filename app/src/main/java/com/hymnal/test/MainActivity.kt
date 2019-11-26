package com.hymnal.test

import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.model.AMapGestureListener
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.Marker
import com.hymnal.lbs.LocationInfo
import com.hymnal.lbs.LocationMarker
import com.hymnal.lbs.gaode.GaodeService
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), AMap.InfoWindowAdapter, AMap.OnCameraChangeListener {


    private lateinit var iMapService: GaodeService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        iMapService = GaodeService(this)
        iMapService.onCreate(savedInstanceState)
        map.addView(iMapService.map)
        `in`.setOnClickListener {
            iMapService.zoomInAndOut(true)
            val textView = TextView(applicationContext)

            textView.setText("吉利研究院")

            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f)

            textView.setTextColor(Color.BLACK)

            textView.setBackgroundResource(R.drawable.bg)
//        iMapService.updateMarkers(LocationMarker("2", 30.336883, 121.244685, start1Bitmap, rotate = 0f, animTime = 1))
            iMapService.addInfoWindowMarker(
                LocationInfo(
                    30.336883, 121.454685,
                    "1231", "1231", address = "123456",
                    rotation = 0f
                ), textView, PointF(0.5f, -0.3f)
            )
        }
        out.setOnClickListener {
            iMapService.zoomInAndOut(false)
            iMapService.clearAllInfoMarker()
        }
    }

    override fun onResume() {
        super.onResume()
        iMapService.moveCamera(
            LocationInfo(
                30.336883, 121.244685,
                "12", "12",
                rotation = 0f
            )
        )
        val startBitmap = resources.bitmap {
            source = R.mipmap.point_a
            scale = 1.1f
        }

        val textView = TextView(applicationContext)

        textView.setText("吉利研究院")

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f)

        textView.setTextColor(Color.BLACK)

        textView.setBackgroundResource(R.drawable.bg)
        iMapService.updateMarkers(LocationMarker("1", 30.336883, 121.244685, startBitmap, rotate = 0f, animTime = 1))
//        iMapService.updateMarkers(LocationMarker("2", 30.336883, 121.244685, start1Bitmap, rotate = 0f, animTime = 1))
        iMapService.addInfoWindowMarker(
            LocationInfo(
                30.336883, 121.254685,
                "1231", "1231", address = "123456",
                rotation = 0f
            ), textView, PointF(0.5f, -0.3f)
        )

        val textView1 = TextView(applicationContext)

        textView1.setText("吉利研究院吉利研究院吉利研究院吉利研究院吉利研究院")
        textView1.width = 40

        textView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f)

        textView1.setTextColor(Color.BLACK)

        textView1.setBackgroundResource(R.drawable.bg)

        iMapService.addInfoWindowMarker(
            LocationInfo(
                30.336883, 121.144685,
                "1234", "1234", address = "123456",
                rotation = 0f
            ), textView1, PointF(0.5f, -0.3f)
        )


        iMapService.addInfoWindowMarker(
            LocationInfo(
                30.336883, 121.254685,
                "吉利汽车", "吉利汽车", address = "123456",
                rotation = 0f
            ), startBitmap, PointF(0.5f, 0.5f)
        )

        val start1Bitmap=
            resources.bitmap {
                source = R.mipmap.location_a
                scale = 1.01f
            }

        iMapService.addInfoWindowMarker(
            LocationInfo(
                30.336883, 121.254685,
                "吉利汽车1", "吉利汽车1", address = "123456",
                rotation = 0f
            ), start1Bitmap, PointF(0.5f, 1.5f)
        )

        iMapService.setMapCameraListener(this)

    }

    override fun getInfoContents(p0: Marker?): View? {
        return null
    }

    private var infoWindow: View? = null

    override fun getInfoWindow(p0: Marker?): View? {
        if (p0?.title.isNullOrBlank()) return null
        if (infoWindow == null) {
            infoWindow = LayoutInflater.from(this).inflate(
                R.layout.custom_info_window, null
            )
        }
        render(p0, infoWindow)
        return infoWindow
    }

    private fun render(p0: Marker?, view: View?) {
        if (p0?.title == "") {

        }
    }

    override fun onCameraChangeFinish(p0: CameraPosition?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCameraChange(p0: CameraPosition) {
        Log.d("map", p0.bearing.toString())
    }


}
