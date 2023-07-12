package com.yunshang.haile_life.ui.activity.shop

import com.lsy.framelib.async.LiveDataBus
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.StarfishRefundViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.databinding.ActivityStarfishRefundBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity

class StarfishRefundActivity : BaseBusinessActivity<ActivityStarfishRefundBinding, StarfishRefundViewModel>(
    StarfishRefundViewModel::class.java,BR.vm
) {
    override fun layoutId(): Int =R.layout.activity_starfish_refund

    override fun initIntent() {
        super.initIntent()
        mViewModel.shopIdList = IntentParams.StarfishRefundParams.parseShopIdList(intent)
        mViewModel.totalAmount = IntentParams.StarfishRefundParams.parseTotalAmount(intent)
    }

    override fun initEvent() {
        super.initEvent()
        LiveDataBus.with(BusEvents.STARFISH_REFUND_STATUS)?.observe(this){
            finish()
        }
    }

    override fun initView() {


    }

    override fun initData() {
    }
}