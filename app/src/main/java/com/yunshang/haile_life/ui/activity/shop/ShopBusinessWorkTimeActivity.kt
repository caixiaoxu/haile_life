package com.yunshang.haile_life.ui.activity.shop

import android.graphics.Color
import android.os.Bundle
import com.lsy.framelib.ui.base.activity.BaseActivity
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.BusinessHourEntity
import com.yunshang.haile_life.databinding.ActivityShopBusinessWorkTimeBinding
import com.yunshang.haile_life.databinding.ItemShopBusinessWorkTimeBinding

class ShopBusinessWorkTimeActivity : BaseActivity() {
    private val mShopBusinessWorkTimeBinding: ActivityShopBusinessWorkTimeBinding by lazy {
        ActivityShopBusinessWorkTimeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mShopBusinessWorkTimeBinding.root)
        window.statusBarColor = Color.WHITE

        val workTimeArr = IntentParams.ShopWorkTimeParams.parseWorkTimeArr(intent)
        mShopBusinessWorkTimeBinding.llShopBusinessWorkTimeArr.buildChild<ItemShopBusinessWorkTimeBinding, BusinessHourEntity>(
            workTimeArr
        ) { _, childBinding, data ->
            childBinding.child = data
        }
    }
}