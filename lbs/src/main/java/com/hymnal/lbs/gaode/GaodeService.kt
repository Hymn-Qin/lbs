package com.hymnal.lbs.gaode

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.view.View
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.*
import com.amap.api.maps.model.*
import com.amap.api.maps.utils.overlay.MovingPointOverlay
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.InputtipsQuery
import com.amap.api.services.route.*
import com.hymnal.lbs.*
import org.slf4j.LoggerFactory
import java.util.*


class GaodeService(context: Context) : BaseMapService(context) {

    private val logger by lazy { LoggerFactory.getLogger(GaodeService::class.java) }


    // 地图视图对象
    private val mapView: MapView = MapView(context)
    private var myLocationStyle: MyLocationStyle? = null
    // 地图管理对象
    private val aMap: AMap = mapView.map
    // 地图位置变化回调对象
    private var mLocationChangeListener: LocationSource.OnLocationChangedListener? = null
    // 管理地图标记集合
    private val mMarkersHashMap = HashMap<String, MovingPointOverlay>()

    private val markersHashMap = HashMap<String, Marker>()

    // 异步路径规划驾车模式查询
    private val mRouteSearch = RouteSearch(context)
    //位置定位对象
    private var mLocationClient: AMapLocationClient? = null
    private lateinit var locationOption: AMapLocationClientOption

    override val map: View = mapView
    override lateinit var city: String
    override var mLocationListener: OnLocationListener? = null

    fun initLocation() {
        logger.info("初始化定位")

        locationOption = AMapLocationClientOption()
        //设置为出行
        locationOption.locationPurpose = AMapLocationClientOption.AMapLocationPurpose.Transport
        //返回最近3s的定位数据
        locationOption.isOnceLocationLatest = true

        // 创建定位对象
        mLocationClient = AMapLocationClient(context)
        mLocationClient?.setLocationOption(locationOption)
        mLocationClient?.setLocationListener { location ->
            if (location.errorCode == 0) {
                logger.info(
                    "定位成功:{}",
                    "longitude:${location.longitude},latitude:${location.latitude}"
                )

                if (myLocationStyle?.myLocationType == MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE) {
                    if (aMap.cameraPosition.zoom != 16f) {
                        aMap.animateCamera(CameraUpdateFactory.zoomTo(16f))
                    }

                }

                mLocationChangeListener?.onLocationChanged(location)

                city = location.city

                val visibleRegion = aMap.projection.visibleRegion // 获取可视区域、

                val latLngBounds = visibleRegion.latLngBounds// 获取可视区域的Bounds
                val user = LatLng(location.latitude, location.longitude)
                val isContain = latLngBounds.contains(user)// 判断经纬度是否包括在当前地图可见区域
                mLocationListener?.onLocationChange(
                    LocationInfo(
                        name = location.poiName,
                        address = location.address,
                        latitude = location.latitude,
                        longitude = location.longitude
                    ),
                    isContain
                )

            } else {
                logger.error(
                    "定位失败:{}",
                    "location Error, ErrCode:" + location.errorCode + ", errInfo:" + location.errorInfo
                )
            }
        }
        mLocationClient?.startLocation()

        aMap.animateCamera(CameraUpdateFactory.zoomTo(16f))

    }

    override fun setLocationRes(res: Int) {
        logger.info("初始化定位蓝点和模式")
        myLocationStyle = MyLocationStyle()
        myLocationStyle!!.myLocationIcon(BitmapDescriptorFactory.fromResource(res))// 设置小蓝点的图标
        myLocationStyle!!.strokeColor(Color.argb(150, 3, 145, 255))// 设置圆形的边框颜色
        myLocationStyle!!.radiusFillColor(Color.argb(18, 0, 0, 170))// 设置圆形的填充颜色
        myLocationStyle!!.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle!!.strokeWidth(1.0f)// 设置圆形的边框粗细

        logger.info("初始化定位按钮手势")
        // 设置默认定位按钮是否显示，这里先不想业务使用方开放
        val uiSettings = aMap.uiSettings
        uiSettings.isMyLocationButtonEnabled = false
        uiSettings.isZoomControlsEnabled = false
        uiSettings.isZoomGesturesEnabled = false
        uiSettings.setAllGesturesEnabled(true)
        uiSettings.isZoomGesturesEnabled = true
        uiSettings.isScrollGesturesEnabled = true
        uiSettings.isTiltGesturesEnabled = false
        uiSettings.isRotateGesturesEnabled = false
        uiSettings.isGestureScaleByMapCenter = true
        uiSettings.setLogoBottomMargin(-50)
        // 设置地图定位数据源
        aMap.setLocationSource(object : LocationSource {
            override fun activate(onLocationChangedListener: LocationSource.OnLocationChangedListener) {
                logger.info("开始定位")
                mLocationChangeListener = onLocationChangedListener

            }

            override fun deactivate() {
                logger.info("结束定位")
                mLocationClient?.stopLocation()
                mLocationClient?.onDestroy()
                mLocationClient = null

            }
        })

        aMap.myLocationStyle = myLocationStyle
        aMap.isMyLocationEnabled = true
    }

