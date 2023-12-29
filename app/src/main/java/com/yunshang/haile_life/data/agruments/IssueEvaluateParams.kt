package com.yunshang.haile_life.data.agruments

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.data.entities.EvaluateTagTemplate
import com.yunshang.haile_life.data.entities.FeedbackTemplateProjectDto

/**
 * Title :
 * Author: Lsy
 * Date: 2023/12/29 14:53
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class IssueEvaluateParams(
    var orderId: Int? = null,
    var orderNo: String? = null,
    var goodsId: Int? = null,
    var buyerId: Int? = null,
    var sellerId: Int? = null,
    var feedbackProjectListDtos: List<FeedbackTemplateProjectDto>? = null,
    var feedbackTagDtos: List<EvaluateTagTemplate>? = null,
    var pictures: List<String>? = null,
    var reply: String? = null,
) : BaseObservable() {

    @get:Bindable
    var replyVal: String
        get() = reply ?: ""
        set(value) {
            reply = value
            notifyPropertyChanged(BR.replyVal)
        }
}
