package com.hymnal.test

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.hymnal.lbs.gaode.GaodeService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val iMapService = GaodeService(this)
        iMapService.onCreate(savedInstanceState)
        map.addView(iMapService.map)
        `in`.setOnClickListener {
            iMapService.zoomInAndOut(true)
        }
        out.setOnClickListener {
            iMapService.zoomInAndOut(false)
        }

    }
}
