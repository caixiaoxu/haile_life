package com.yunshang.haile_life.business.vm

import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.CommonService
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.data.agruments.IssueEvaluateParams
import com.yunshang.haile_life.data.entities.EvaluateTagTemplate
import com.yunshang.haile_life.data.entities.FeedbackTemplateProjectDto
import com.yunshang.haile_life.data.model.ApiRepository
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

    val issueEvaluateParams: MutableLiveData<IssueEvaluateParams> =
        MutableLiveData(IssueEvaluateParams())

    val evaluateScoreTemplates: MutableLiveData<List<FeedbackTemplateProjectDto>> by lazy {
        MutableLiveData()
    }

    var evaluateTagTemplates: List<EvaluateTagTemplate> = mutableListOf()

    val showEvaluateTagTemplates: MutableLiveData<List<EvaluateTagTemplate>> by lazy {
        MutableLiveData()
    }

    val evaluatePics: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())

    fun requestData() {
        launch({
            val tagList = mutableListOf<EvaluateTagTemplate>()
            for (i in 1..3) {
                ApiRepository.dealApiResult(
                    mOrderRepo.requestEvaluateTagTemplate(
                        ApiRepository.createRequestBody(
                            hashMapOf(
                                "feedbackLevel" to i
                            )
                        )
                    )
                )?.let {
                    tagList.addAll(it)
                }
            }
            evaluateTagTemplates = tagList

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
        })
    }

    fun changeScore(scoreTemp: FeedbackTemplateProjectDto, score: Int) {
        scoreTemp.scoreVal = score

        launch({
            evaluateScoreTemplates.value?.let { scoreList ->
                var scoreTotal = 0.0
                scoreList.forEach {
                    scoreTotal += it.scoreVal
                }
                scoreTotal /= scoreList.size
                //差评：[1-2.5)；中评：[2.5-3.5]；好评：(3.5-5]
                val level = if (scoreTotal <= 2.5) 3 else if (scoreTotal > 3.5) 1 else 2
                // 保留已选择的
                val list =
                    showEvaluateTagTemplates.value?.filter { item -> item.selectVal }
                        ?.toMutableList()
                        ?: mutableListOf()
                val levelTagList = evaluateTagTemplates.filter { item -> item.type == level }
                list.removeAll(levelTagList)
                // 加上当前返回的
                list.addAll(levelTagList)
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
}