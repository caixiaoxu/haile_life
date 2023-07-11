package com.yunshang.haile_life.ui.fragment

import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.MineViewModel
import com.yunshang.haile_life.business.vm.StoreViewModel
import com.yunshang.haile_life.databinding.FragmentMineBinding
import com.yunshang.haile_life.databinding.FragmentStoreBinding

/**
 * Title :
 * Author: Lsy
 * Date: 2023/4/7 13:58
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class StoreFragment : BaseBusinessFragment<FragmentStoreBinding, StoreViewModel>(
    StoreViewModel::class.java,BR.vm
) {

    override fun layoutId(): Int = R.layout.fragment_store

    override fun initView() {}

    override fun initData() {
    }

}