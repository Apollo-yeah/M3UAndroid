package com.m3u.androidApp.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.m3u.androidApp.components.AppNavigation
import com.m3u.androidApp.components.AppSnackHost
import com.m3u.core.util.collections.withEach
import com.m3u.i18n.R.string
import com.m3u.material.components.IconButton
import com.m3u.material.components.NavigationScaffold
import com.m3u.material.components.ToolkitScaffold
import com.m3u.material.model.LocalSpacing
import com.m3u.ui.ActionsFactory
import com.m3u.ui.Destination
import com.m3u.ui.Fob
import com.m3u.ui.Helper
import com.m3u.ui.M3ULocalProvider
import com.m3u.ui.Navigate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun AppScaffold(
    title: String,
    snacker: String,
    actionsFactory: ActionsFactory,
    rootDestination: Destination.Root?,
    fob: Fob?,
    isSystemBarVisible: Boolean,
    isSystemBarScrollable: Boolean,
    helper: Helper,
    cinemaMode: Boolean,
    isPlaying: Boolean,
    navigate: Navigate,
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme = if (!isSystemInDarkTheme()) dynamicLightColorScheme(LocalContext.current)
    else dynamicDarkColorScheme(LocalContext.current),
    onBackPressed: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val spacing = LocalSpacing.current

    val windowSizeClass = helper.windowSizeClass
    val useNavRail = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact

    M3ULocalProvider(colorScheme, helper) {
        val scope = rememberCoroutineScope()
        val darkMode = when {
            cinemaMode -> true
            isPlaying -> true
            else -> isSystemInDarkTheme()
        }
        DisposableEffect(
            darkMode,
            scope,
            isPlaying,
            cinemaMode
        ) {
            scope.launch {
                if (!cinemaMode && isPlaying) {
                    delay(800.milliseconds)
                }
                helper.darkMode = darkMode
            }
            onDispose {}
        }
        NavigationScaffold(
            useNavRail = useNavRail,
            visible = isSystemBarVisible,
            navigation = {
                AppNavigation(
                    navigate = navigate,
                    rootDestination = rootDestination,
                    fob = fob,
                    useNavRail = useNavRail
                )
            },
            content = {
                ToolkitScaffold(
                    title = title,
                    visible = isSystemBarVisible,
                    scrollable = isSystemBarScrollable,
                    actions = {
                        val actions = actionsFactory()
                        actions.withEach {
                            IconButton(
                                icon = icon,
                                contentDescription = contentDescription,
                                onClick = onClick
                            )
                        }
                    },
                    onBackPressed = onBackPressed,
                    onBackPressedContentDescription = stringResource(string.ui_cd_top_bar_on_back_pressed),
                    modifier = modifier
                ) { padding ->
                    Box {
                        val navRailModifier = if (useNavRail) Modifier.navigationBarsPadding()
                        else Modifier
                        content(padding)
                        AppSnackHost(
                            message = snacker,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.small)
                                .align(Alignment.BottomCenter)
                                .then(navRailModifier)
                        )
                    }
                }
            }
        )
    }
}