package com.hymnal.lbs


import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.amap.api.maps.model.BitmapDescriptor

import com.amap.api.maps.model.LatLng

/**
 * 地图的抽象接口
 * Created by liuguangli on 17/3/18.
 */

interface IMapService {
    /**
     * 获取一个地图视图
     */
    val map: View

    /**
     * 获取当前城市
     */
    var city: String

    /**
     * 设置定位图标
     */
    fun setLocationRes(res: Int)

    fun addInfoWindowMarker(locationInfo: LocationInfo, bitmap: Bitmap)


    fun setMarkerInfoWindowClickListener(listener: OnInfoWindowMarkerListener)


    fun updateMarkers(vararg markers: LocationMarker)

    /**
     * 移除标记
     */
    fun removeMarker(vararg keys: String)

    /**
     * 移除所有标记
     */

    fun clearAllMarker()

    // 业务层使用通用的监听器
    var mLocationListener: OnLocationListener?

    /**
     * 位置变化监听
     */
    fun setLocationChangeListener(listener: OnLocationListener)

    /**
     * 联动搜索附近的位置
     */
    fun poiSearch(key: String, listener: OnSearchedListener)

    /**
     * 移动相机到点
     */

    fun moveCamera(locationInfo: LocationInfo, scale: Int)

    fun moveCamera(locationInfo: LocationInfo)

    fun zoomInAndOut(zoom: Boolean)

    /**
     * 移动相机到范围
     */
    fun moveCamera(locationInfo1: LocationInfo, locationInfo2: LocationInfo)

    fun startOnceLocation()

    fun setMySensor()

    fun setPointToCenter(x: Int, y: Int)

    //    void rotateEnable(boolean enable);

    fun changeBearing(bearing: Float)

    fun changeLatLng(locationInfo: LocationInfo)

    fun changeTilt(tilt: Float)

    fun polyline(tag: String, list: List<LatLng>, width: Float, color: Int)

    fun polyline(tag: String, list: List<LatLng>, textures: List<BitmapDescriptor>, indexList:List<Int>, width: Float)

    fun removeLine(tag: String)

    /**
     * 驾车线路规划
     */

    fun driverRoute(start: LocationInfo, end: LocationInfo, color: Int, listener: OnRouteListener)

    fun calculateLineDistance(start: LocationInfo, end: LocationInfo): Float

    /**
     * 生命周期函数定义
     */

    fun onCreate(savedState: Bundle?)

    fun onSaveInstanceState(outState: Bundle)

    fun onResume()

    fun onPause()

    fun onDestroy()


}
