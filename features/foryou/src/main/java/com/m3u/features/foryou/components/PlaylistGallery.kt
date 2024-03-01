package com.m3u.features.foryou.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import com.m3u.data.database.model.Playlist
import com.m3u.features.foryou.model.PlaylistDetail
import com.m3u.i18n.R.string
import com.m3u.material.ktx.plus
import com.m3u.material.model.LocalSpacing
import com.m3u.ui.UiMode
import com.m3u.ui.currentUiMode
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun PlaylistGallery(
    rowCount: Int,
    details: ImmutableList<PlaylistDetail>,
    navigateToPlaylist: (Playlist) -> Unit,
    onMenu: (Playlist) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    when (currentUiMode()) {
        UiMode.Default -> {
            PlaylistGalleryImpl(
                rowCount = rowCount,
                details = details,
                navigateToPlaylist = navigateToPlaylist,
                onMenu = onMenu,
                contentPadding = contentPadding,
                modifier = modifier
            )
        }

        UiMode.Television -> {
            TvPlaylistGalleryImpl(
                rowCount = rowCount,
                details = details,
                navigateToPlaylist = navigateToPlaylist,
                onMenu = onMenu,
                contentPadding = contentPadding,
                modifier = modifier
            )
        }

        UiMode.Compat -> {
            CompactPlaylistGalleryImpl(
                rowCount = rowCount,
                details = details,
                navigateToPlaylist = navigateToPlaylist,
                onMenu = onMenu,
                contentPadding = contentPadding,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun PlaylistGalleryImpl(
    rowCount: Int,
    details: ImmutableList<PlaylistDetail>,
    navigateToPlaylist: (Playlist) -> Unit,
    onMenu: (Playlist) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(rowCount),
        contentPadding = PaddingValues(spacing.medium) + contentPadding,
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        modifier = modifier
    ) {
        items(
            items = details,
            key = { it.playlist.url },
            contentType = {}
        ) { detail ->
            PlaylistItem(
                label = detail.playlist.calculateUiTitle(),
                type = detail.playlist.type,
                typeWithSource = detail.playlist.typeWithSource,
                number = detail.count,
                local = detail.playlist.fromLocal,
                modifier = Modifier.fillMaxWidth(),
                onClick = { navigateToPlaylist(detail.playlist) },
                onLongClick = { onMenu(detail.playlist) }
            )
        }
    }
}

@Composable
private fun TvPlaylistGalleryImpl(
    rowCount: Int,
    details: ImmutableList<PlaylistDetail>,
    navigateToPlaylist: (Playlist) -> Unit,
    onMenu: (Playlist) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    TvLazyVerticalGrid(
        columns = TvGridCells.Fixed(rowCount),
        contentPadding = PaddingValues(
            vertical = spacing.medium,
            horizontal = spacing.large
        ) + contentPadding,
        verticalArrangement = Arrangement.spacedBy(spacing.large),
        horizontalArrangement = Arrangement.spacedBy(spacing.large),
        modifier = modifier
    ) {
        items(
            items = details,
            key = { it.playlist.url },
            contentType = {}
        ) { detail ->
            PlaylistItem(
                label = detail.playlist.calculateUiTitle(),
                type = detail.playlist.type,
                typeWithSource = detail.playlist.typeWithSource,
                number = detail.count,
                local = detail.playlist.fromLocal,
                modifier = Modifier.fillMaxWidth(),
                onClick = { navigateToPlaylist(detail.playlist) },
                onLongClick = { onMenu(detail.playlist) }
            )
        }
    }
}

@Composable
private fun CompactPlaylistGalleryImpl(
    rowCount: Int,
    details: ImmutableList<PlaylistDetail>,
    navigateToPlaylist: (Playlist) -> Unit,
    onMenu: (Playlist) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(rowCount),
        contentPadding = contentPadding,
        modifier = modifier.fillMaxSize()
    ) {
        items(
            items = details,
            key = { it.playlist.url },
            contentType = {}
        ) { detail ->
            PlaylistItem(
                label = detail.playlist.calculateUiTitle(),
                type = detail.playlist.type,
                typeWithSource = detail.playlist.typeWithSource,
                number = detail.count,
                local = detail.playlist.fromLocal,
                modifier = Modifier.fillMaxWidth(),
                onClick = { navigateToPlaylist(detail.playlist) },
                onLongClick = { onMenu(detail.playlist) }
            )
        }
    }
}

@Composable
private fun Playlist.calculateUiTitle(): String {
    val actual = title.ifEmpty {
        if (fromLocal) stringResource(string.feat_foryou_imported_playlist_title)
        else ""
    }
    return actual.uppercase()
}