package com.opentext.browser

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manages browser tabs and persistence
 */
class TabManager private constructor(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val tabs = mutableListOf<Tab>()
    private var activeTabId: String? = null

    companion object {
        private const val PREFS_NAME = "OpenTextBrowserTabs"
        private const val KEY_TABS = "tabs"
        private const val KEY_ACTIVE_TAB = "active_tab_id"

        @Volatile
        private var instance: TabManager? = null

        fun getInstance(context: Context): TabManager {
            return instance ?: synchronized(this) {
                instance ?: TabManager(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        loadTabs()
    }

    /**
     * Get all tabs
     */
    fun getTabs(): List<Tab> = tabs.toList()

    /**
     * Get the number of tabs
     */
    fun getTabCount(): Int = tabs.size

    /**
     * Get the currently active tab
     */
    fun getActiveTab(): Tab? = tabs.find { it.id == activeTabId }

    /**
     * Get the active tab index
     */
    fun getActiveTabIndex(): Int = tabs.indexOfFirst { it.id == activeTabId }

    /**
     * Add a new tab
     */
    fun addTab(tab: Tab = Tab()): Tab {
        tabs.add(tab)
        activeTabId = tab.id
        saveTabs()
        return tab
    }

    /**
     * Remove a tab by ID
     */
    fun removeTab(tabId: String): Boolean {
        val removed = tabs.removeAll { it.id == tabId }
        if (removed) {
            // If we removed the active tab, switch to another one
            if (activeTabId == tabId) {
                if (tabs.isNotEmpty()) {
                    // Switch to the last tab or the first one
                    val index = tabs.size - 1
                    activeTabId = tabs[maxOf(0, index)].id
                } else {
                    // Create a new tab if all are closed
                    val newTab = Tab()
                    tabs.add(newTab)
                    activeTabId = newTab.id
                }
            }
            saveTabs()
        }
        return removed
    }

    /**
     * Set a tab as active
     */
    fun setActiveTab(tabId: String): Boolean {
        if (tabs.any { it.id == tabId }) {
            activeTabId = tabId
            saveTabs()
            return true
        }
        return false
    }

    /**
     * Update tab information
     */
    fun updateTab(tabId: String, url: String? = null, title: String? = null, isLoading: Boolean? = null) {
        tabs.find { it.id == tabId }?.let { tab ->
            url?.let { tab.url = it }
            title?.let { tab.title = it }
            isLoading?.let { tab.isLoading = it }
            saveTabs()
        }
    }

    /**
     * Get tab by ID
     */
    fun getTab(tabId: String): Tab? = tabs.find { it.id == tabId }

    /**
     * Get tab by index
     */
    fun getTabAt(index: Int): Tab? = tabs.getOrNull(index)

    /**
     * Close all tabs except the specified one
     */
    fun closeOtherTabs(keepTabId: String) {
        tabs.removeAll { it.id != keepTabId }
        activeTabId = keepTabId
        saveTabs()
    }

    /**
     * Close all tabs
     */
    fun closeAllTabs() {
        tabs.clear()
        val newTab = Tab()
        tabs.add(newTab)
        activeTabId = newTab.id
        saveTabs()
    }

    /**
     * Save tabs to SharedPreferences
     */
    private fun saveTabs() {
        val tabsJson = gson.toJson(tabs)
        sharedPreferences.edit()
            .putString(KEY_TABS, tabsJson)
            .putString(KEY_ACTIVE_TAB, activeTabId)
            .apply()
    }

    /**
     * Load tabs from SharedPreferences
     */
    private fun loadTabs() {
        val tabsJson = sharedPreferences.getString(KEY_TABS, null)
        val savedActiveTabId = sharedPreferences.getString(KEY_ACTIVE_TAB, null)

        if (tabsJson != null) {
            try {
                val type = object : TypeToken<List<Tab>>() {}.type
                val savedTabs: List<Tab> = gson.fromJson(tabsJson, type)
                tabs.clear()
                tabs.addAll(savedTabs)

                // Restore active tab
                if (savedActiveTabId != null && tabs.any { it.id == savedActiveTabId }) {
                    activeTabId = savedActiveTabId
                } else if (tabs.isNotEmpty()) {
                    activeTabId = tabs.first().id
                }
            } catch (e: Exception) {
                // If parsing fails, create a new tab
                tabs.clear()
                val newTab = Tab()
                tabs.add(newTab)
                activeTabId = newTab.id
            }
        } else {
            // No saved tabs, create a new one
            val newTab = Tab()
            tabs.add(newTab)
            activeTabId = newTab.id
        }
    }

    /**
     * Force save tabs (call this when app goes to background)
     */
    fun forceSave() {
        saveTabs()
    }
}
