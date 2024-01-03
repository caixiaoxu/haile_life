package com.yunshang.haile_life.business.vm

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.business.apiService.CommonService
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.data.agruments.IssueEvaluateParams
import com.yunshang.haile_life.data.entities.EvaluateTagTemplate
import com.yunshang.haile_life.data.entities.FeedbackOrderTagModel
import com.yunshang.haile_life.data.entities.FeedbackTemplateProjectDto
import com.yunshang.haile_life.data.model.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Title :
 * Author: Lsy
 * Date: 2023/12/28 17:53
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class IssueEvaluateViewModel : BaseViewModel() {
    private val mCommonRepo = ApiRepository.apiClient(CommonService::class.java)
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)

    var orderShopPhone: String? = null

    var isAdd: Boolean = false
    var originScoreList: List<FeedbackTemplateProjectDto>? = null
    var originTagList: List<FeedbackOrderTagModel>? = null

    val issueEvaluateParams: MutableLiveData<IssueEvaluateParams> =
        MutableLiveData(IssueEvaluateParams())

    val evaluateScoreTemplates: MutableLiveData<List<FeedbackTemplateProjectDto>> by lazy {
        MutableLiveData()
    }

    var evaluateTagTemplates: MutableMap<Int, MutableList<EvaluateTagTemplate>> = mutableMapOf()

    val showEvaluateTagTemplates: MutableLiveData<List<EvaluateTagTemplate>> by lazy {
        MutableLiveData()
    }

    val evaluatePics: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())

    fun requestData() {
        launch({
            for (i in 1..3) {
                ApiRepository.dealApiResult(
                    mOrderRepo.requestEvaluateTagTemplate(
                        ApiRepository.createRequestBody(
                            hashMapOf(
                                "feedbackLevel" to i
                            )
                        )
                    )
                )?.let { list ->
                    // 如果是已经选中的状态，修改状态
                    evaluateTagTemplates[i] = list
                }
            }

            if (originScoreList.isNullOrEmpty()) {
                ApiRepository.dealApiResult(
                    mOrderRepo.requestEvaluateScoreTemplate(
                        ApiRepository.createRequestBody(
                            hashMapOf(
                                "sceneId" to 1,//1.校园洗
                                "type" to 1,//1.订单
                            )
                        )
                    )
                )?.let {
                    evaluateScoreTemplates.postValue(it.feedbackTemplateProjectDtos?.filter { item -> 1 == item.scoreType })
                }
            } else {
                evaluateScoreTemplates.postValue(originScoreList)
            }
        })
    }

    fun calculateScoreTotal(): Int =
        evaluateScoreTemplates.value?.let { scoreList ->
            var scoreTotal = 0.0
            scoreList.forEach {
                scoreTotal += it.scoreVal
            }
            scoreTotal /= scoreList.size
            //差评：[1-2.5)；中评：[2.5-3.5]；好评：(3.5-5]
            if (0.0 == scoreTotal) 0 else if (scoreTotal <= 2.5) 3 else if (scoreTotal > 3.5) 1 else 2
        } ?: 0

    fun changeScore(scoreTemp: FeedbackTemplateProjectDto, score: Int) {
        scoreTemp.scoreVal = score
        refreshTagList()
    }

    fun refreshTagList() {
        launch({
            val level = calculateScoreTotal()
            // 保留已选择的
            val list =
                showEvaluateTagTemplates.value?.filter { item -> item.selectVal }
                    ?.toMutableList()
                    ?: mutableListOf()
            if (level == 0) {
                showEvaluateTagTemplates.postValue(null)
            } else {
                val levelTagList = evaluateTagTemplates[level]
                levelTagList?.let {
                    levelTagList.forEach { levelTag ->
                        levelTag.selectVal =
                            list.contains(levelTag) || null != originTagList?.find { item -> item.tagId == levelTag.id }
                    }
                    list.removeAll(levelTagList)
                    // 加上当前返回的
                    list.addAll(levelTagList)
                }
                showEvaluateTagTemplates.postValue(list)
            }

        }, showLoading = false)
    }

    /**
     * 上传头像
     */
    fun uploadEvaluatePic(paths: List<String>) {
        launch({
            var list = mutableListOf<String>()
            paths.forEach { path ->
                ApiRepository.dealApiResult(
                    mCommonRepo.updateLoadFile(
                        ApiRepository.createFileUploadBody(path)
                    )
                )?.let {
                    Timber.i("图片上传成功：$it")
                    list.add(it)
                }
            }

            evaluatePics.value?.let {
                it.addAll(list)
                list = it
            }

            evaluatePics.postValue(list)
        })
    }

    fun submit(context: Context) {
        if (null == issueEvaluateParams.value) return

        issueEvaluateParams.value?.feedbackProjectListDtos = evaluateScoreTemplates.value
        issueEvaluateParams.value?.feedbackTagDtos =
            showEvaluateTagTemplates.value?.filter { item -> item.selectVal }
        issueEvaluateParams.value?.pictures = evaluatePics.value

        launch({
            ApiRepository.dealApiResult(
                if (isAdd) {
                    mOrderRepo.submitAddEvaluate(
                        ApiRepository.createRequestBody(
                            GsonUtils.any2Json(issueEvaluateParams.value)
                        )
                    )
                } else {
                    mOrderRepo.submitEvaluate(
                        ApiRepository.createRequestBody(
                            GsonUtils.any2Json(issueEvaluateParams.value)
                        )
                    )
                }
            )

            withContext(Dispatchers.Main) {
                SToast.showToast(context, "评价成功")
            }
            LiveDataBus.post(BusEvents.EVALUATE_SUCCESS_STATUS, true)
            jump.postValue(if (isAdd) 0 else 1)
        })
    }
}