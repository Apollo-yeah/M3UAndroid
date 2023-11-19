package com.m3u.features.console

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

sealed class MonoStyle(
    val prefix: String
) {
    data object Input : MonoStyle(">-")
    data object Error : MonoStyle("!-")
    data object HighLight : MonoStyle("#-")
    data object Common : MonoStyle("")

    val color: Color
        @Composable get() = when (this) {
            Input -> Color.Yellow
            Error -> MaterialTheme.colorScheme.error
            HighLight -> MaterialTheme.colorScheme.primary
            Common -> LocalContentColor.current
        }

    fun actual(text: String): String = when (this) {
        Common -> text
        Error -> text.drop(2)
        HighLight -> text.drop(2)
        Input -> text.drop(2)
    }

    companion object {
        fun get(text: String): MonoStyle {
            if (text.startsWith(Input.prefix)) return Input
            if (text.startsWith(Error.prefix)) return Error
            if (text.startsWith(HighLight.prefix)) return HighLight
            return Common
        }
    }
}

