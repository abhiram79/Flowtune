package com.abhiram.innertube.pages

import com.abhiram.innertube.models.Album
import com.abhiram.innertube.models.AlbumItem
import com.abhiram.innertube.models.Artist
import com.abhiram.innertube.models.ArtistItem
import com.abhiram.innertube.models.MusicResponsiveListItemRenderer
import com.abhiram.innertube.models.MusicTwoRowItemRenderer
import com.abhiram.innertube.models.PlaylistItem
import com.abhiram.innertube.models.SongItem
import com.abhiram.innertube.models.YTItem
import com.abhiram.innertube.models.oddElements
import com.abhiram.innertube.utils.parseTime

data class LibraryAlbumsPage(
    val albums: List<AlbumItem>,
    val continuation: String?,
) {
    companion object {
        fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): AlbumItem? {
            return AlbumItem(
                        browseId = renderer.navigationEndpoint.browseEndpoint?.browseId ?: return null,
                        playlistId = renderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content
                            ?.musicPlayButtonRenderer?.playNavigationEndpoint
                            ?.watchPlaylistEndpoint?.playlistId ?: return null,
                        title = renderer.title.runs?.firstOrNull()?.text ?: return null,
                        artists = null,
                        year = renderer.subtitle?.runs?.lastOrNull()?.text?.toIntOrNull(),
                        thumbnail = renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                        explicit = renderer.subtitleBadges?.find {
                            it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                        } != null
                    )
        }
    }
}
