package com.yunshang.haile_life.data.entities

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.yunshang.haile_life.BR

/**
 * Title :
 * Author: Lsy
 * Date: 2023/12/29 14:25
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class EvaluateTagTemplate(
    val id: Int,
    val name: String,
    val type: Int,
) : BaseObservable() {
    @Transient
    @get:Bindable
    var selectVal: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.selectVal)
        }
}
