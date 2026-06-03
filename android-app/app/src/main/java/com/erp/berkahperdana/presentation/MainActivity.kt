package com.erp.berkahperdana.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private val ENTERPRISE_URL = "https://enterprise.pages.dev"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webView)
        setupWebView()
        checkPermissions()
        webView.loadUrl(ENTERPRISE_URL)
    }

    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        settings.useWideViewPort = true
        val userAgent = "BerkahPerdanaApp/1.0 (Android ${Build.VERSION.RELEASE})"
        settings.userAgentString = userAgent
        CookieManager.getInstance().setAcceptCookie(true)
        webView.webViewClient = object : WebViewClient() {}
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val missing = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage("App needs file access and internet permissions")
                .setPositiveButton("Allow") { _, _ ->
                    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
                        .launch(missing.toTypedArray())
                }
                .setNegativeButton("Deny") { _, _ ->
                    finishAffinity()
                }
                .setCancelable(false)
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_close -> { closeApp(); true }
            R.id.menu_restart -> { restartApp(); true }
            R.id.menu_update -> { openUpdate(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun closeApp() {
        AlertDialog.Builder(this).setTitle("Close App").setMessage("Force close?")
            .setPositiveButton("Close") { _, _ -> Process.killProcess(Process.myPid()) }
            .setNegativeButton("Cancel", null).show()
    }

    private fun restartApp() {
        AlertDialog.Builder(this).setTitle("Restart").setMessage("Restart app?")
            .setPositiveButton("Restart") { _, _ ->
                webView.clearCache(true)
                val intent = Intent(this, SplashActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                Process.killProcess(Process.myPid())
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun openUpdate() {
        AlertDialog.Builder(this).setTitle("Update").setMessage("Open update URL?")
            .setPositiveButton("Open") { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$ENTERPRISE_URL/download")))
            }
            .setNegativeButton("Cancel", null).show()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
