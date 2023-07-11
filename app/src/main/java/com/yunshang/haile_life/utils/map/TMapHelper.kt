package com.yunshang.haile_life.utils.map

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import com.tencent.lbssearch.TencentSearch
import com.tencent.lbssearch.httpresponse.BaseObject
import com.tencent.lbssearch.`object`.param.SearchParam
import com.tencent.lbssearch.`object`.result.SearchResultObject
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.map.tools.net.http.HttpResponseListener
import com.tencent.tencentmap.mapsdk.maps.*
import com.tencent.tencentmap.mapsdk.maps.LocationSource.OnLocationChangedListener
import com.tencent.tencentmap.mapsdk.maps.TencentMap.OnMapLoadedCallback
import com.tencent.tencentmap.mapsdk.maps.model.LatLng

/**
 * Title :
 * Author: Lsy
 * Date: 2023/5/22 15:22
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
</desc></version></time></author> */
class TMapHelper : IMap, LocationSource, TencentLocationListener {
    private var mMapView: MapView? = null
    private var mTencentMap: TencentMap? = null
    private var mLocationManager: TencentLocationManager? = null
    private var mLocationChangedListener: OnLocationChangedListener? = null
    private val secretkey = "ebuVGZMAxQCBDwonHPvReX9EEJqB2p1f"
    override fun setAgreePrivacy(context: Context?) {
        TencentLocationManager.setUserAgreePrivacy(true)
        TencentMapInitializer.setAgreePrivacy(true)
    }

    override fun createMap(mContext: Context, parent: ViewGroup) {
        mMapView = MapView(mContext).also { mapView ->
            parent.addView(
                mMapView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            mTencentMap = mapView.map.also { tMap ->
                tMap.addOnMapLoadedCallback(OnMapLoadedCallback {})
                tMap.mapType = TencentMap.MAP_TYPE_NORMAL
                tMap.moveCamera(CameraUpdateFactory.zoomTo(17f))

                // ui控件
                val uiSettings = tMap.uiSettings
                uiSettings.setLogoScale(0.7f)
                uiSettings.isScaleViewEnabled = false
                uiSettings.isCompassEnabled = false
            }
        }
    }

    override fun initLocation(mContext: Context, callback: OnLocationCallback) {
        checkNotNull(mTencentMap) { "TencentMap 为空" }
        mLocationManager = TencentLocationManager.getInstance(mContext)
        mTencentMap?.setOnMyLocationChangeListener { location: Location ->
            callback.onLocation(location)
        }
        mTencentMap?.setLocationSource(this)
        mTencentMap?.isMyLocationEnabled = true
        mTencentMap?.setMyLocationClickListener { latLng: LatLng? -> true }
    }

    override fun onCreate(savedInstanceState: Bundle) {}
    override fun onStart() {
        mMapView?.onStart()
    }

    override fun onResume() {
        mMapView?.onResume()
    }

    override fun onPause() {
        mMapView?.onPause()
    }

    override fun onStop() {
        mMapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {}
    override fun onDestroy() {
        deactivate()
        mMapView?.onDestroy()
    }

    override fun searchPoiCity(
        mContext: Context, city: String, keyword: String,
        callBack: OnSearchPoiCallBack
    ) {
        TencentSearch(mContext, secretkey).search(SearchParam(
            keyword, SearchParam.Region(city).autoExtend(false)
        ), object : HttpResponseListener<BaseObject> {
            override fun onFailure(arg0: Int, msg: String, arg3: Throwable) {
                callBack.onPoiResult(false, msg, null)
            }

            override fun onSuccess(arg0: Int, obj: BaseObject?) {
                if (null == obj || null == (obj as SearchResultObject).data) {
                    callBack.onPoiResult(false, "数据为空", null)
                    return
                }
                callBack.onPoiResult(true, "", obj.data.map {
                    PoiResultData(it.title, it.address, Location("").apply {
                        latitude = it.latLng.getLatitude()
                        longitude = it.latLng.getLongitude()
                    }, it.distance)
                }.toList())
            }
        })
    }

    override fun searchPoiNearBy(
        mContext: Context, city: String, keyword: String, location: Location,
        callBack: OnSearchPoiCallBack
    ) {
        TencentSearch(mContext, secretkey).search(SearchParam(
            keyword, SearchParam.Nearby(
                LatLng(
                    location.latitude,
                    location.longitude
                ), 10000
            )
        ), object : HttpResponseListener<BaseObject?> {
            override fun onSuccess(i: Int, obj: BaseObject?) {
                if (null == obj || null == (obj as SearchResultObject).data) {
                    callBack.onPoiResult(false, "数据为空", null)
                    return
                }
                callBack.onPoiResult(true, "", obj.data.map {
                    PoiResultData(
                        it.title, it.address, Location("").apply {
                            latitude = it.latLng.getLatitude()
                            longitude = it.latLng.getLongitude()
                        },
                        it.distance
                    )
                }.toList())
            }

            override fun onFailure(i: Int, msg: String, throwable: Throwable) {
                callBack.onPoiResult(false, msg, null)
            }
        })
    }

    override fun activate(onLocationChangedListener: OnLocationChangedListener) {
        //这里我们将地图返回的位置监听保存为当前 Activity 的成员变量
        mLocationChangedListener = onLocationChangedListener
        //开启定位
        val err = mLocationManager?.requestSingleFreshLocation(
            null, this, Looper.myLooper()
        )
        when (err) {
            1 -> Log.e("定位失败", "设备缺少使用腾讯定位服务需要的基本条件")
            2 -> Log.e("定位失败", "manifest 中配置的 key 不正确")
            3 -> Log.e("定位失败", "自动加载libtencentloc.so失败")
            4 -> Log.e("定位失败", "隐私未权限")
            else -> {}
        }
    }

    override fun deactivate() {
        //当不需要展示定位点时，需要停止定位并释放相关资源
        if (null != mLocationManager) {
            mLocationManager!!.removeUpdates(this)
            mLocationManager = null
        }
        mLocationChangedListener = null
    }

    override fun onLocationChanged(tencentLocation: TencentLocation, i: Int, s: String) {
        if (i == TencentLocation.ERROR_OK) {
            //将位置信息返回给地图
            mLocationChangedListener?.onLocationChanged(Location(tencentLocation.provider).apply {
                //设置经纬度
                latitude = tencentLocation.latitude
                longitude = tencentLocation.longitude
                //设置精度，这个值会被设置为定位点上表示精度的圆形半径
                accuracy = tencentLocation.accuracy
                //设置定位标的旋转角度，注意 tencentLocation.getBearing() 只有在 gps 时才有可能获取
                bearing = tencentLocation.bearing
            })
        }
    }

    override fun onStatusUpdate(s: String, i: Int, s1: String) {}
}