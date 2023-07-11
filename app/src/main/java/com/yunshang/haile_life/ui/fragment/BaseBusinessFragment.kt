package com.yunshang.haile_life.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.ui.base.fragment.BaseVMFragment
import com.yunshang.haile_life.business.vm.SharedViewModel

/**
 * Title :
 * Author: Lsy
 * Date: 2023/4/10 09:51
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
abstract class BaseBusinessFragment<T : ViewDataBinding, VM : BaseViewModel>(
    clz: Class<VM>,
    private val vId: Int? = null
) : BaseVMFragment<T, VM>(clz) {

    // 贯穿整个项目的（只会让App(Application)初始化一次）
    protected val mSharedViewModel: SharedViewModel by lazy {
        getAppViewModelProvider()[SharedViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
        vId?.let {
            mBinding.setVariable(it, mViewModel)
        }
        initData()
    }

    /**
     * 初始化界面
     */
    abstract fun initView()

    /**
     * 初始化监听事件
     */
    protected open fun initEvent() {}

    /**
     * 初始化数据
     */
    abstract fun initData()
}