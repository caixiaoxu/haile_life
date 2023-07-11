package com.yunshang.haile_life.ui.activity.order

import android.os.Bundle
import android.text.style.AbsoluteSizeSpan
import android.view.View
import com.lsy.framelib.ui.base.activity.BaseBindingActivity
import com.lsy.framelib.utils.DimensionUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.data.DeviceCategory
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.databinding.ActivityOrderPaySuccessBinding
import com.yunshang.haile_life.utils.DateTimeUtils
import com.yunshang.haile_life.utils.string.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class OrderPaySuccessActivity : BaseBindingActivity<ActivityOrderPaySuccessBinding>() {
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)

    override fun layoutId(): Int = R.layout.activity_order_pay_success

    override fun backBtn(): View = mBinding.barPaySuccessTitle.getBackBtn()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        IntentParams.OrderParams.parseOrderNo(intent)?.let {
            launch({
                ApiRepository.dealApiResult(mOrderRepo.requestOrderDetail(it))?.let {
                    withContext(Dispatchers.Main) {
                        mBinding.tvPaySuccessAmount.text = StringUtils.formatMultiStyleStr(
                            "¥ ${it.realPrice}", arrayOf(
                                AbsoluteSizeSpan(
                                    DimensionUtils.sp2px(16f, this@OrderPaySuccessActivity),
                                )
                            ), 0, 1
                        )
                        mBinding.tvPaySuccessPrompt.text =
                            if (it.orderItemList.isNotEmpty()) {
                                if (it.appointmentUsageTime.isNullOrEmpty()) {
                                    com.lsy.framelib.utils.StringUtils.getString(
                                        R.string.pay_success_hint,
                                        DateTimeUtils.formatDateTimeForStr(
                                            it.orderItemList.first().finishTime,
                                            "HH:mm"
                                        ),
                                        DeviceCategory.categoryName(it.orderItemList.first().categoryCode)
                                            .replace("机", "")
                                    )
                                } else {
                                    val useDate =
                                        DateTimeUtils.formatDateFromString(it.appointmentUsageTime)
                                    com.lsy.framelib.utils.StringUtils.getString(
                                        R.string.appoint_pay_success_hint,
                                        if (DateTimeUtils.isSameDay(useDate, Date())) {
                                            "今天${DateTimeUtils.formatDateTime(useDate, "HH:mm")}"
                                        } else it,
                                        it.orderItemList.first().goodsName
                                    )
                                }
                            } else ""
                    }
                }
            })
        }

        mBinding.btnPaySuccess.setOnClickListener {
            finish()
        }
    }
}