package id.tkalfalah.rapor

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var database: RaporDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = RaporDatabase(applicationContext)
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true // Hanya untuk migrasi data versi lama.
            settings.allowFileAccess = true
            settings.allowContentAccess = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            addJavascriptInterface(NativeBridge(), "AndroidBridge")
        }
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }

    inner class NativeBridge {
        @JavascriptInterface
        fun loadData(): String = database.loadData()

        @JavascriptInterface
        fun saveData(json: String): Boolean = database.saveData(json)

        @JavascriptInterface
        fun printPage() {
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

    override fun onDestroy() {
        if (this::web.isInitialized) web.destroy()
        super.onDestroy()
    }
}