    fun setFollow(follow: Boolean) {
        aMap.myLocationStyle = if (follow) {
            if (myLocationStyle!!.myLocationType == MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE) return
            aMap.animateCamera(CameraUpdateFactory.zoomTo(16f), object : AMap.CancelableCallback {
                override fun onFinish() {
                    aMap.animateCamera(CameraUpdateFactory.changeBearing(0f))
                }

                override fun onCancel() {

                }
            })

            myLocationStyle!!.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
        } else {
            if (myLocationStyle!!.myLocationType == MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER) return
            myLocationStyle!!.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
        }
    }

    override fun updateMarkers(vararg markers: LocationMarker) {
        for (marker in markers)
            updateMakerOneTimeSmooth(marker)
    }


    private fun updateMakerOneTimeSmooth(marker: LocationMarker) {
        val points = ArrayList<LatLng>()
        val endLatLng = LatLng(marker.latitude, marker.longitude)

        var smoothMarker = mMarkersHashMap[marker.key]
        if (smoothMarker != null) {
            val startLatLng = smoothMarker.position
            points.add(startLatLng)
            points.add(endLatLng)
        } else {
            points.add(endLatLng)
            points.add(endLatLng)
            val options = MarkerOptions()
            options.icon(BitmapDescriptorFactory.fromBitmap(marker.icon))
            options.rotateAngle(marker.rotate)
            options.position(endLatLng)
            options.anchor(0.5f, 0.5f)
            val newMark = aMap.addMarker(options)
            newMark.isInfoWindowEnable = false
            newMark.isClickable = false
            smoothMarker = MovingPointOverlay(aMap, newMark)
            mMarkersHashMap[marker.key] = smoothMarker
        }
        // 设置滑动的轨迹左边点
        smoothMarker.setPoints(points)
        // 设置滑动的图标
        // 设置滑动的总时间
        smoothMarker.setTotalDuration(marker.animTime)
        smoothMarker.setRotate(marker.rotate)
        // 开始滑动
        smoothMarker.startSmoothMove()

    }

    override fun removeMarker(vararg keys: String) {
        for (key in keys)
            if (mMarkersHashMap.containsKey(key)) {
                mMarkersHashMap[key]!!.destroy()
                mMarkersHashMap.remove(key)
            }
    }

    override fun addInfoWindowMarker(locationInfo: LocationInfo, bitmap: Bitmap, point: PointF) {
        val latLng = LatLng(locationInfo.latitude, locationInfo.longitude)


        val options = MarkerOptions()
        options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        options.anchor(point.x, point.y)
        options.position(latLng)
        options.isFlat = true
        options.title(locationInfo.name)
        options.snippet(locationInfo.address)
        val marker = aMap.addMarker(options)
        marker.isInfoWindowEnable = false
        marker.isClickable = false
        marker.isFlat = true
        markersHashMap[locationInfo.key] = marker
    }

    override fun addInfoWindowMarker(locationInfo: LocationInfo, view: View, point: PointF) {
        val latLng = LatLng(locationInfo.latitude, locationInfo.longitude)


        val options = MarkerOptions()
        options.icon(BitmapDescriptorFactory.fromView(view))
        options.anchor(point.x, point.y)
        options.position(latLng)
        options.isFlat = true
        options.title(locationInfo.name)
        options.snippet(locationInfo.address)
        val marker = aMap.addMarker(options)
        marker.isInfoWindowEnable = false
        marker.isClickable = false
        marker.isFlat = true
        markersHashMap[locationInfo.key] = marker
    }

