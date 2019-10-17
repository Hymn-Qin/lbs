package com.hymnal.lbs

import kotlin.math.*

/**
 * 各地图API坐标系统比较与转换;
 * WGS84坐标系：即地球坐标系，国际上通用的坐标系。设备一般包含GPS芯片或者北斗芯片获取的经纬度为WGS84地理坐标系,
 * 谷歌地图采用的是WGS84地理坐标系（中国范围除外）;
 * GCJ02坐标系：即火星坐标系，是由中国国家测绘局制订的地理信息系统的坐标系统。由WGS84坐标系经加密后的坐标系。
 * 谷歌中国地图和搜搜中国地图采用的是GCJ02地理坐标系; BD09坐标系：即百度坐标系，GCJ02坐标系经加密后的坐标系;
 * 搜狗坐标系、图吧坐标系等，估计也是在GCJ02基础上加密而成的。 chenhua
 */

val BAIDU_LBS_TYPE = "bd09ll"

var pi = 3.1415926535897932384626
var a = 6378245.0
var ee = 0.00669342162296594323

/**
 * 84 to 火星坐标系 (GCJ-02) World Geodetic System ==> Mars Geodetic System
 *
 * @param lat
 * @param lon
 * @return
 */
fun Gps.gps84_To_Gcj02(): Gps {
    //        if (outOfChina(lat, lon)) {
    //            return null;
    //        }

    //        LogUtils.d("是否为中国坐标" + outOfChina(lat, lon));

    var dLat = transformLat(longitude - 105.0, latitude - 35.0)
    var dLon = transformLon(longitude - 105.0, latitude - 35.0)
    val radLat = latitude / 180.0 * pi
    var magic = Math.sin(radLat)
    magic = 1 - ee * magic * magic
    val sqrtMagic = Math.sqrt(magic)
    dLat = dLat * 180.0 / (a * (1 - ee) / (magic * sqrtMagic) * pi)
    dLon = dLon * 180.0 / (a / sqrtMagic * Math.cos(radLat) * pi)
    val mgLat = latitude + dLat
    val mgLon = longitude + dLon
    return Gps(mgLat, mgLon)
}

/**
 * * 火星坐标系 (GCJ-02) to 84 * * @param lon * @param lat * @return
 */
fun Gps.gcj_To_Gps84(): Gps {
    val (latitude, longitude) = transform()
    return Gps(this.latitude * 2 - latitude, this.longitude * 2 - longitude)
}

/**
 * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 将 GCJ-02 坐标转换成 BD-09 坐标
 *
 * @param gg_lat
 * @param gg_lon
 */
fun Gps.gcj02_To_Bd09(): Gps {
    val z = sqrt(longitude * longitude + latitude * latitude) + 0.00002 * sin(latitude * pi)
    val theta = atan2(latitude, longitude) + 0.000003 * cos(longitude * pi)
    return Gps(z * sin(theta) + 0.006, z * cos(theta) + 0.0065)
}

/**
 * * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 * * 将 BD-09 坐标转换成GCJ-02 坐标 * * @param
 * bd_lat * @param bd_lon * @return
 */
fun Gps.bd09_To_Gcj02(): Gps {
    val x = longitude - 0.0065
    val y = latitude - 0.006
    val z = sqrt(x * x + y * y) - 0.00002 * sin(y * pi)
    val theta = atan2(y, x) - 0.000003 * cos(x * pi)
    return Gps(z * sin(theta), z * cos(theta))
}

/**
 * (BD-09)-->84
 *
 * @param bd_lat
 * @param bd_lon
 * @return
 */
fun Gps.bd09_To_Gps84() = bd09_To_Gcj02().gcj_To_Gps84()

fun Gps.outOfChina(): Boolean {
    if (longitude < 72.004 || latitude > 137.8347)
        return true
    return latitude < 0.8293 || latitude > 55.8271
}

fun Gps.transform(): Gps {
    if (outOfChina()) {
        return this
    }
    var dLat = transformLat(longitude - 105.0, latitude - 35.0)
    var dLon = transformLon(longitude - 105.0, latitude - 35.0)
    val radLat = latitude / 180.0 * pi
    var magic = sin(radLat)
    magic = 1 - ee * magic * magic
    val sqrtMagic = sqrt(magic)
    dLat = dLat * 180.0 / (a * (1 - ee) / (magic * sqrtMagic) * pi)
    dLon = dLon * 180.0 / (a / sqrtMagic * cos(radLat) * pi)
    val mgLat = latitude + dLat
    val mgLon = longitude + dLon
    return Gps(mgLat, mgLon)
}

fun transformLat(x: Double, y: Double): Double {
    var ret = (-100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
            + 0.2 * sqrt(abs(x)))
    ret += (20.0 * sin(6.0 * x * pi) + 20.0 * sin(2.0 * x * pi)) * 2.0 / 3.0
    ret += (20.0 * sin(y * pi) + 40.0 * sin(y / 3.0 * pi)) * 2.0 / 3.0
    ret += (160.0 * sin(y / 12.0 * pi) + 320 * sin(y * pi / 30.0)) * 2.0 / 3.0
    return ret
}

fun transformLon(x: Double, y: Double): Double {
    var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * sqrt(abs(x))
    ret += (20.0 * sin(6.0 * x * pi) + 20.0 * sin(2.0 * x * pi)) * 2.0 / 3.0
    ret += (20.0 * sin(x * pi) + 40.0 * sin(x / 3.0 * pi)) * 2.0 / 3.0
    ret += (150.0 * sin(x / 12.0 * pi) + 300.0 * sin(x / 30.0 * pi)) * 2.0 / 3.0
    return ret
}

fun main(args: Array<String>) {
    // 北斗芯片获取的经纬度为WGS84地理坐标 31.426896,119.496145
    val gps = Gps(31.426896, 119.496145)
    println("gps :$gps")
    val gcj = gps.gps84_To_Gcj02()
    println("gcj :$gcj")
    val star = gcj.gcj_To_Gps84()
    println("star:$star")
    val bd = gcj.gcj02_To_Bd09()
    println("bd  :$bd")
    val gcj2 = bd.bd09_To_Gcj02()
    println("gcj :$gcj2")
}
