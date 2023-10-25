package com.yunshang.haile_life

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.lsy.framelib.ui.base.activity.BaseActivity
import com.yunshang.haile_life.data.Constants
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.model.SPRepository
import com.yunshang.haile_life.databinding.ActivitySplashBinding
import com.yunshang.haile_life.ui.activity.MainActivity
import com.yunshang.haile_life.ui.view.dialog.CommonDialog
import com.yunshang.haile_life.utils.string.StringUtils
import com.yunshang.haile_life.web.WebViewActivity
import timber.log.Timber

class SplashActivity : BaseActivity() {
    private val delayTime = 1000L

    private val mSplashBinding: ActivitySplashBinding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun isFullScreen(): Boolean = true

    //隐私协议同意弹窗
    private val mAgreementDialog: CommonDialog by lazy {
        CommonDialog.Builder(
            StringUtils.formatMultiStyleStr(
                getString(R.string.agreement_hint),
                arrayOf(
                    ForegroundColorSpan(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.colorPrimary,
                            null
                        )
                    ),
                    object : ClickableSpan() {
                        override fun onClick(view: View) {
                            startActivity(
                                Intent(
                                    this@SplashActivity,
                                    WebViewActivity::class.java
                                ).apply {
                                    putExtras(
                                        IntentParams.WebViewParams.pack(
                                            Constants.agreement,
                                        )
                                    )
                                })
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            //去掉下划线
                            ds.isUnderlineText = false
                        }
                    },
                ), 17, 23
            )
        ).apply {
            negativeClickListener = View.OnClickListener {
                finish()
            }
            positiveClickListener = View.OnClickListener {
                SPRepository.isAgreeAgreement = true
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }
        }.build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mSplashBinding.root)
//        parseURL()
        initView()
    }

    /**
     * 处理跳转链接
     */
    private fun parseURL() {
        val intent = intent
        val action = intent.action
        if (Intent.ACTION_VIEW == action) {
            val uri: Uri? = intent.data
            if (uri != null) {
                Timber.i("path:${uri.path}")
                Timber.i("isRechargeShop:${uri.getQueryParameter("isRechargeShop")}")
            }
        }
    }

    private fun initView() {
        initStatusBarTxtColor(mSplashBinding.root)

        if (!SPRepository.isAgreeAgreement) {
            mAgreementDialog.show(supportFragmentManager)
        } else {
            checkDelayJump()
        }
    }

    /**
     * 延时跳转，根据是否登录判断
     */
    private fun checkDelayJump() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }, delayTime)
    }
}