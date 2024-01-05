package com.yunshang.haile_life.data.model

import android.os.Handler
import android.os.Looper
import com.lsy.framelib.network.ApiService
import com.lsy.framelib.network.DefaultOkHttpClient
import com.lsy.framelib.network.exception.CommonCustomException
import com.lsy.framelib.network.interceptors.OkHttpBodyLogInterceptor
import com.lsy.framelib.network.interceptors.ProgressInterceptor
import com.lsy.framelib.network.interceptors.ResponseInterceptor
import com.lsy.framelib.network.response.ResponseWrapper
import com.lsy.framelib.utils.FileUtils
import com.lsy.framelib.utils.StringUtils
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.BuildConfig
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.DownloadService
import com.yunshang.haile_life.utils.ActivityManagerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

/**
 * Title : Repository层，获取数据来源
 * Author: Lsy
 * Date: 2023/3/17 17:15
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
object ApiRepository {
    private val mHandler = Handler(Looper.getMainLooper())
    private fun env() =
        if (BuildConfig.DEBUG && !SPRepository.selectEnv.isNullOrEmpty()) SPRepository.selectEnv!! else BuildConfig.BASE_URL

    /**
     * 获取网络请求Retrofit
     * @param service api Service
     */
    fun <T> apiClient(service: Class<T>): T {
        return ApiService.get(env(), service)
    }

    fun downloadClient(progressInterceptor: (curSize: Long, totalSize: Long, isDone: Boolean) -> Unit): DownloadService {
        return ApiService.get(
            env(), DownloadService::class.java, DefaultOkHttpClient()
                .setInterceptors(
                    arrayOf(
                        OkHttpBodyLogInterceptor().getInterceptor(),
                        ResponseInterceptor().getInterceptor()
                    )
                ).setNetworkInterceptors(
                    arrayOf(
                        ProgressInterceptor(progressInterceptor)
                    )
                ).getClient()
        )
    }

    /**
     * 生成请求body
     */
    fun createRequestBody(params: Map<String, Any?>): RequestBody =
        createRequestBody(GsonUtils.any2Json(params))


    /**
     * 生成请求body
     */
    fun createRequestBody(paramsJson: String): RequestBody =
        RequestBody.create(MediaType.parse("application/json"), paramsJson)

    fun createFileUploadBody(filePath: String, paramName: String = "file"): MultipartBody.Part {
        val file = File(filePath)
        val requestFile: RequestBody =
            RequestBody.create(MultipartBody.FORM, file)
        return MultipartBody.Part.createFormData(paramName, file.name, requestFile)
    }

    /**
     * 处理网络请求结果
     */
    fun <T> dealApiResult(response: ResponseWrapper<T>): T? {
        if (0 != response.code) {
            // 1 参数不正常
            // 2 未登录
            // 100000 未账号注册
            // 100002 登录失效
            // 100003 账号在其他地方登录

            when (response.code) {
                2, 100002, 100003 -> {
                    mHandler.post {
                        ActivityManagerUtils.clearLoginInfoGoLogin()
                    }
                }
            }
            throw CommonCustomException(response.code, response.message)
        }
        return response.data
    }

    suspend fun downloadFile(url: String, fileName: String, callback: OnDownloadProgressListener) {
        val file = FileUtils.saveDownLoadFile(downloadClient() { curSize, totalSize, isDone ->
            mHandler.post {
                callback.onProgress(curSize, totalSize)
            }
        }.downLoadFile(url).byteStream(), fileName)

        withContext(Dispatchers.Main) {
            file?.let {
                callback.onSuccess(it)
            } ?: callback.onFailure(
                CommonCustomException(
                    -1,
                    StringUtils.getString(R.string.err_save_file)
                )
            )
        }
    }
}

interface OnDownloadProgressListener {
    /**
     * 百分比，完成100
     */
    fun onProgress(curSize: Long, totalSize: Long)

    fun onSuccess(file: File)

    fun onFailure(e: Throwable)
}