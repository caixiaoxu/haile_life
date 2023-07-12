package com.yunshang.haile_life.ui.activity.personal

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.lsy.framelib.ui.base.activity.BaseActivity
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.databinding.ActivityWalletBalanceBinding

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
            mWalletBinding.tvWalletBalanceValue.text = it
        }
    }
}