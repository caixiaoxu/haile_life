package com.yunshang.haile_life

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.lsy.framelib.ui.base.activity.BaseActivity
import com.yunshang.haile_life.data.model.SPRepository
import com.yunshang.haile_life.databinding.ActivitySplashBinding
import com.yunshang.haile_life.ui.activity.MainActivity
import com.yunshang.haile_life.ui.activity.login.LoginActivity

class SplashActivity : BaseActivity() {
    private val delayTime = 1000L

    private val mSplashBinding: ActivitySplashBinding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun isFullScreen(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mSplashBinding.root)
        initView()
    }

    private fun initView() {
        initStatusBarTxtColor(mSplashBinding.root)
        checkDelayJump()
    }

    /**
     * 延时跳转，根据是否登录判断
     */
    private fun checkDelayJump() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (SPRepository.isLogin()) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
            finish()

        }, delayTime)
    }
}