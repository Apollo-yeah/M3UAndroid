package com.m3u.features.live.fragments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Brightness2
import androidx.compose.material.icons.rounded.Brightness7
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.PictureInPicture
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.m3u.core.annotation.ClipMode
import com.m3u.core.util.basic.isNotEmpty
import com.m3u.features.live.LiveState
import com.m3u.features.live.components.CoverPlaceholder
import com.m3u.features.live.components.LiveMask
import com.m3u.features.live.fragments.LiveFragmentDefaults.detectVerticalMaskGestures
import com.m3u.i18n.R.string
import com.m3u.material.components.Background
import com.m3u.material.components.Image
import com.m3u.material.components.MaskButton
import com.m3u.material.components.MaskCircleButton
import com.m3u.material.components.MaskState
import com.m3u.material.components.lockAndActive
import com.m3u.material.model.LocalSpacing
import com.m3u.ui.LocalHelper
import com.m3u.ui.Player
import com.m3u.ui.rememberPlayerState

@Composable
internal fun LiveFragment(
    playerState: LiveState.PlayerState,
    title: String,
    feedTitle: String,
    url: String,
    cover: String,
    maskState: MaskState,
    experimentalMode: Boolean,
    @ClipMode clipMode: Int,
    fullInfoPlayer: Boolean,
    recording: Boolean,
    stared: Boolean,
    volume: Float,
    light: Float,
    onVolume: (Float) -> Unit,
    onLight: (Float) -> Unit,
    onRecord: () -> Unit,
    onFavourite: () -> Unit,
    openDlnaDevices: () -> Unit,
    onBackPressed: () -> Unit,
    onInstallMedia: (String) -> Unit,
    onUninstallMedia: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = MaterialTheme.colorScheme

    // because they will be updated frequently,
    // they must be wrapped with rememberUpdatedState when using them.
    val currentVolume by rememberUpdatedState(volume)
    val currentLight by rememberUpdatedState(light)

    Background(
        color = Color.Black,
        contentColor = Color.White
    ) {
        Box(modifier) {
            val helper = LocalHelper.current
            val state = rememberPlayerState(
                player = playerState.player,
                url = url,
                clipMode = clipMode,
                onInstallMedia = onInstallMedia,
                onUninstallMedia = onUninstallMedia
            )

            Player(
                state = state,
                modifier = Modifier.fillMaxSize()
            )

            val shouldShowPlaceholder = cover.isNotEmpty() && playerState.videoSize.isEmpty

            CoverPlaceholder(
                visible = shouldShowPlaceholder,
                cover = cover,
                modifier = Modifier.align(Alignment.Center)
            )

            BoxWithConstraints {
                var gesture: MaskGesture? by rememberSaveable { mutableStateOf(null) }
                val muted = volume == 0f
                LiveMask(
                    state = maskState,
                    header = {
                        MaskButton(
                            state = maskState,
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            onClick = onBackPressed,
                            contentDescription = stringResource(string.feat_live_tooltip_on_back_pressed)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        MaskButton(
                            state = maskState,
                            icon = if (muted) Icons.AutoMirrored.Rounded.VolumeOff
                            else Icons.AutoMirrored.Rounded.VolumeUp,
                            onClick = {
                                onVolume(if (volume != 0f) 0f else 1f)
                            },
                            contentDescription = if (muted) stringResource(string.feat_live_tooltip_unmute)
                            else stringResource(string.feat_live_tooltip_mute),
                            tint = if (muted) theme.error else Color.Unspecified
                        )
                        MaskButton(
                            state = maskState,
                            icon = Icons.Rounded.Star,
                            tint = if (stared) Color.Yellow else Color.Unspecified,
                            onClick = onFavourite,
                            contentDescription = if (stared) stringResource(string.feat_live_tooltip_unfavourite)
                            else stringResource(string.feat_live_tooltip_favourite)
                        )
                        if (experimentalMode) {
                            MaskButton(
                                state = maskState,
                                enabled = false,
                                icon = if (recording) Icons.Rounded.RadioButtonChecked
                                else Icons.Rounded.RadioButtonUnchecked,
                                tint = if (recording) theme.error
                                else Color.Unspecified,
                                onClick = onRecord,
                                contentDescription = if (recording) stringResource(string.feat_live_tooltip_unrecord)
                                else stringResource(string.feat_live_tooltip_record)
                            )
                            if (playerState.playState != Player.STATE_IDLE) {
                                MaskButton(
                                    state = maskState,
                                    icon = Icons.Rounded.Cast,
                                    onClick = openDlnaDevices,
                                    contentDescription = stringResource(string.feat_live_tooltip_cast)
                                )
                            }
                        }
                        if (playerState.videoSize.isNotEmpty) {
                            MaskButton(
                                state = maskState,
                                icon = Icons.Rounded.PictureInPicture,
                                onClick = {
                                    helper.enterPipMode(playerState.videoSize)
                                    maskState.sleep()
                                },
                                contentDescription = stringResource(string.feat_live_tooltip_enter_pip_mode)
                            )
                        }
                    },
                    body = {
                        val icon = when (gesture) {
                            MaskGesture.LIGHT -> when {
                                light < 0.5f -> Icons.Rounded.Brightness2
                                else -> Icons.Rounded.Brightness7
                            }

                            MaskGesture.VOLUME -> when {
                                volume == 0f -> Icons.AutoMirrored.Rounded.VolumeOff
                                volume < 0.5f -> Icons.AutoMirrored.Rounded.VolumeDown
                                else -> Icons.AutoMirrored.Rounded.VolumeUp

                            }

                            null -> Icons.Rounded.Refresh
                        }
                        MaskCircleButton(
                            state = maskState,
                            icon = icon,
                            onClick = {
                                onInstallMedia(state.url)
                            }
                        )
                    },
                    footer = {
                        val spacing = LocalSpacing.current
                        if (fullInfoPlayer && cover.isNotEmpty()) {
                            Image(
                                model = cover,
                                modifier = Modifier
                                    .size(64.dp)
                                    .align(Alignment.Bottom)
                                    .clip(RoundedCornerShape(spacing.small))
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .semantics(mergeDescendants = true) { }
                                .fillMaxSize()
                        ) {
                            Text(
                                text = feedTitle,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.basicMarquee()
                            )
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.basicMarquee()
                            )
                            val playStateDisplayText =
                                LiveFragmentDefaults.playStateDisplayText(playerState.playState)
                            val exceptionDisplayText =
                                LiveFragmentDefaults.playbackExceptionDisplayText(playerState.playerError)
                            if (playStateDisplayText.isNotEmpty() || exceptionDisplayText.isNotEmpty()) {
                                Spacer(
                                    modifier = Modifier.height(spacing.small)
                                )
                            }
                            AnimatedVisibility(playStateDisplayText.isNotEmpty()) {
                                Text(
                                    text = playStateDisplayText.uppercase(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LocalContentColor.current.copy(alpha = 0.75f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.basicMarquee()
                                )
                            }
                            AnimatedVisibility(exceptionDisplayText.isNotEmpty()) {
                                Text(
                                    text = exceptionDisplayText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = theme.error,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.basicMarquee()
                                )
                            }
                            // TODO: implement servers ui here.

                        }
                    },
                    modifier = Modifier.detectVerticalMaskGestures(
                        volume = { deltaPixel ->
                            onVolume(
                                (currentVolume - (deltaPixel / maxHeight.value)).coerceIn(0f..1f)
                            )
                        },
                        light = { deltaPixel ->
                            onLight(
                                (currentLight - deltaPixel / maxHeight.value).coerceIn(0f..1f)
                            )
                        },
                        onDragStart = {
                            maskState.lockAndActive()
                            gesture = it
                        },
                        onDragEnd = {
                            maskState.unlock()
                            gesture = null
                        }
                    )
                )
            }

            LaunchedEffect(playerState.playerError) {
                if (playerState.playerError != null) {
                    maskState.active()
                }
            }
        }
    }
}

private enum class MaskGesture {
    VOLUME, LIGHT
}

private object LiveFragmentDefaults {
    @Composable
    fun Modifier.detectVerticalMaskGestures(
        volume: (pixel: Float) -> Unit,
        light: (pixel: Float) -> Unit,
        onDragStart: ((MaskGesture) -> Unit)? = null,
        onDragEnd: (() -> Unit)? = null
    ): Modifier {
        var gesture: MaskGesture? = null
        return this then Modifier.pointerInput(Unit) {
            detectVerticalDragGestures(
                onDragStart = { start ->
                    gesture = when (start.x) {
                        in 0f..size.width / 2f -> MaskGesture.LIGHT
                        else -> MaskGesture.VOLUME
                    }.also {
                        onDragStart?.invoke(it)
                    }
                },
                onDragEnd = {
                    onDragEnd?.invoke()
                    gesture = null
                },
                onDragCancel = {
                    gesture = null
                },
                onVerticalDrag = { _, dragAmount ->
                    when (gesture) {
                        MaskGesture.LIGHT -> light(dragAmount)
                        MaskGesture.VOLUME -> volume(dragAmount)
                        null -> {}
                    }
                }
            )
        }
    }

    @Composable
    fun playbackExceptionDisplayText(e: PlaybackException?): String = when (e) {
        null -> ""
        else -> "[${e.errorCode}] ${e.errorCodeName}"
    }

    @Composable
    fun playStateDisplayText(@Player.State state: Int): String = when (state) {
        Player.STATE_IDLE -> string.feat_live_playback_state_idle
        Player.STATE_BUFFERING -> string.feat_live_playback_state_buffering
        Player.STATE_READY -> null
        Player.STATE_ENDED -> string.feat_live_playback_state_ended
        else -> null
    }
        ?.let { stringResource(it) }
        .orEmpty()
}
