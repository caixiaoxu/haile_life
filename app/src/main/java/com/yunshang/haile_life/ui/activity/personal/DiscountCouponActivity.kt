package com.yunshang.haile_life.ui.activity.personal

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.lsy.framelib.ui.base.activity.BaseBindingActivity
import com.yunshang.haile_life.R
import com.yunshang.haile_life.databinding.ActivityDiscountCouponBinding
import com.yunshang.haile_life.ui.fragment.DiscountCouponFragment

class DiscountCouponActivity : BaseBindingActivity<ActivityDiscountCouponBinding>() {

    override fun layoutId(): Int = R.layout.activity_discount_coupon

    override fun backBtn(): View = mBinding.barDiscountCouponTitle.getBackBtn()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.WHITE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_discount_coupon_control, DiscountCouponFragment().apply {
                arguments = intent.extras
            }).commit()
    }
}