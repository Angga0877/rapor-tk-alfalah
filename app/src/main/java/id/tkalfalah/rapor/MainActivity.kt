package id.tkalfalah.rapor

import android.app.Activity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import android.print.PrintAttributes
import android.print.PrintManager
import android.content.Context

class MainActivity : Activity() {
    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = true
        web.settings.allowContentAccess = false
        web.settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        web.webViewClient = WebViewClient()
        web.webChromeClient = WebChromeClient()
        web.addJavascriptInterface(Bridge(), "AndroidBridge")
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }

    inner class Bridge {
        @JavascriptInterface fun printPage() {
            runOnUiThread {
                val manager = getSystemService(Context.PRINT_SERVICE) as PrintManager
                val adapter = web.createPrintDocumentAdapter("Rapor-TK-Alfalah")
                manager.print("Rapor TK Alfalah", adapter, PrintAttributes.Builder().build())
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (this::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
