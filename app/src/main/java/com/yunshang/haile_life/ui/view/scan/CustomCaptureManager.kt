package com.yunshang.haile_life.ui.view.scan

import android.app.Activity
import android.content.Intent
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/28 17:24
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class CustomCaptureManager(
    private val activity: Activity,
    barcodeView: DecoratedBarcodeView,
    private val resultCallBack: (String?) -> Unit
) :
    CaptureManager(activity, barcodeView) {

    override fun returnResult(rawResult: BarcodeResult?) {
//        super.returnResult(rawResult)
        resultCallBack(rawResult?.result?.text)
    }

    fun returnResultForAlbum(code: String?) {
        activity.setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(Intents.Scan.RESULT, code)
        })
        closeAndFinish()
    }
}