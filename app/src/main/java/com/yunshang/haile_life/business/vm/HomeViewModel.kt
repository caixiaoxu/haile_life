package com.yunshang.haile_life.business.vm

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.MarketingService
import com.yunshang.haile_life.business.apiService.MessageService
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.data.DeviceCategory
import com.yunshang.haile_life.data.entities.ADEntity
import com.yunshang.haile_life.data.entities.MessageEntity
import com.yunshang.haile_life.data.entities.NearStoreEntity
import com.yunshang.haile_life.data.entities.StoreDeviceEntity
import com.yunshang.haile_life.data.model.ApiRepository

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

    val lastMessage: MutableLiveData<MessageEntity> by lazy {
        MutableLiveData()
    }

    val adEntity: MutableLiveData<ADEntity> by lazy {
        MutableLiveData()
    }

    val nearStoreEntity: MutableLiveData<NearStoreEntity> by lazy {
        MutableLiveData()
    }

    val storeDevices: MutableLiveData<MutableList<StoreDeviceEntity>> by lazy {
        MutableLiveData()
    }

    val goodsRecommendList = arrayListOf(
        GoodsRecommend(
            "1650698183995617282",
            "https://static.haier-ioc.com/images/dashboard/hw_1.png",
            "潮流服饰"
        ),
        GoodsRecommend(
            "1650698249267376130",
            "https://static.haier-ioc.com/images/dashboard/hw_2.png",
            "电子数码"
        ),
        GoodsRecommend(
            "1650698340661260290",
            "https://static.haier-ioc.com/images/dashboard/hw_3.png",
            "日常洗护"
        ),
        GoodsRecommend(
            "1650732225545695233",
            "https://static.haier-ioc.com/images/dashboard/hw_4.png",
            "生活用品"
        ),
    )

    val studentRecommendList = arrayListOf(
        GoodsRecommend(
            "1650701908705341442",
            "https://static.haier-ioc.com/images/dashboard/goods/good_7.jpg",
            "MR.CLEARE懒人洗鞋泡泡粉"
        ),
        GoodsRecommend(
            "1650699343200579585",
            "https://static.haier-ioc.com/images/dashboard/goods/good_2.jpg",
            "卡蛙小方盒除湿机KW-CS01"
        ),
        GoodsRecommend(
            "1650699135309901826",
            "https://static.haier-ioc.com/images/dashboard/goods/good_3.jpg",
            "卡蛙X台风桌面摇头风扇"
        ),
        GoodsRecommend(
            "1650699132633935874",
            "https://static.haier-ioc.com/images/dashboard/goods/good_4.jpeg",
            "快乐小鸡Mini隐形眼镜清洗盒"
        ),
        GoodsRecommend(
            "1650700433019170818",
            "https://static.haier-ioc.com/images/dashboard/goods/good_5.jpg",
            "云裳澜锦轻薄透气男防晒服"
        ),
        GoodsRecommend(
            "1650701906624966658",
            "https://static.haier-ioc.com/images/dashboard/goods/good_6.jpg",
            "lassdear氨基酸去屑洗发水"
        ),
        GoodsRecommend(
            "1650699132633935874",
            "https://static.haier-ioc.com/images/dashboard/goods/good_8.jpg",
            "快乐小鸡Mini隐形眼镜清洗盒"
        ),
        GoodsRecommend(
            "1650702088745840642",
            "https://static.haier-ioc.com/images/dashboard/goods/good_9.jpg",
            "MR.CLEARE去渍剂套装"
        ),
        GoodsRecommend(
            "1650699137432219649",
            "https://static.haier-ioc.com/images/dashboard/goods/good_1.jpg",
            "颈部按摩器"
        ),
        GoodsRecommend(
            "1650699144579313666",
            "https://static.haier-ioc.com/images/dashboard/goods/good_10.jpg",
            "Nuby颈部按摩器"
        ),
    )

    fun requestData() {
        launch({
            requestHomeMsg()
            requestAD()
        })
    }

    /**
     * 首页消息
     */
    fun requestHomeMsgAsync() {
        launch({ requestHomeMsg() })
    }

    /**
     * 首页消息
     */
    private suspend fun requestHomeMsg() {
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
                storeDevices.postValue(it.filter { item -> DeviceCategory.canShowDeviceCategory(item.categoryCode) }
                    .toMutableList())
            }
        })
    }
}

data class HomeCategory(
    val icon: Int,
    val name: Int,
    val hasTag: MutableLiveData<Boolean>? = null
)

data class GoodsRecommend(
    val id: String,
    val icon: String,
    val name: String,
)