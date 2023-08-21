package com.yunshang.haile_life.utils.qrcode

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import com.king.camera.scan.AnalyzeResult
import com.king.camera.scan.CameraScan
import com.king.camera.scan.util.LogUtils
import com.king.camera.scan.util.PointUtils
import com.king.wechat.qrcode.WeChatQRCodeDetector
import com.king.wechat.qrcode.scanning.WeChatCameraScanActivity
import com.king.wechat.qrcode.scanning.analyze.WeChatScanningAnalyzer
import com.yunshang.haile_life.R
import com.yunshang.haile_life.utils.PictureSelectUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Title :
 * Author: Lsy
 * Date: 2023/8/21 17:33
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class WeChatQRCodeScanActivity : WeChatCameraScanActivity() {
    private lateinit var mIvResult: AppCompatImageView

    override fun getLayoutId(): Int = R.layout.activity_wechat_qrcode_scan

    override fun getPreviewViewId(): Int = R.id.pv_qrcode_scan_pre

    override fun getViewfinderViewId(): Int = R.id.vfv_qrcode_scan

//    override fun getFlashlightId(): Int = R.id.cb_capture_light

    override fun initUI() {
        super.initUI()

        mIvResult = findViewById<AppCompatImageView>(R.id.iv_qrcode_scan_result)
        findViewById<AppCompatImageButton>(R.id.btn_capture_back).setOnClickListener {
            finish()
        }
        findViewById<AppCompatTextView>(R.id.tv_capture_album).setOnClickListener {
            // 从相册中选择图片
            PictureSelectUtils.pictureForAlbum(
                this@WeChatQRCodeScanActivity,
                1,
                needCompress = false,
                needCrop = false
            ) { isSuccess, result ->
                if (isSuccess) {
                    result?.firstOrNull()?.let {
                        try {
                            processPickPhotoResult(
                                MediaStore.Images.Media.getBitmap(
                                    contentResolver,
                                    Uri.parse(it.path)
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        val cbCaptureLight = findViewById<AppCompatCheckBox>(R.id.cb_capture_light)
        cbCaptureLight.visibility = if (hasFlash()) View.VISIBLE else View.GONE
        cbCaptureLight.isChecked = cameraScan?.isTorchEnabled == true
        cbCaptureLight.setOnCheckedChangeListener { _, _ ->
            toggleTorchState()
        }
    }

    /**
     * 是否有闪光灯功能
     */
    private fun hasFlash(): Boolean {
        return applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    override fun onScanResultCallback(result: AnalyzeResult<MutableList<String>>) {
        // 停止分析
        cameraScan.setAnalyzeImage(false)
        Timber.d(result.result.toString())
        val frameMetadata = result.frameMetadata
        var width = frameMetadata.width
        var height = frameMetadata.height
        if (frameMetadata.rotation == 90 || frameMetadata.rotation == 270) {
            width = frameMetadata.height
            height = frameMetadata.width
        }
        // 当初始化 WeChatScanningAnalyzer 时，如果是需要二维码的位置信息，则会返回 WeChatScanningAnalyzer.QRCodeAnalyzeResult
        if (result is WeChatScanningAnalyzer.QRCodeAnalyzeResult) { // 如果需要处理结果二维码的位置信息
            //取预览当前帧图片并显示，为结果点提供参照
            mIvResult.setImageBitmap(previewView.bitmap)
            val points = ArrayList<Point>()
            result.points?.forEach { mat ->
                // 扫码结果二维码的四个点（一个矩形）
                Timber.d("point0: ${mat[0, 0][0]}, ${mat[0, 1][0]}")
                Timber.d("point1: ${mat[1, 0][0]}, ${mat[1, 1][0]}")
                Timber.d("point2: ${mat[2, 0][0]}, ${mat[2, 1][0]}")
                Timber.d("point3: ${mat[3, 0][0]}, ${mat[3, 1][0]}")

                val point0 = Point(mat[0, 0][0].toInt(), mat[0, 1][0].toInt())
                val point1 = Point(mat[1, 0][0].toInt(), mat[1, 1][0].toInt())
                val point2 = Point(mat[2, 0][0].toInt(), mat[2, 1][0].toInt())
                val point3 = Point(mat[3, 0][0].toInt(), mat[3, 1][0].toInt())

                val centerX = (point0.x + point1.x + point2.x + point3.x) / 4
                val centerY = (point0.y + point1.y + point2.y + point3.y) / 4

                //将实际的结果中心点坐标转换成界面预览的坐标
                val point = PointUtils.transform(
                    centerX,
                    centerY,
                    width,
                    height,
                    viewfinderView.width,
                    viewfinderView.height
                )
                points.add(point)
            }
            //设置Item点击监听
            viewfinderView.setOnItemClickListener {
                //显示点击Item将所在位置扫码识别的结果返回
                resultIntent(result.result[it])
            }
            //显示结果点信息
            viewfinderView.showResultPoints(points)

            if (result.result.size == 1) {
                resultIntent(result.result[0])
            }
        } else {
            // 一般需求都是识别一个码，所以这里取第0个就可以；有识别多个码的需求，可以取全部
            resultIntent(result.result[0])
        }
    }

    private fun resultIntent(result: String) {
        val intent = Intent()
        intent.putExtra(CameraScan.SCAN_RESULT, result)
        setResult(RESULT_OK, intent)
        finish()
    }

    /**
     * 处理选择图片后，从图片中检测二维码结果
     */
    private fun processPickPhotoResult(bitmap: Bitmap) {
        try {
            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    // 通过WeChatQRCodeDetector识别图片中的二维码
                    WeChatQRCodeDetector.detectAndDecode(bitmap)
                }
                if (result.isNotEmpty()) {// 不为空，则表示识别成功
                    // 打印所有结果
                    for ((index, text) in result.withIndex()) {
                        LogUtils.d("result$index:$text")
                    }
                    // 一般需求都是识别一个码，所以这里取第0个就可以；有识别多个码的需求，可以取全部
                    resultIntent(result[0])
                } else {
                    // 为空表示识别失败
                    LogUtils.d("result = null")
                }
            }
        } catch (e: Exception) {
            LogUtils.w(e)
        }
    }
}