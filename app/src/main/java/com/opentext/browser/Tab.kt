package com.opentext.browser

import java.io.Serializable
import java.util.UUID

/**
 * Represents a browser tab with its state
 */
data class Tab(
    val id: String = UUID.randomUUID().toString(),
    var url: String = "https://www.google.com",
    var title: String = "New Tab",
    var favicon: String? = null,
    var isLoading: Boolean = false,
    var scrollPosition: Int = 0
) : Serializable
