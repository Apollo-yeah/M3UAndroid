package com.m3u.features.setting

import android.net.Uri

data class SettingState(
    val versionName: String = "",
    val versionCode: Int = -1,
    val snapshot: Boolean = false,
    val title: String = "",
    val url: String = "",
    val uri: Uri = Uri.EMPTY,
    val localStorage: Boolean = false
) {
    val actualUrl
        get() = if (localStorage) {
            uri.takeIf { uri != Uri.EMPTY }?.toString()
        } else {
            url.ifEmpty { null }
        }
}