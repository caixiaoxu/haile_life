package com.yunshang.haile_life.utils.map

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.ViewGroup

/**
 * Title :
 * Author: Lsy
 * Date: 2023/5/22 15:34
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
</desc></version></time></author> */
interface IMap {
    fun setAgreePrivacy(context: Context?)

    /**
     * 创建地图
     *
     * @return
     */
    fun createMap(mContext: Context, parent: ViewGroup)

    /**
     * 初始化定位
     */
    fun initLocation(mContext: Context, callback: OnLocationCallback)

    fun onCreate(savedInstanceState: Bundle)
    fun onStart()
    fun onResume()
    fun onPause()
    fun onStop()
    fun onSaveInstanceState(outState: Bundle)
    fun onDestroy()

    /**
     * 根据城市搜索
     */
    fun searchPoiCity(
        mContext: Context,
        city: String,
        keyword: String,
        callBack: OnSearchPoiCallBack
    )

    /**
     * 搜索附近
     */
    fun searchPoiNearBy(
        mContext: Context, city: String, keyword: String, location: Location,
        callBack: OnSearchPoiCallBack
    )
}

interface OnLocationCallback {
    fun onLocation(location: Location)
}

interface OnSearchPoiCallBack {
    fun onPoiResult(isSuccess: Boolean, msg: String, poiResultData: List<PoiResultData>?)
}

data class PoiResultData(
    var title: String?,
    var address: String?,
    var location: Location?,
    var distance: Double
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Location::class.java.classLoader),
        parcel.readDouble()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(address)
        parcel.writeParcelable(location, flags)
        parcel.writeDouble(distance)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PoiResultData> {
        override fun createFromParcel(parcel: Parcel): PoiResultData {
            return PoiResultData(parcel)
        }

        override fun newArray(size: Int): Array<PoiResultData?> {
            return arrayOfNulls(size)
        }
    }

}