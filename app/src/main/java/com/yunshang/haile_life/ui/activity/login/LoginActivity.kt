package com.yunshang.haile_life.ui.activity.login

import android.content.Intent
import android.graphics.Color
import android.view.View
import com.lsy.framelib.async.LiveDataBus
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.LoginViewModel
import com.yunshang.haile_life.data.Constants
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.databinding.ActivityLoginBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.utils.ViewUtils
import com.yunshang.haile_life.utils.thrid.AlipayHelper
import com.yunshang.haile_life.utils.thrid.WeChatHelper
import com.yunshang.haile_life.web.WebViewActivity
import timber.log.Timber

class LoginActivity : BaseBusinessActivity<ActivityLoginBinding, LoginViewModel>(
    LoginViewModel::class.java, BR.vm
) {

    override fun layoutId(): Int = R.layout.activity_login

    override fun needSoftHideKeyBoard(): Boolean = false

    override fun initEvent() {
        super.initEvent()

        LiveDataBus.with(BusEvents.LOGIN_STATUS)?.observe(this) {
//            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }

        LiveDataBus.with(BusEvents.WECHAT_LOGIN, String::class.java)?.observe(this) {
            Timber.i("微信授权登录,code:$it")
            launch({
                mSharedViewModel.thirdLogin(8, it) { code ->
                    startActivity(Intent(this@LoginActivity, BindPhoneActivity::class.java).apply {
                        putExtras(IntentParams.BindPhoneParams.pack(code, 8))
                    })
                }
            })
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE
        mBinding.barLoginTitle.getBackBtn()
            .setCompoundDrawablesRelativeWithIntrinsicBounds(R.mipmap.icon_close, 0, 0, 0)
        mBinding.barLoginTitle.setOnBackListener {
            finish()
        }

        // 协议内容
        val content = resources.getString(R.string.login_agreement)
        val end = content.length
        val start = end - 7
        ViewUtils.initAgreementToTextView(mBinding.tvLoginAgreement, content, start, end) {
            // 跳转隐私协议
            startActivity(
                Intent(
                    this@LoginActivity,
                    WebViewActivity::class.java
                ).apply {
                    putExtras(
                        IntentParams.WebViewParams.pack(
                            Constants.agreement,
                        )
                    )
                })
        }
        // 微信和支付宝登录
        val isAlipayInstall = AlipayHelper.isAlipayInstalled(this)
        val isWxInstall = WeChatHelper.isWxInstall

        mBinding.ivLoginAlipay.visibility =
            if (isAlipayInstall) View.VISIBLE else View.GONE
        mBinding.ivLoginAlipay.setOnClickListener {
            mViewModel.aliPayAuth(this@LoginActivity) {
                mSharedViewModel.thirdLogin(7, it) { code ->
                    startActivity(
                        Intent(
                            this@LoginActivity,
                            BindPhoneActivity::class.java
                        ).apply {
                            putExtras(IntentParams.BindPhoneParams.pack(code, 7))
                        })
                }
            }
        }

        mBinding.ivLoginWechat.visibility =
            if (isWxInstall) View.VISIBLE else View.GONE
        mBinding.ivLoginWechat.setOnClickListener {
            WeChatHelper.openWeChatLogin()
        }
        mBinding.tvLoginThirdLoginTitle.visibility =
            if (isAlipayInstall || isWxInstall) View.VISIBLE else View.GONE
    }

    override fun initData() {
        mBinding.shared = mSharedViewModel
    }

    override fun onDestroy() {
        mViewModel.timer?.cancel()
        super.onDestroy()
    }
}