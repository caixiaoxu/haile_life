package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2024/1/2 15:25
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class EvaluateDetailsEntity(
    val id: Int? = null,
    val orderId: Int? = null,
    val orderNo: String? = null,
    val goodsId: Int? = null,
    val buyerId: Int? = null,
    val buyerPhone: String? = null,
    val sellerId: Int? = null,
    val sellerName: String? = null,
    val organizationId: String? = null,
    val organizationAccount: String? = null,
    val feedbackProjectListDtos: List<FeedbackTemplateProjectDto>? = null,
    val feedbackOrderTagModels: List<FeedbackOrderTagModel>? = null,
    val totalScore: Double? = null,
    val average: Double? = null,
    val type: Int? = null,
    val state: Int? = null,
    val viewReply: Int? = null,
    val reply: String? = null,
    val replyTime: String? = null,
    val replyName: String? = null,
    val createTime: String? = null,
    val deviceName: String? = null,
    val comment: String? = null,
    val goodsCategoryCode: String? = null,
    val goodsCategoryName: String? = null,
    val serviceTelephone: String? = null,
    val feedbackOrderReplayDtos: List<FeedbackOrderReplayDtos>? = null,
    val pictures: List<String>? = null,
)

data class FeedbackOrderTagModel(
    val id: Int? = null,
    val tagName: String? = null,
    val orderId: String? = null,
    val tagId: Int? = null,
    val deleteFlag: String? = null,
    val createTime: String? = null
)

data class FeedbackOrderReplayDtos(
    val id: Int? = null,
    val orderId: String? = null,
    val average: Double? = null,
    val type: Int? = null,
    val reply: String? = null,
    val replyId: Int? = null,
    val replyTime: String? = null,
    val replyName: String? = null,
    val viewReply: Int? = null,
    val viewTime: String? = null,
    val createTime: String? = null,
    val updateTime: String? = null,
    val pictures: List<String>? = null,
) {

    /**
     * 1平台回复 2商家回复 3用户追评
     */
    fun replyTitle(): String = "${
        when (type) {
            1 -> "平台回复"
            2 -> "商家回复"
            3 -> "追评"
            else -> ""
        }
    }：($replyTime)"

}
