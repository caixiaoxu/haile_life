package com.yunshang.haile_life.data.entities

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.yunshang.haile_life.BR

/**
 * Title :
 * Author: Lsy
 * Date: 2023/10/17 16:59
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class WaterControlCodeEntity(
    val accountPrefix: String? = null,
    val code: String? = null,
    var state: Int? = null
) : BaseObservable() {

    @get:Bindable
    var hasState: Boolean
        get() = null == state || 1 == state
        set(value) {
            state = if (value) 1 else 2
            notifyPropertyChanged(BR.hasState)
        }

    val hasCode: Boolean
        get() = !code.isNullOrEmpty()

    fun prefixIndex(index: Int): String = try {
        accountPrefix?.let { it[index].toString() } ?: ""
    } catch (e: Exception) {
        ""
    }

    fun codeIndex(index: Int): String =
        if (index < 4 && code.isNullOrEmpty()) prefixIndex(index) else try {
            code?.let { it[index].toString() } ?: "*"
        } catch (e: Exception) {
            "*"
        }
}