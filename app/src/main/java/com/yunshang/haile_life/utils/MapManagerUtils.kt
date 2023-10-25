package com.yunshang.haile_life.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.lsy.framelib.utils.AppPackageUtils
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Title :
 * Author: Lsy
 * Date: 2023/10/16 11:49
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
object MapManagerUtils {
    private val PN_GAODE_MAP = "com.autonavi.minimap"// 高德地图包名
    private val PN_BAIDU_MAP = "com.baidu.BaiduMap" // 百度地图包名
    private val PN_TENCENT_MAP = "com.tencent.map" // 腾讯地图包名

    /**
     * 检测是否安装高德地图
     */
    fun checkGeoMapEInstall(context: Context): Boolean =
        AppPackageUtils.checkAppInstalled(context, PN_GAODE_MAP)

    /**
     * 跳转到高德地图，没有安装跳转到应用市场
     */
    fun goToGeoMap(context: Context, lat: Double, lng: Double, name: String) {
        //判断是否安装高德地图
        if (checkGeoMapEInstall(context)) {
            val uri =
                Uri.parse("amapuri://route/plan/?sourceApplication=haile_life&dlat=${lat}&dlon=${lng}&dname=${name}&dev=0&t=0")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            AppPackageUtils.openApplicationMarket(context, PN_GAODE_MAP)
        }
    }

    /**
     * 检测是否安装腾讯地图
     */
    fun checkTencentMapEInstall(context: Context): Boolean =
        AppPackageUtils.checkAppInstalled(context, PN_TENCENT_MAP)

    /**
     * 跳转到腾讯地图，没有安装跳转到应用市场
     */
    fun goToTencentMap(context: Context, lat: Double, lng: Double, name: String) {
        //判断是否安装高德地图
        if (checkTencentMapEInstall(context)) {
            val uri =
                Uri.parse("qqmap://map/routeplan?type=drive&fromcoord=CurrentLocation&tocoord=${lat},${lng}&to=${name}&referer=BOOBZ-ISOKZ-TXYX2-TNK3M-J7TVT-SYF4H")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            AppPackageUtils.openApplicationMarket(context, PN_TENCENT_MAP)
        }
    }


    /**
     * 检测是否安装百度地图
     */
    fun checkBaiduMapEInstall(context: Context): Boolean =
        AppPackageUtils.checkAppInstalled(context, PN_BAIDU_MAP)

    /**
     * 跳转到百度地图，没有安装跳转到应用市场
     */
    fun goToBaiduMap(context: Context, lat: Double, lng: Double, name: String) {
        //判断是否安装高德地图
        if (checkBaiduMapEInstall(context)) {
            val uri =
                Uri.parse("baidumap://map/direction?destination=name:${name}|latlng:${lat},${lng}&coord_type=gcj02&src=andr.yunshang.haile_life&to=name")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            AppPackageUtils.openApplicationMarket(context, PN_BAIDU_MAP)
        }
    }


    /**
     * 调用高德地图和腾讯地图需要传入GCJ02坐标系坐标，调用百度地图需要传入BD09坐标系坐标
     *
     * BD-09 坐标转换成 GCJ-02 坐标
     */
    fun BD2GCJ(gd: LatLng): LatLng {
        val x: Double = gd.longitude - 0.0065
        val y: Double = gd.latitude - 0.006
        val z = sqrt(x * x + y * y) - 0.00002 * sin(y * Math.PI)
        val theta = atan2(y, x) - 0.000003 * cos(x * Math.PI)
        val lng = z * cos(theta) //lng
        val lat = z * sin(theta) //lat
        return LatLng(lat, lng)
    }

    /**
     * GCJ-02 坐标转换成 BD-09 坐标
     */
    fun GCJ2BD(gd: LatLng): LatLng {
        val x: Double = gd.longitude
        val y: Double = gd.latitude
        val z = sqrt(x * x + y * y) + 0.00002 * sin(y * Math.PI)
        val theta = atan2(y, x) + 0.000003 * cos(x * Math.PI)
        val tempLon = z * cos(theta) + 0.0065
        val tempLat = z * sin(theta) + 0.006
        return LatLng(tempLat, tempLon)
    }

}