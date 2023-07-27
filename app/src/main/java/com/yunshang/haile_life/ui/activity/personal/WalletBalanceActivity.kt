package com.yunshang.haile_life.ui.activity.personal

import android.graphics.Color
import android.os.Bundle
import android.text.style.AbsoluteSizeSpan
import android.view.View
import com.lsy.framelib.ui.base.activity.BaseActivity
import com.lsy.framelib.utils.DimensionUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.databinding.ActivityWalletBalanceBinding
import com.yunshang.haile_life.utils.string.StringUtils

class WalletBalanceActivity : BaseActivity() {

    private val mWalletBinding: ActivityWalletBalanceBinding by lazy {
        ActivityWalletBalanceBinding.inflate(layoutInflater)
    }

    override fun backBtn(): View = mWalletBinding.barWalletBalanceTitle.getBackBtn()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mWalletBinding.root)
        window.statusBarColor = Color.WHITE
        IntentParams.WalletBalanceParams.parseBalance(intent)?.let {
            mWalletBinding.tvWalletBalanceValue.text = StringUtils.formatMultiStyleStr(
                com.lsy.framelib.utils.StringUtils.getString(R.string.unit_money) + " " + it,
                arrayOf(
                    AbsoluteSizeSpan(DimensionUtils.sp2px(24f))
                ), 0, 2
            )
        }
    }
}