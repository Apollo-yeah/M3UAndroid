package com.m3u.material.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun NavigationScaffold(
    useNavRail: Boolean,
    visible: Boolean,
    navigation: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    when {
        useNavRail -> {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = visible,
                    transitionSpec = {
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { -it }
                    },
                    label = "AppBottomSheet",
                    modifier = Modifier.fillMaxHeight(),
                ) { visible ->
                    if (visible) {
                        navigation()
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    content()
                }
            }
        }

        else -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    content()
                }
                AnimatedContent(
                    targetState = visible,
                    transitionSpec = {
                        slideInVertically { it } togetherWith slideOutVertically { it }
                    },
                    label = "AppBottomSheet",
                    modifier = Modifier.fillMaxWidth(),
                ) { visible ->
                    if (visible) {
                        navigation()
                    }
                }
            }
        }
    }
}