package com.opentext.browser

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.net.toUri

/**
 * OpenTextBrowser - Android Browser with Text Copying Enabled
 * 
 * This browser is designed to allow users to copy text from any website,
 * overriding website restrictions that prevent text selection and copying.
 */
class MainActivity : AppCompatActivity() {

    // UI Components
    private lateinit var webView: WebView
    private lateinit var urlEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageButton
    private lateinit var forwardButton: ImageButton
    private lateinit var refreshButton: ImageButton
    private lateinit var copyButton: ImageButton
    private lateinit var toolbar: Toolbar

    // Clipboard manager for copying text
    private lateinit var clipboardManager: ClipboardManager

    // SharedPreferences for storing the last URL
    private val sharedPreferences by lazy {
        getSharedPreferences("OpenTextBrowserPrefs", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_LAST_URL = "last_url"
        private const val DEFAULT_URL = "https://www.google.com"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize clipboard manager
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Initialize UI components
        initializeViews()

        // Configure WebView settings
        configureWebView()

        // Restore WebView state if available
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        }

        // Load the last visited URL or default URL
        val lastUrl = getLastUrl()
        loadUrl(lastUrl)

        // Handle back button press using the modern OnBackPressedDispatcher API
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    // Disable this callback and call it again to perform default action (like exit)
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    // Re-enable it for next time if activity stays alive
                    isEnabled = true
                }
            }
        })
    }

    /**
     * Initialize all UI components
     */
    private fun initializeViews() {
        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize WebView
        webView = findViewById(R.id.webView)

        // Add layout change listener to WebView to handle keyboard appearance
        webView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            webView.invalidate()
        }

        // Initialize URL input field
        urlEditText = findViewById(R.id.urlEditText)

        // Initialize progress bar
        progressBar = findViewById(R.id.progressBar)

        // Initialize navigation buttons
        backButton = findViewById(R.id.btnBack)
        forwardButton = findViewById(R.id.btnForward)
        refreshButton = findViewById(R.id.btnRefresh)
        copyButton = findViewById(R.id.btnCopy)

        // Set up button click listeners
        setupButtonListeners()

        // Set up URL input handling
        setupUrlInputHandling()

        // Set up keyboard visibility detection and WebView redraw
        setupKeyboardDetection()
    }

    /**
     * Set up keyboard visibility detection to redraw WebView when keyboard appears
     */
    private fun setupKeyboardDetection() {
        val rootView = findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.height
            val keypadHeight = screenHeight - rect.bottom

            // If keyboard is visible (keypadHeight > screenHeight * 0.15), refresh WebView
            if (keypadHeight > screenHeight * 0.15) {
                // Keyboard is visible - force WebView to redraw and request layout
                webView.post {
                    webView.invalidate()
                    webView.requestLayout()
                    // Also scroll to show content properly
                    webView.scrollTo(0, 0)
                }
            } else {
                // Keyboard is hidden - force WebView to redraw and request layout
                webView.post {
                    webView.invalidate()
                    webView.requestLayout()
                }
            }
        }
    }

    /**
     * Set up click listeners for navigation buttons
     */
    private fun setupButtonListeners() {
        // Back button - navigate to previous page
        backButton.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }

        // Forward button - navigate to next page
        forwardButton.setOnClickListener {
            if (webView.canGoForward()) {
                webView.goForward()
            }
        }

        // Refresh button - reload current page
        refreshButton.setOnClickListener {
            webView.reload()
        }

        // Copy button - copy selected text to clipboard
        copyButton.setOnClickListener {
            copySelectedText()
        }
    }

    /**
     * Set up URL input field handling
     */
    private fun setupUrlInputHandling() {
        // Handle focus changes to redraw WebView when keyboard appears
        urlEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Redraw WebView when keyboard appears (input focus)
                webView.invalidate()
            } else {
                // Redraw WebView when keyboard hides
                webView.invalidate()
            }
        }

        // Handle keyboard actions on URL input
        urlEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val url = urlEditText.text.toString().trim()
                if (url.isNotEmpty()) {
                    loadUrl(url)
                    hideKeyboard()
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }

    /**
     * Configure WebView settings for optimal browsing
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        webView.apply {
            // Set white background to prevent lavender/blank display
            setBackgroundColor(android.graphics.Color.WHITE)

            // Enable JavaScript for dynamic content
            settings.javaScriptEnabled = true

            // Enable DOM storage for web apps
            settings.domStorageEnabled = true

            // Enable built-in zoom controls
            settings.builtInZoomControls = true
            settings.displayZoomControls = false

            // Enable text zoom
            settings.textZoom = 100

            // Enable file access
            settings.allowFileAccess = true

            // Enable content access
            settings.allowContentAccess = true

            // Enable database storage
            settings.databaseEnabled = true

            // Enable mixed content (for compatibility)
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

            // Set load overview mode for better reading
            settings.loadWithOverviewMode = true

            // Use wide viewport
            settings.useWideViewPort = true

            // Set custom user agent
            settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; SM-G975F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36"

            // Set up WebViewClient for page loading
            webViewClient = BrowserWebViewClient()

            // Set up WebChromeClient for progress updates
            webChromeClient = BrowserChromeClient()

            // Enable horizontal and vertical scrollbar overlays
            scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        }
    }

    /**
     * Load a URL with automatic https:// prefix if needed
     */
    private fun loadUrl(url: String) {
        val processedUrl = when {
            url.startsWith("http://") -> url
            url.startsWith("https://") -> url
            url.contains(".") -> "https://$url"
            else -> "https://www.google.com/search?q=$url"
        }
        
        webView.loadUrl(processedUrl)
        urlEditText.setText(processedUrl)
    }

    /**
     * Custom WebViewClient for handling page loads
     */
    private inner class BrowserWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            // Show progress bar when loading starts
            progressBar.visibility = View.VISIBLE
            updateNavigationButtons()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            // Hide progress bar when loading finishes
            progressBar.visibility = View.GONE
            
            // Update URL field
            url?.let {
                if (!urlEditText.isFocused) {
                    urlEditText.setText(it)
                }
                // Save the URL to SharedPreferences for automatic recall
                saveLastUrl(it)
            }

            // Inject JavaScript to enable text selection and copying
            injectTextSelectionEnabler()

            updateNavigationButtons()
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            url?.let {
                // Handle different protocols
                return when {
                    it.startsWith("http://") || it.startsWith("https://") -> {
                        false // Let WebView handle it
                    }
                    it.startsWith("intent://") -> {
                        // Handle intent URLs
                        try {
                            val intent = Intent.parseUri(it, Intent.URI_INTENT_SCHEME)
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "Cannot open this link", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    else -> {
                        // Try to open with external app
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, it.toUri())
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "Cannot open this link", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                }
            }
            return false
        }
    }

    /**
     * Custom WebChromeClient for handling progress updates
     */
    private inner class BrowserChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            // Update progress bar
            progressBar.progress = newProgress
            
            // Hide progress bar when fully loaded
            if (newProgress == 100) {
                progressBar.visibility = View.GONE
            }
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            // Update toolbar title if needed
            title?.let {
                supportActionBar?.title = it
            }
        }
    }

    /**
     * Inject JavaScript to enable text selection and copying on all websites
     * This overrides website restrictions that prevent text selection
     */
    private fun injectTextSelectionEnabler() {
        val javascript = """
            (function() {
                // Create CSS to force text selection
                var style = document.createElement('style');
                style.id = 'opentext-browser-style';
                style.innerHTML = `
                    html, body, div, span, p, a, li, td, tr, table, 
                    h1, h2, h3, h4, h5, h6, pre, code, blockquote,
                    article, section, main, nav, header, footer, aside,
                    input, textarea, label, select, option, button {
                        -webkit-user-select: text !important;
                        -moz-user-select: text !important;
                        -ms-user-select: text !important;
                        user-select: text !important;
                        -webkit-touch-callout: default !important;
                        -moz-touch-callout: default !important;
                        -ms-touch-callout: default !important;
                        touch-callout: default !important;
                    }
                    * {
                        -webkit-user-select: text !important;
                        -moz-user-select: text !important;
                        -ms-user-select: text !important;
                        user-select: text !important;
                        pointer-events: auto !important;
                    }
                `;
                
                // Remove existing style if present
                var existingStyle = document.getElementById('opentext-browser-style');
                if (existingStyle) {
                    existingStyle.remove();
                }
                
                // Add new style
                document.head.appendChild(style);

                // Remove event listeners that block text selection
                var events = ['contextmenu', 'selectstart', 'copy', 'cut', 'paste', 
                             'mousedown', 'mouseup', 'keydown', 'keyup', 'drag', 'dragstart',
                             'touchstart', 'touchend', 'touchmove'];
                
                events.forEach(function(event) {
                    document.addEventListener(event, function(e) {
                        // Allow the event to propagate
                    }, true);
                });

                // Override oncontextmenu to allow right-click
                document.oncontextmenu = null;
                document.body.oncontextmenu = null;
                
                // Remove inline oncontextmenu handlers
                var elements = document.querySelectorAll('*');
                elements.forEach(function(el) {
                    el.oncontextmenu = null;
                });

                // Make sure body allows selection
                if (document.body) {
                    document.body.style.webkitUserSelect = 'text';
                    document.body.style.userSelect = 'text';
                }

                console.log('OpenText Browser: Text selection enabled');
            })();
        """.trimIndent()

        webView.evaluateJavascript(javascript, null)
    }

    /**
     * Copy selected text to clipboard
     */
    private fun copySelectedText() {
        // First, try to get selected text from JavaScript
        val javascript = """
            (function() {
                var selection = window.getSelection();
                if (selection && selection.toString().length > 0) {
                    return selection.toString();
                }
                return '';
            })();
        """.trimIndent()

        webView.evaluateJavascript(javascript) { selectedText ->
            if (!selectedText.isNullOrEmpty() && selectedText != "null") {
                // Clean up the result (remove quotes)
                val cleanText = selectedText.trim('"')
                if (cleanText.isNotEmpty()) {
                    copyToClipboard(cleanText)
                    Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No text selected. Long-press to select text.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "No text selected. Long-press to select text.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Copy text to system clipboard
     */
    private fun copyToClipboard(text: String) {
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboardManager.setPrimaryClip(clip)
    }

    /**
     * Update navigation button states
     */
    private fun updateNavigationButtons() {
        // Enable/disable back button
        backButton.isEnabled = webView.canGoBack()
        backButton.alpha = if (webView.canGoBack()) 1.0f else 0.5f

        // Enable/disable forward button
        forwardButton.isEnabled = webView.canGoForward()
        forwardButton.alpha = if (webView.canGoForward()) 1.0f else 0.5f
    }

    /**
     * Hide the keyboard
     */
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(urlEditText.windowToken, 0)
    }

    /**
     * Save the last visited URL to SharedPreferences
     */
    private fun saveLastUrl(url: String) {
        sharedPreferences.edit {
            putString(KEY_LAST_URL, url)
        }
    }

    /**
     * Get the last visited URL from SharedPreferences
     * Returns default URL if no previous URL was saved
     */
    private fun getLastUrl(): String {
        return sharedPreferences.getString(KEY_LAST_URL, DEFAULT_URL) ?: DEFAULT_URL
    }

    /**
     * Save WebView state when activity is destroyed
     */
    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    /**
     * Restore WebView state when activity is recreated
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }

    /**
     * Handle pause to pause WebView timers
     */
    override fun onPause() {
        super.onPause()
        webView.onPause()
        // Save the current URL when app goes to background
        val currentUrl = webView.url
        if (!currentUrl.isNullOrEmpty()) {
            saveLastUrl(currentUrl)
        }
    }

    /**
     * Handle resume to resume WebView timers
     */
    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    /**
     * Clean up WebView when activity is destroyed
     */
    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
