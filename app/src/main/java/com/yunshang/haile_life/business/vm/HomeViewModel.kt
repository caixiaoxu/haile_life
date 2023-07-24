package com.yunshang.haile_life.business.vm

import android.location.Location
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.MarketingService
import com.yunshang.haile_life.business.apiService.MessageService
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.entities.ADEntity
import com.yunshang.haile_life.data.entities.MessageEntity
import com.yunshang.haile_life.data.entities.NearStoreEntity
import com.yunshang.haile_life.data.entities.StoreDeviceEntity
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.data.model.SPRepository

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/5 19:45
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class HomeViewModel : BaseViewModel() {
    private val mMessageRepo = ApiRepository.apiClient(MessageService::class.java)
    private val mMarketingRepo = ApiRepository.apiClient(MarketingService::class.java)
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)

    val bigCategory = arrayListOf(
        HomeCategory(R.mipmap.icon_home_wash, R.string.home_category_wash),
        HomeCategory(R.mipmap.icon_home_hair, R.string.home_category_hair),
        HomeCategory(R.mipmap.icon_home_drinking, R.string.home_category_drinking),
        HomeCategory(R.mipmap.icon_home_shower, R.string.home_category_shower),
        HomeCategory(R.mipmap.icon_home_dry_cleaning, R.string.home_category_dry_cleaning),
    )
    val smallCategory = arrayListOf(
        HomeCategory(R.mipmap.icon_home_cloth_recycle, R.string.home_category_cloth_recyce),
        HomeCategory(
            R.mipmap.icon_home_cloth_lease,
            R.string.home_category_hair,
            MutableLiveData<Boolean>(true)
        ),
        HomeCategory(R.mipmap.icon_home_cloth_care, R.string.home_category_drinking),
        HomeCategory(R.mipmap.icon_home_cloth_manager, R.string.home_category_shower),
        HomeCategory(
            R.mipmap.icon_home_student_entrepreneurship,
            R.string.home_category_student_entrepreneurship
        ),
    )

    val showCurTaskClose: MutableLiveData<Boolean> = MutableLiveData(true)

    val lastMessage: MutableLiveData<MessageEntity> by lazy {
        MutableLiveData()
    }

    val adEntity: MutableLiveData<ADEntity> by lazy {
        MutableLiveData()
    }

    val storeAdEntity: MutableLiveData<ADEntity> by lazy {
        MutableLiveData()
    }

    val goodsRecommendAdEntity: MutableLiveData<ADEntity> by lazy {
        MutableLiveData()
    }

    val studentRecommendAdEntity: MutableLiveData<ADEntity> by lazy {
        MutableLiveData()
    }

    val nearStoreEntity: MutableLiveData<NearStoreEntity> by lazy {
        MutableLiveData()
    }

    val storeDevices: MutableLiveData<MutableList<StoreDeviceEntity>> by lazy {
        MutableLiveData()
    }

    fun requestData() {
        launch({
            requestHomeMsg()
            requestAD()
            requestGoodsRecommendAD()
            requestStoreAD()
            requestStudentRecommendAD()
        })
    }

    /**
     * 首页消息
     */
    fun requestHomeMsgAsync() {
        launch({ requestHomeMsg() }, showLoading = false)
    }

    /**
     * 首页消息
     */
    private suspend fun requestHomeMsg() {
        if (SPRepository.isLogin()) {
            ApiRepository.dealApiResult(
                mMessageRepo.requestHomeMessage(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "page" to 1,
                            "pageSize" to 1,
                            "readStatus" to 0
                        )
                    )
                )
            )?.let {
                if (it.isNotEmpty()) {
                    lastMessage.postValue(it[0])
                }
            }
        }
    }

    /**
     * 首页banner
     */
    private suspend fun requestAD() {
        ApiRepository.dealApiResult(
            mMarketingRepo.requestAD(
                ApiRepository.createRequestBody(
                    hashMapOf(
                        "slotKey" to "9191"
                    )
                )
            )
        )?.let {
            adEntity.postValue(it)
        }
    }

    fun hideCurTask(v: View) {
        showCurTaskClose.value = false
    }

    /**
     * 商城广告位
     */
    private suspend fun requestStoreAD() {
        ApiRepository.dealApiResult(
            mMarketingRepo.requestAD(
                ApiRepository.createRequestBody(
                    hashMapOf(
                        "slotKey" to "mini_store"
                    )
                )
            )
        )?.let {
            storeAdEntity.postValue(it)
        }
    }

    /**
     * 好物推荐广告位
     */
    private suspend fun requestGoodsRecommendAD() {
        ApiRepository.dealApiResult(
            mMarketingRepo.requestAD(
                ApiRepository.createRequestBody(
                    hashMapOf(
                        "slotKey" to "mini_goods_recommend"
                    )
                )
            )
        )?.let {
            goodsRecommendAdEntity.postValue(it)
        }
    }

    /**
     * 学生专区广告位
     */
    private suspend fun requestStudentRecommendAD() {
        ApiRepository.dealApiResult(
            mMarketingRepo.requestAD(
                ApiRepository.createRequestBody(
                    hashMapOf(
                        "slotKey" to "mini_student_recommend"
                    )
                )
            )
        )?.let {
            studentRecommendAdEntity.postValue(it)
        }
    }

    /**
     * 附近门店
     */
    fun requestNearByStore(location: Location) {
        launch({
            ApiRepository.dealApiResult(
                mShopRepo.requestNearStores(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "page" to 1,
                            "pageSize" to 1,
                            "lng" to location.longitude,
                            "lat" to location.latitude,
                        )
                    )
                )
            )?.let {
                it.items.firstOrNull()?.let { e ->
                    nearStoreEntity.postValue(e)
                }
            }
        })
    }

    /**
     * 门店设备列表
     */
    fun requestStoreDevices() {
        if (null == nearStoreEntity.value?.id) {
            return
        }

        launch({
            ApiRepository.dealApiResult(
                mShopRepo.requestShopDevice(nearStoreEntity.value!!.id)
            )?.let {
                storeDevices.postValue(it)
            }
        })
    }
}

data class HomeCategory(
    val icon: Int,
    val name: Int,
    val hasTag: MutableLiveData<Boolean>? = null
)