    fun addInfoWindowMarker(
        locationInfo: LocationInfo,
        bitmap: Bitmap,
        p0: AMap.InfoWindowAdapter
    ) {
        aMap.setInfoWindowAdapter(p0)
        val latLng = LatLng(locationInfo.latitude, locationInfo.longitude)
        val options = MarkerOptions()
        options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        options.anchor(0.5f, 0.5f)
        options.position(latLng)
        options.isFlat = true
        options.draggable(true)
        options.title(locationInfo.name)
        val marker = aMap.addMarker(options)
        marker.rotateAngle = 0f
        marker.title = locationInfo.name
        marker.snippet = locationInfo.address
        marker.setAnchor(1f, 1f)
        marker.showInfoWindow()
        marker.isVisible = true
        marker.isClickable = false
        marker.isFlat = true
        markersHashMap[locationInfo.key] = marker
    }

    fun clearAllInfoMarker() {
        markersHashMap.forEach {
            it.value.destroy()
        }
    }

    fun clearInfoMarker(key: String) {
        markersHashMap[key]?.destroy()
    }


    override fun setMarkerInfoWindowClickListener(listener: OnInfoWindowMarkerListener) {}

    override fun clearAllMarker() {
        aMap.clear()
        mMarkersHashMap.clear()
    }

    override fun poiSearch(key: String, listener: OnSearchedListener) {
        if (!key.isBlank()) {
            val inputquery = InputtipsQuery(key, "")
            val inputTips = Inputtips(context, inputquery)
            inputTips.setInputtipsListener { tipList, rCode ->
                when (rCode) {
                    AMapException.CODE_AMAP_SUCCESS -> {
                        val locationInfos = ArrayList<LocationInfo>()
                        for (tip in tipList)
                            locationInfos.add(
                                LocationInfo(
                                    name = tip.name,
                                    address = tip.address,
                                    latitude = tip.point.latitude,
                                    longitude = tip.point.longitude
                                )
                            )
                        listener.onSearched(locationInfos)
                    }
                    else -> listener.onError(rCode)
                }
            }
            inputTips.requestInputtipsAsyn()
        }
    }

    override fun zoomInAndOut(zoom: Boolean) {
        if (zoom) {
            aMap.animateCamera(
                CameraUpdateFactory.zoomIn()
            )
        } else {
            aMap.animateCamera(
                CameraUpdateFactory.zoomOut()
            )
        }
    }

