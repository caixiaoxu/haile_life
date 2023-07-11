package com.yunshang.haile_life.ui.activity.common

import android.os.Bundle
import android.view.KeyEvent
import com.google.zxing.client.android.R
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.lsy.framelib.ui.base.activity.BaseActivity

/**
 *
 */
class CustomCaptureActivity : BaseActivity() {
    private lateinit var barcodeScannerView: DecoratedBarcodeView
    private lateinit var capture: CaptureManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        barcodeScannerView = initializeContent()
        capture = CaptureManager(this, barcodeScannerView)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()
    }

    /**
     * Override to use a different layout.
     *
     * @return the DecoratedBarcodeView
     */
    private fun initializeContent(): DecoratedBarcodeView {
        setContentView(R.layout.zxing_capture)
        return findViewById(R.id.zxing_barcode_scanner)
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
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}