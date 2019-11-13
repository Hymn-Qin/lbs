package com.hymnal.lbs.gaode

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View

import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.PolylineOptions
import com.amap.api.maps.utils.overlay.SmoothMoveMarker
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.InputtipsQuery
import com.amap.api.services.route.BusRouteResult
import com.amap.api.services.route.DriveRouteResult
import com.amap.api.services.route.RideRouteResult
import com.amap.api.services.route.RouteSearch
import com.amap.api.services.route.WalkRouteResult
import com.hymnal.lbs.*

import java.util.ArrayList
import java.util.HashMap


class GaodeService(context: Context) : BaseMapService(context) {

    //位置定位对象
    private var mLocationClient: AMapLocationClient? = null
    // 地图视图对象
    private val mapView: MapView = MapView(context)
    private var myLocationStyle: MyLocationStyle? = null
    // 地图管理对象
    private val aMap: AMap = mapView.map
    // 地图位置变化回调对象
    private var mLocationChangeListener: LocationSource.OnLocationChangedListener? = null
    private var firstLocation = true
    // 管理地图标记集合
    private val mMarkersHashMap = HashMap<String, SmoothMoveMarker>()

    // 异步路径规划驾车模式查询
    private val mRouteSearch = RouteSearch(context)
    private var locationOption: AMapLocationClientOption? = null

    override val map: View = mapView
    override lateinit var city: String
    override var mLocationListener: OnLocationListener? = null

    fun initLocation(context: Context) {
        // 创建定位对象
        mLocationClient = AMapLocationClient(context)
        locationOption = AMapLocationClientOption()
        //设置为高精度定位模式
        locationOption?.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy

        locationOption?.interval = 1000
        //设置定位参数
        //        mLocationClient?.setLocationOption(locationOption)
        //获取一次定位结果：
        //该方法默认为false。
        //        locationOption.setOnceLocation(true);

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        //        locationOption.setOnceLocationLatest(true);
        //给定位客户端对象设置定位参数

        mLocationClient?.setLocationOption(locationOption)
        mLocationClient?.startLocation()

    }

    override fun setLocationRes(res: Int) {
        myLocationStyle = MyLocationStyle()
        myLocationStyle!!.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
        myLocationStyle!!.myLocationIcon(BitmapDescriptorFactory.fromResource(res))// 设置小蓝点的图标
        myLocationStyle!!.strokeColor(Color.BLACK)// 设置圆形的边框颜色
        myLocationStyle!!.radiusFillColor(Color.TRANSPARENT)// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle!!.strokeWidth(1.0f)// 设置圆形的边框粗细
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
            smoothMarker = SmoothMoveMarker(aMap)
            mMarkersHashMap[marker.key] = smoothMarker
        }
        // 设置滑动的轨迹左边点
        smoothMarker.setPoints(points)
        // 设置滑动的图标
        smoothMarker.setDescriptor(BitmapDescriptorFactory.fromBitmap(marker.icon))
        // 设置滑动的总时间
        smoothMarker.setTotalDuration(marker.animTime)
        smoothMarker.setRotate(marker.rotate)
        // 开始滑动
        smoothMarker.startSmoothMove()

