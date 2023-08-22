package com.yunshang.haile_life.ui.activity.common

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.View
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.lsy.framelib.ui.base.activity.BaseActivity
import com.yunshang.haile_life.databinding.ActivityCustomCaptureBinding
import com.yunshang.haile_life.ui.view.scan.CustomCaptureManager
import com.yunshang.haile_life.ui.view.scan.CustomViewFinderView
import com.yunshang.haile_life.utils.PictureSelectUtils


/**
 * 自定义扫码界面
 */
class CustomCaptureActivity : BaseActivity() {
    private val mBinding: ActivityCustomCaptureBinding by lazy {
        ActivityCustomCaptureBinding.inflate(layoutInflater)
    }

    private lateinit var capture: CustomCaptureManager

    override fun isFullScreen(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        mBinding.btnCaptureBack.setOnClickListener {
            finish()
        }
        mBinding.tvCaptureAlbum.setOnClickListener {
            // 从相册中选择图片
            PictureSelectUtils.pictureForAlbum(
                this@CustomCaptureActivity,
                1,
                needCompress = false,
                needCrop = false
            ) { isSuccess, result ->
                if (isSuccess) {
                    result?.firstOrNull()?.let {
                        try {
                            parseImgQrCode(
                                // ImageDecoder.decodeBitmap返回为空
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                                ImageDecoder.decodeBitmap(
//                                    ImageDecoder.createSource(
//                                        contentResolver,
//                                        Uri.parse(it.path)
//                                    )
//                                )
//                            } else {
                                MediaStore.Images.Media.getBitmap(
                                    contentResolver,
                                    Uri.parse(it.path)
                                )
//                            }
                                //
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        mBinding.cbCaptureLight.visibility = if (hasFlash()) View.VISIBLE else View.GONE
        mBinding.cbCaptureLight.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mBinding.zxingBarcodeScanner.setTorchOn()
            } else {
                mBinding.zxingBarcodeScanner.setTorchOff()
            }
        }

        capture = CustomCaptureManager(this, mBinding.zxingBarcodeScanner){}
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()
    }

    /**
     * 是否有闪光灯功能
     */
    private fun hasFlash(): Boolean {
        return applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    /**
     * 解析图片选择
     */
    private fun parseImgQrCode(bitmap: Bitmap) {
        capture.returnResultForAlbum(
            try {
                val width = bitmap.width
                val height = bitmap.height
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                val source = RGBLuminanceSource(width, height, pixels)
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

                QRCodeReader().decode(binaryBitmap).text
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        )
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        // 关闭动画
        if (mBinding.zxingBarcodeScanner.viewFinder is CustomViewFinderView) {
            (mBinding.zxingBarcodeScanner.viewFinder as CustomViewFinderView).stopScanAnimAndReset()
        }

        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return mBinding.zxingBarcodeScanner.onKeyDown(keyCode, event) || super.onKeyDown(
            keyCode,
            event
        )
    }
}