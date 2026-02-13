package com.opentext.browser

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Activity for switching between tabs
 */
class TabSwitcherActivity : AppCompatActivity() {

    private lateinit var tabManager: TabManager
    private lateinit var tabAdapter: TabAdapter
    private lateinit var tvTabCount: TextView

    companion object {
        const val EXTRA_SELECTED_TAB_ID = "selected_tab_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab_switcher)

        tabManager = TabManager.getInstance(this)

        setupViews()
        setupRecyclerView()
        updateTabCount()
    }

    private fun setupViews() {
        tvTabCount = findViewById(R.id.tvTabCount)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val fabNewTab = findViewById<FloatingActionButton>(R.id.fabNewTab)
        fabNewTab.setOnClickListener {
            // Add new tab and return to main activity
            val newTab = tabManager.addTab()
            returnToMainActivity(newTab.id)
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewTabs)

        tabAdapter = TabAdapter(
            onTabClick = { tab ->
                // Switch to this tab
                tabManager.setActiveTab(tab.id)
                returnToMainActivity(tab.id)
            },
            onTabClose = { tab ->
                // Close this tab
                tabManager.removeTab(tab.id)
                tabAdapter.setTabs(tabManager.getTabs())
                updateTabCount()

                // If no tabs left, create a new one
                if (tabManager.getTabCount() == 0) {
                    tabManager.addTab()
                }
            }
        )

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = tabAdapter
        tabAdapter.setTabs(tabManager.getTabs())
    }

    private fun updateTabCount() {
        val count = tabManager.getTabCount()
        tvTabCount.text = "$count ${if (count == 1) "tab" else "tabs"}"
    }

    private fun returnToMainActivity(tabId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_SELECTED_TAB_ID, tabId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        finish()
    }
}
