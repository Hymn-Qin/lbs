package com.hymnal.test

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.amap.api.maps.AMap
import com.amap.api.maps.model.Marker
import com.hymnal.lbs.LocationInfo
import com.hymnal.lbs.gaode.GaodeService
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), AMap.InfoWindowAdapter {

    private val startBitmap by lazy {
        resources.bitmap {
            source = R.mipmap.point_a
            scale = 1f
        }
    }

    private val start1Bitmap by lazy {
        resources.bitmap {
            source = R.mipmap.location_a
            scale = 1f
        }
    }

    private val start2Bitmap by lazy {
        resources.bitmap {
            source = R.drawable.bg
        }
    }

    private lateinit var iMapService: GaodeService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        iMapService = GaodeService(this)
        iMapService.onCreate(savedInstanceState)
        map.addView(iMapService.map)
        `in`.setOnClickListener {
            iMapService.zoomInAndOut(true)
        }
        out.setOnClickListener {
            iMapService.zoomInAndOut(false)
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

//        iMapService.updateMarkers(LocationMarker("1", 30.336883, 121.244685, startBitmap, rotate = 0f, animTime = 1))
//        iMapService.updateMarkers(LocationMarker("2", 30.336883, 121.244685, start1Bitmap, rotate = 0f, animTime = 1))
        iMapService.addInfoWindowMarker(
            LocationInfo(
                30.336883, 121.244685,
                "123", "123", address = "123456",
                rotation = 0f
            ), startBitmap, this
        )
    }

    override fun getInfoContents(p0: Marker?): View? {
        return null
    }

    var infoWindow: View? = null

    override fun getInfoWindow(p0: Marker?): View {
        if(infoWindow == null) {
            infoWindow = LayoutInflater.from(this).inflate(
                R.layout.custom_info_window, null)
        }
        render(p0, infoWindow)
        return infoWindow!!
    }

    private fun render(p0: Marker?, view: View?) {

    }

}
