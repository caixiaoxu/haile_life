package com.yunshang.haile_life.ui.activity.common

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.lsy.framelib.ui.base.activity.BaseActivity
import com.lsy.framelib.ui.base.activity.BaseBindingActivity
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.databinding.ActivityPicBrowseBinding
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.loadImage

class PicBrowseActivity : BaseBindingActivity<ActivityPicBrowseBinding>() {

    override fun layoutId(): Int  = R.layout.activity_pic_browse

    override fun backBtn(): View = mBinding.barPicBrowseTitle.getBackBtn()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        window.statusBarColor = Color.WHITE

        mBinding.ivPicBrowse.loadImage(url = IntentParams.PicParams.parseUrl(intent))
    }
}