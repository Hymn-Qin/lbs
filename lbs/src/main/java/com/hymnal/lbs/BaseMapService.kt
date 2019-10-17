package com.hymnal.lbs

import android.content.Context
import android.os.Bundle

abstract class BaseMapService(var context: Context) : IMapService {

    var KEY_MY_MARKERE = "-1"



    override fun setLocationChangeListener(listener: OnLocationListener) {
        this.mLocationListener = listener
    }

    override fun onCreate(savedState: Bundle?) {

    }

    override fun onSaveInstanceState(outState: Bundle) {

    }

    override fun onResume() {

    }

    override fun onPause() {

    }

    override fun onDestroy() {

    }
}
