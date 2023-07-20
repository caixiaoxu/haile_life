package com.yunshang.haile_life.ui.activity.login

import android.content.Intent
import android.graphics.Color
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.ActivityUtils
import com.lsy.framelib.utils.AppManager
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.LoginViewModel
import com.yunshang.haile_life.data.Constants
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.databinding.ActivityLoginBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.MainActivity
import com.yunshang.haile_life.utils.ViewUtils
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
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
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
            AppManager.finishAllActivity()
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

        mBinding.ivLoginWechat.setOnClickListener {
            WeChatHelper.openWeChatLogin()
        }
    }

    override fun initData() {
        mBinding.shared = mSharedViewModel
    }

    override fun onBackListener() {
        ActivityUtils.doubleBack(this)
    }
}