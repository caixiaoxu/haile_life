package com.yunshang.haile_life.ui.activity.login

import android.content.Intent
import android.graphics.Color
import android.view.View
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.BindPhoneViewModel
import com.yunshang.haile_life.data.Constants
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.databinding.ActivityBindPhoneBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.dialog.CommonDialog
import com.yunshang.haile_life.utils.ViewUtils
import com.yunshang.haile_life.web.WebViewActivity

class BindPhoneActivity : BaseBusinessActivity<ActivityBindPhoneBinding, BindPhoneViewModel>(
    BindPhoneViewModel::class.java, BR.vm
) {

    override fun layoutId(): Int = R.layout.activity_bind_phone

    override fun backBtn(): View = mBinding.barBindPhoneTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.authCode = IntentParams.BindPhoneParams.parseCode(intent)
        mViewModel.loginType = IntentParams.BindPhoneParams.parseLoginType(intent)
    }

    override fun initEvent() {
        super.initEvent()
        mViewModel.bindPhoneSuccess.observe(this) {
            mSharedViewModel.initLoginInfo(
                it.tokenLicenseDto.organizationId,
                it.tokenLicenseDto.organizationType,
                it.tokenLicenseDto.token,
                it.tokenLicenseDto.userId
            )
            LiveDataBus.post(BusEvents.LOGIN_STATUS, true)
            finish()
        }
        mViewModel.jump.observe(this) {
            val thirdName = if (7 == mViewModel.loginType) "支付宝" else "微信"
            CommonDialog.Builder("该手机号已绑定其他${thirdName}授权，是否替换成本${thirdName}授权。").apply {
                title = "提示"
                negativeTxt = StringUtils.getString(R.string.cancel)
                setPositiveButton("确认") {
                    mViewModel.requestBindPhoneAsync()
                }
            }.build().show(supportFragmentManager)
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE
        // 协议内容
        val content = resources.getString(R.string.login_agreement)
        val end = content.length
        val start = end - 7
        ViewUtils.initAgreementToTextView(mBinding.tvBindPhoneAgreement, content, start, end) {
            // 跳转隐私协议
            startActivity(
                Intent(
                    this@BindPhoneActivity,
                    WebViewActivity::class.java
                ).apply {
                    putExtras(
                        IntentParams.WebViewParams.pack(
                            Constants.agreement,
                        )
                    )
                })
        }
    }

    override fun initData() {
    }
}