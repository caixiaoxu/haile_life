package com.yunshang.haile_life.data.entities

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.yunshang.haile_life.BR

/**
 * Title :
 * Author: Lsy
 * Date: 2023/12/29 14:13
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class EvaluateScoreTemplate(
    val id: Int?,
    val feedbackTemplateProjectDtos: MutableList<FeedbackTemplateProjectDto>? = null
)

data class FeedbackTemplateProjectDto(
    val name: String? = null,
    val templateId: Int? = null,
    val scoreType: Int? = null,
    var score: Int? = null,
    var id: Int? = null,
    var templateProjectId:Int? = null
) : BaseObservable() {

    @get:Bindable
    var scoreVal: Int
        get() = score ?: 0
        set(value) {
            score = value
            notifyPropertyChanged(BR.scoreVal)
        }

    fun changeScore(score: Int) {
        scoreVal = score
    }
}
