package com.yunshang.haile_life.web

import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.view.View
import android.webkit.*
import androidx.activity.result.contract.ActivityResultContracts
import com.github.lzyzsd.jsbridge.BridgeWebView
import com.google.gson.Gson
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.SystemPermissionHelper
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.WebViewViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.databinding.ActivityWebviewBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity


class WebViewActivity : BaseBusinessActivity<ActivityWebviewBinding, WebViewViewModel>(
    WebViewViewModel::class.java
) {
    private var mWebView: BridgeWebView? = null
    private var fileChooserParams: WebChromeClient.FileChooserParams? = null
    private var fileCallback: ValueCallback<Array<Uri>>? = null

    // 权限
    private val requestMultiplePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
            if (result.values.any { it }) {
                // 授权权限成功
                fileChooserParams?.createIntent()?.let { intent ->
                    startFileChooser.launch(intent)
                }
                fileChooserParams = null
            } else {
                // 授权失败
                SToast.showToast(this, R.string.empty_permission)
            }
        }

    // 文件选择界面
    private val startFileChooser =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            fileCallback?.let { callback ->
                callback.onReceiveValue(if (result.resultCode == RESULT_OK && null != result.data) {
                    result.data?.dataString?.let { arrayOf(Uri.parse(it)) }
                } else null)
                fileCallback = null
            }
        }

    override fun layoutId(): Int = R.layout.activity_webview

    override fun backBtn(): View = mBinding.barWebviewTitle.getBackBtn()

    override fun initView() {
        window.statusBarColor = Color.WHITE

        IntentParams.WebViewParams.parseTitle(intent)?.let {
            mBinding.barWebviewTitle.setTitle(it)
        }

        initWebView()
    }

    private fun initWebView() {
        mWebView = BridgeWebView(applicationContext).apply {
            settings.run {
                defaultTextEncodingName = "utf-8"
                builtInZoomControls = false
//            layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
                domStorageEnabled = true
                allowFileAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                if (IntentParams.WebViewParams.parseNoCache(intent)) {
                    cacheMode = WebSettings.LOAD_NO_CACHE
                }
            }

            webViewClient = object : WebViewClient() {
                //防止加载网页时调起系统浏览器
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    view?.loadUrl(request?.url.toString())
                    return true
                }

                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler?,
                    error: SslError?
                ) {
                    handler?.proceed()
//                super.onReceivedSslError(view, handler, error)
                }
            }

            webChromeClient = object : WebChromeClient() {

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)

                    title?.let {
                        if (IntentParams.WebViewParams.parseAutoWebTitle(intent)) {
                            mBinding.barWebviewTitle.setTitle(it)
                        }
                    }
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    if (newProgress == 100) {
                        hideLoading()
                    } else {
                        showLoading()
                    }
                    super.onProgressChanged(view, newProgress)
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    this@WebViewActivity.fileChooserParams = fileChooserParams
                    fileCallback = filePathCallback
                    requestMultiplePermission.launch(SystemPermissionHelper.readWritePermissions())
                    return true
                }
            }
            setGson(Gson())
        }
        mBinding.flWebview.addView(mWebView)
    }

    override fun initData() {
        loadUrl()
    }

    private fun loadUrl() {
        IntentParams.WebViewParams.parseUrl(intent)?.let { url ->
            if (url.lastIndexOf(".jpg") != -1 || url.lastIndexOf(".png") != -1) {// 加载图片
                val sbimgs = StringBuilder("<html><center>")
                sbimgs.append("<img src=$url width=\"100%\">")
                sbimgs.append("</br>")
                sbimgs.append("</center></html>")
                mWebView?.loadData(sbimgs.toString(), "text/html", "UTF-8")
            } else {
                mWebView?.loadUrl(url)
            }
        }
    }

    override fun onBackListener() {
        if (mWebView?.canGoBack() == true) {
            mWebView?.goBack()
        } else {
            // 清空缓存
            mWebView?.clearCache(true)
            mWebView?.clearFormData()
            mWebView?.clearHistory()
            // 销毁控件
            mBinding.flWebview.removeView(mWebView)
            mWebView?.destroy()
            mWebView = null
            super.onBackListener()
        }
    }
}