package id.tkalfalah.rapor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var database: RaporDatabase
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var pendingBackup: String? = null

    companion object {
        private const val REQUEST_IMPORT_JSON = 4101
        private const val REQUEST_EXPORT_JSON = 4102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = RaporDatabase(applicationContext)
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true // Dipakai untuk migrasi satu kali dari versi lama.
            settings.allowFileAccess = true
            settings.allowContentAccess = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val uri: Uri = request?.url ?: return true
                    return uri.scheme != "file"
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onShowFileChooser(
                    view: WebView?,
                    callback: ValueCallback<Array<Uri>>?,
                    params: FileChooserParams?
                ): Boolean {
                    filePathCallback?.onReceiveValue(null)
                    filePathCallback = callback
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/json"
                    }
                    startActivityForResult(intent, REQUEST_IMPORT_JSON)
                    return true
                }
            }
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
        fun exportBackup(json: String) {
            pendingBackup = json
            runOnUiThread {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, "backup-rapor-tk-alfalah.json")
                }
                startActivityForResult(intent, REQUEST_EXPORT_JSON)
            }
        }

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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMPORT_JSON) {
            val selected = if (resultCode == RESULT_OK) data?.data?.let { arrayOf(it) } else null
            filePathCallback?.onReceiveValue(selected)
            filePathCallback = null
        } else if (requestCode == REQUEST_EXPORT_JSON) {
            val uri = if (resultCode == RESULT_OK) data?.data else null
            if (uri != null) {
                try {
                    contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use {
                        it.write(pendingBackup.orEmpty())
                    } ?: throw IllegalStateException("File tujuan tidak dapat dibuka")
                    Toast.makeText(this, "Cadangan berhasil disimpan", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal menyimpan cadangan: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            pendingBackup = null
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (this::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        if (this::web.isInitialized) {
            filePathCallback?.onReceiveValue(null)
            web.removeJavascriptInterface("AndroidBridge")
            web.destroy()
        }
        super.onDestroy()
    }
}