        //        if (mMarkersHashMap.containsKey(marker.key)) {
//            Marker smoothMarker = mMarkersHashMap.get(marker.key);
//            smoothMarker.setPosition(new LatLng(marker.latitude, marker.longitude));
//            smoothMarker.setRotateAngle(marker.rotate - 110);
//            smoothMarker.setIcon(BitmapDescriptorFactory.fromBitmap(marker.icon));
//        } else {
//            MarkerOptions options = new MarkerOptions();
//            options.icon(BitmapDescriptorFactory.fromBitmap(marker.icon));
//            options.rotateAngle(marker.rotate - 110);
//            options.position(new LatLng(marker.latitude, marker.longitude));
//            Marker newMark = aMap.addMarker(options);
//            newMark.setRotateAngle(marker.rotate - 110);
//            newMark.showInfoWindow();
//            mMarkersHashMap.put(marker.key, newMark);
//        }

    }

    override fun removeMarker(vararg keys: String) {
        for (key in keys)
            if (mMarkersHashMap.containsKey(key)) {
                mMarkersHashMap[key]!!.destroy()
                mMarkersHashMap.remove(key)
            }
    }

    override fun addInfoWindowMarker(locationInfo: LocationInfo, bitmap: Bitmap) {
        val latLng = LatLng(locationInfo.latitude, locationInfo.longitude)
        //        // 如果已经存在则更新角度、位置   // 如果不存在则创建
        //        Marker marker = mMarkersHashMap.get(locationInfo.key);
        //        if (marker != null) {
        //            marker.setPosition(latLng);
        //            marker.setTitle(locationInfo.name);
        //            marker.setSnippet(locationInfo.address);
        //            marker.setRotateAngle(locationInfo.rotation);
        //        } else {
        //
        //            mMarkersHashMap.put(locationInfo.key, marker);
        //            if (locationInfo.animation != null) {
        //                marker.setAnimation(locationInfo.animation);
        //                marker.startAnimation();
        //            }
        //        }
        val options = MarkerOptions()
        options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        options.anchor(0.5f, 0.5f)
        options.position(latLng)
        options.title(locationInfo.name)
        val marker = aMap.addMarker(options)
        marker.title = locationInfo.name
        marker.snippet = locationInfo.address
        marker.rotateAngle = 0f
        marker.showInfoWindow()
    }

    override fun setMarkerInfoWindowClickListener(listener: OnInfoWindowMarkerListener) {
        //        aMap.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() {
        //            @Override
        //            public void onInfoWindowClick(Marker marker) {
        //                listener.onClick(marker.getTitle(), marker.getSnippet());
        //            }
        //        });
    }


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

    override fun startOnceLocation() {
        mLocationClient?.startLocation()
    }

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

    private fun setUpLocation() {
        //设置监听器

        mLocationClient?.setLocationListener { aMapLocation ->
            if (mLocationChangeListener != null) {
                mLocationChangeListener!!.onLocationChanged(aMapLocation)

                if (firstLocation) {
                    firstLocation = false
                    aMap.animateCamera(CameraUpdateFactory.zoomTo(18f))
                }
                city = aMapLocation.city
                mLocationListener?.onLocationChange(
                    LocationInfo(
                        name = aMapLocation.poiName,
                        address = aMapLocation.address,
                        latitude = aMapLocation.latitude,
                        longitude = aMapLocation.longitude
                    )
                )
            }
            if (mLocationChangeListener != null) {
                // 地图已经激活，通知蓝点实时更新
                mLocationChangeListener!!.onLocationChanged(aMapLocation)// 显示系统小蓝点

                if (firstLocation) {
                    firstLocation = false
                    aMap.animateCamera(CameraUpdateFactory.zoomTo(18f))
                }
                city = aMapLocation.city
                mLocationListener?.onLocationChange(
                    LocationInfo(
                        name = aMapLocation.poiName,
                        address = aMapLocation.address,
                        latitude = aMapLocation.latitude,
                        longitude = aMapLocation.longitude
                    )
                )
            }

            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除

            mLocationClient?.startLocation()
        }
    }

    override fun calculateLineDistance(start: LocationInfo, end: LocationInfo): Float {
        return AMapUtils.calculateLineDistance(
            LatLng(start.latitude, start.longitude),
            LatLng(end.latitude, end.longitude)
        )
    }

    private fun setUpMap() {
        if (myLocationStyle != null) {
            aMap.myLocationStyle = myLocationStyle
        }
        // 设置地图激活（加载监听）
        aMap.setLocationSource(object : LocationSource {
            override fun activate(onLocationChangedListener: LocationSource.OnLocationChangedListener) {
                mLocationChangeListener = onLocationChangedListener

            }

            override fun deactivate() {

                mLocationClient?.stopLocation()
                mLocationClient?.onDestroy()
                mLocationClient = null

            }
        })
        // 设置默认定位按钮是否显示，这里先不想业务使用方开放
        val uiSettings = aMap.uiSettings
        uiSettings.isMyLocationButtonEnabled = false
        uiSettings.isZoomControlsEnabled = false
        uiSettings.isZoomGesturesEnabled = false
        uiSettings.setAllGesturesEnabled(true)
        uiSettings.setLogoBottomMargin(-50)
        aMap.isMyLocationEnabled = true
//        aMap.animateCamera(CameraUpdateFactory.zoomIn())
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false，这里先不想业务使用方开放
    }

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        mapView.onCreate(savedState)
        setUpMap()

    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        setUpLocation()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        mLocationClient?.stopLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        mLocationClient?.onDestroy()
    }
}