    override fun moveCamera(locationInfo: LocationInfo, scale: Int) {
        aMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    locationInfo.latitude,
                    locationInfo.longitude
                ), scale.toFloat()
            )
        )
    }

    override fun moveCamera(locationInfo: LocationInfo) {
        aMap.animateCamera(
            CameraUpdateFactory.newLatLng(
                LatLng(
                    locationInfo.latitude,
                    locationInfo.longitude
                )
            )
        )
    }

    override fun moveCamera(locationInfo1: LocationInfo, locationInfo2: LocationInfo) {
        val latLngFirst = LatLng(locationInfo1.latitude, locationInfo1.longitude)
        val latLngEnd = LatLng(locationInfo2.latitude, locationInfo2.longitude)
        val latLngBounds = LatLngBounds(latLngFirst, latLngEnd)
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 20))
    }

    override fun startOnceLocation() {}

    override fun setMySensor() {
        val mSensor = SensorEventHelper(context)
        mSensor.registerSensorListener(object : LbsSensorListner(context) {
            override fun onSensorLisner(v: Float) {
                if (mMarkersHashMap.containsKey(KEY_MY_MARKERE)) {
                    mMarkersHashMap[KEY_MY_MARKERE]?.setRotate(v)
                }

            }
        })
    }

    override fun setPointToCenter(x: Int, y: Int) {
        aMap.setPointToCenter(x, y)
    }

    override fun changeBearing(bearing: Float) {
        aMap.animateCamera(CameraUpdateFactory.changeBearing(bearing))
    }

    override fun changeLatLng(locationInfo: LocationInfo) {
        aMap.animateCamera(
            CameraUpdateFactory.changeLatLng(
                LatLng(
                    locationInfo.latitude,
                    locationInfo.longitude
                )
            )
        )
    }

    override fun changeTilt(tilt: Float) {
        aMap.animateCamera(CameraUpdateFactory.changeTilt(tilt))
    }

    private val lineHashMap = HashMap<String, Polyline>()
    //Color.argb(255,255,20,147)
    override fun polyline(tag: String, list: List<LatLng>, width: Float, color: Int) {
        lineHashMap[tag]?.remove()
        aMap.mapTextZIndex = 2

        val line = PolylineOptions()
            //手动数据测试
            //.add(new LatLng(26.57, 106.71),new LatLng(26.14,105.55),new LatLng(26.58, 104.82), new LatLng(30.67, 104.06))
            //集合数据
            .addAll(list)
            //线的宽度
            .width(width)
            .setDottedLine(false)
            .geodesic(true)
            //颜色
            .setUseTexture(true)
            .color(color)
        val polyline = aMap.addPolyline(line)
        polyline.isVisible = true
        lineHashMap[tag] = polyline
    }

    private var list: List<LatLng>? = null

    override fun polyline(
        tag: String,
        list: List<LatLng>,
        textures: List<BitmapDescriptor>,
        indexList: List<Int>,
        width: Float
    ) {

        if (lineHashMap[tag] != null) {
            if (this.list.hashCode() == list.hashCode()) return
//            val line = lineHashMap[tag]
//            line?.remove()
            lineHashMap.forEach {
                it.value.remove()
            }
            lineHashMap.clear()
        }
        this.list = list
        val newLine = PolylineOptions()
            .addAll(list)
            .width(width)
            .setDottedLine(false)
            .geodesic(true)
            .setCustomTextureList(textures)
            .setCustomTextureIndex(indexList)
            .setUseTexture(true)
            .zIndex(1f)
        val polyline = aMap.addPolyline(newLine)
        polyline.isVisible = true
        lineHashMap[tag] = polyline
    }

    fun addCarLine(
        tag: String,
        point: LatLng,
        textures: List<BitmapDescriptor>,
        indexList: List<Int>,
        width: Float
    ) {
        val converter = CoordinateConverter(context)
        // CoordType.GPS 待转换坐标类型
        converter.from(CoordinateConverter.CoordType.GPS)
        // sourceLatLng待转换坐标点 LatLng类型
        converter.coord(point)
        // 执行转换操作
        val desLatLng = converter.convert()
        if (lineHashMap[tag] == null) {
            val line = PolylineOptions()
                //手动数据测试
                .add(desLatLng)
                //线的宽度
                .width(width)
                .setDottedLine(false)
                .geodesic(true)
                //颜色
                .setCustomTextureList(textures)
                .setCustomTextureIndex(indexList)
                .setUseTexture(true)
                .zIndex(1f)
            val polyline = aMap.addPolyline(line)
            polyline.isVisible = true
            lineHashMap[tag] = polyline
        } else {
            val line = lineHashMap[tag]!!
            val list = line.points
            list.add(desLatLng)
            line.points = list
        }


    }

    fun addPointInLine(
        tag: String,
        point: LatLng,
        textures: List<BitmapDescriptor>,
        indexList: List<Int>,
        width: Float
    ) {
        val converter = CoordinateConverter(context)
        // CoordType.GPS 待转换坐标类型
        converter.from(CoordinateConverter.CoordType.GPS)
        // sourceLatLng待转换坐标点 LatLng类型
        converter.coord(point)
        // 执行转换操作
        val desLatLng = converter.convert()

        val line = lineHashMap[tag] ?: return
        val list = lineHashMap[tag]?.points ?: return
        logger.info("未走段数：{}", list.size)
        if (list.isNullOrEmpty() || list.size <= 1) {

            return
        }
//        float distance = AMapUtils.calculateLineDistance(latLng1,latLng2)//两点间的距离
//        val index = GeometryUtil.closestOnLine
        val distanceMin = list.minBy { AMapUtils.calculateLineDistance(desLatLng, it) }
        val distance = AMapUtils.calculateLineDistance(desLatLng, distanceMin)
        logger.error("车辆距路径：{}米", distance)
        if (distance > 10 ) return
        val index = list.indexOf(distanceMin)
        val newList = list.toTypedArray().clone().toList().subList(index, list.size)
        val oldList = list.toTypedArray().clone().toList().subList(0, index + 1)

        line.points = newList

        if (lineHashMap["old"] == null) {
            val oldLine = PolylineOptions()
                //手动数据测试
                //.add(new LatLng(26.57, 106.71),new LatLng(26.14,105.55),new LatLng(26.58, 104.82), new LatLng(30.67, 104.06))
                //集合数据
                .addAll(oldList)
                //线的宽度
                .width(width)
                .setDottedLine(false)
                .geodesic(true)
                //颜色
                .setCustomTextureList(textures)
                .setCustomTextureIndex(indexList)
                .setUseTexture(true)
                .zIndex(1f)
            val polyline = aMap.addPolyline(oldLine)
            polyline.isVisible = true
            lineHashMap["old"] = polyline
        } else {
            val oldLine = lineHashMap["old"] ?: return
            val list = lineHashMap["old"]?.points ?: return
            list.addAll(oldList)
            oldLine.points = list
        }


    }

    override fun removeLine(tag: String) {
        lineHashMap[tag]?.remove()
    }

    fun clearAllLine() {
        lineHashMap.forEach {
            it.value.remove()
        }
        lineHashMap.clear()
        mMarkersHashMap["start"]?.destroy()
        mMarkersHashMap["end"]?.destroy()
        mMarkersHashMap.remove("start")
        mMarkersHashMap.remove("end")
    }

    override fun driverRoute(
        start: LocationInfo,
        end: LocationInfo,
        color: Int,
        listener: OnRouteListener
    ) {
        val fromAndTo = RouteSearch.FromAndTo(
            LatLonPoint(start.latitude, start.longitude),
            LatLonPoint(end.latitude, end.longitude)
        )
        // 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
        val query =
            RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DrivingDefault, null, null, "")
        mRouteSearch.calculateDriveRouteAsyn(query)
        mRouteSearch.setRouteSearchListener(object : RouteSearch.OnRouteSearchListener {
            override fun onBusRouteSearched(busRouteResult: BusRouteResult, i: Int) {

            }

            override fun onDriveRouteSearched(driveRouteResult: DriveRouteResult, color: Int) {
                // 获取第一条路径
                val driveRoute = driveRouteResult.paths[0]
                val routeOptions = PolylineOptions()
                // 路径起点
                val startPoint = driveRouteResult.startPos
                // 路径中间步骤
                val routeSteps = driveRoute.steps
                // 路径终点
                val endPoint = driveRouteResult.targetPos
                // 绘制路径
                routeOptions.add(LatLng(startPoint.latitude, startPoint.longitude))
                for (step in routeSteps)
                    for (latLonPoint in step.polyline)
                        routeOptions.add(LatLng(latLonPoint.latitude, latLonPoint.longitude))
                routeOptions.color(color)
                routeOptions.add(LatLng(endPoint.latitude, endPoint.longitude))
                aMap.addPolyline(routeOptions)
                val info = RouteInfo(
                    taxiCost = driveRouteResult.taxiCost,
                    distance = 0.5f + driveRoute.distance / 1000,
                    duration = (10 + driveRoute.duration / 1000 * 60).toInt()
                )
                listener.onComplete(info)
            }

            override fun onWalkRouteSearched(walkRouteResult: WalkRouteResult, i: Int) {

            }

            override fun onRideRouteSearched(rideRouteResult: RideRouteResult, i: Int) {

            }
        })
    }

    override fun calculateLineDistance(start: LocationInfo, end: LocationInfo): Float {
        return AMapUtils.calculateLineDistance(
            LatLng(start.latitude, start.longitude),
            LatLng(end.latitude, end.longitude)
        )
    }

    fun setMapCameraListener(listener: AMap.OnCameraChangeListener) {
        aMap.setOnCameraChangeListener(listener)
    }

    fun setMapTouchListener(listener: AMap.OnMapTouchListener) {
        aMap.setOnMapTouchListener(listener)
    }

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        mapView.onCreate(savedState)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        mLocationClient?.stopLocation()
        mLocationClient?.onDestroy()
    }
}