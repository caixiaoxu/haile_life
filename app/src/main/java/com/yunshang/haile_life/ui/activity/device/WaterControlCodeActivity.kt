package com.yunshang.haile_life.ui.activity.device

import android.view.View
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.WaterControlCodeViewModel
import com.yunshang.haile_life.databinding.ActivityWaterControlCodeBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.dialog.CommonDialog

class WaterControlCodeActivity :
    BaseBusinessActivity<ActivityWaterControlCodeBinding, WaterControlCodeViewModel>(
        WaterControlCodeViewModel::class.java, BR.vm
    ) {

    override fun layoutId(): Int = R.layout.activity_water_control_code

    override fun backBtn(): View = mBinding.barWaterControlCodeTitle.getBackBtn()

    override fun initView() {

        mBinding.btnWaterControlCodeEnable.setOnClickListener {
            if (1 == mViewModel.waterControlCode.value?.state){
                CommonDialog.Builder("您确定要关闭水控码功能吗？").apply {
                    title="确认提示"
                    negativeTxt = StringUtils.getString(R.string.cancel)
                    setPositiveButton(StringUtils.getString(R.string.sure)){
                        mViewModel.updateWaterControlCodeState(2){
                            SToast.showToast(it.context,"您已关闭水控码功能")
                        }
                    }
                }.build().show(supportFragmentManager)
            } else {
                mViewModel.updateWaterControlCodeState(1){
                    SToast.showToast(it.context,"您已开启水控码功能")
                }
            }
        }
    }

    override fun initData() {
        mViewModel.requestData()
    }
}