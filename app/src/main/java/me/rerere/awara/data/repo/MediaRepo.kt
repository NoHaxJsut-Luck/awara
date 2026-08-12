package me.rerere.awara.data.repo

import kotlinx.serialization.decodeFromString
import me.rerere.awara.data.entity.FavoriteImage
import me.rerere.awara.data.entity.FavoriteVideo
import me.rerere.awara.data.entity.Image
import me.rerere.awara.data.entity.PlaylistCreationDto
import me.rerere.awara.data.entity.User
import me.rerere.awara.data.entity.Video
import me.rerere.awara.data.entity.VideoFile
import me.rerere.awara.data.entity.signature
import me.rerere.awara.data.source.IwaraAPI
import me.rerere.awara.data.source.Pager
import me.rerere.awara.util.JsonInstance
import me.rerere.awara.util.await
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class MediaRepo(
    private val okHttpClient: OkHttpClient,
    private val iwaraAPI: IwaraAPI
) {
    suspend fun getVideoList(
        queryMap: Map<String, String>
    ) = iwaraAPI.getVideoList(queryMap)

    suspend fun getImageList(
        queryMap: Map<String, String>
    ) = iwaraAPI.getImageList(queryMap)

    suspend fun getVideo(
        id: String
    ) = iwaraAPI.getVideo(id)

    suspend fun parseVideoUrl(
        video: Video
    ): List<VideoFile> {
        val hash = video.signature
        val request = Request.Builder()
            .url(video.fileUrl ?: error("No file url"))
            .header("x-version", hash)
            .get()
            .build()
        return okHttpClient.newCall(request).await().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Video source request failed with HTTP ${response.code}")
            }
            val bodyString = response.body?.string() ?: throw IOException("Video source response has no body")
            JsonInstance.decodeFromString(bodyString)
        }
    }

    suspend fun getRelatedVideos(id: String) = iwaraAPI.getRelatedVideo(id)

    suspend fun likeVideo(id: String) = iwaraAPI.likeVideo(id)

    suspend fun unlikeVideo(id: String) = iwaraAPI.unlikeVideo(id)

    suspend fun likeImage(id: String) = iwaraAPI.likeImage(id)

    suspend fun unlikeImage(id: String) = iwaraAPI.unlikeImage(id)

    suspend fun getImage(id: String) = iwaraAPI.getImage(id)

    suspend fun getTagsSuggestions(query: String) = iwaraAPI.autoCompleteTags(query)

    suspend fun getPlaylists(userId: String, page: Int) = iwaraAPI.getPlaylists(
        mapOf(
            "page" to page.toString(),
            "user" to userId
        )
    )

    suspend fun getPlaylistContent(playlistId: String, page: Int) =
        iwaraAPI.getPlaylist(id = playlistId, page = page)

    suspend fun getLightPlaylist(videoId: String) = iwaraAPI.getLightPlaylists(videoId)

    suspend fun addVideoToPlaylist(playlistId: String, videoId: String) =
        iwaraAPI.addVideoToPlaylist(playlistId, videoId)

    suspend fun removeVideoFromPlaylist(playlistId: String, videoId: String) =
        iwaraAPI.removeVideoFromPlaylist(playlistId, videoId)

    suspend fun createPlaylist(title: String) = iwaraAPI.createPlaylist(PlaylistCreationDto(title = title))

    suspend fun getFavoriteVideos(page: Int): Pager<FavoriteVideo> = iwaraAPI.getFavoriteVideos(page = page)

    suspend fun getFavoriteImages(page: Int): Pager<FavoriteImage> = iwaraAPI.getFavoriteImages(page = page)

    suspend fun searchVideo(
        query: String,
        page: Int,
        sort: SearchSort = SearchSort.DATE_DESCENDING,
    ): Pager<Video> = searchMedia(page, sort) { requestedPage ->
        iwaraAPI.searchVideo(
            query = query,
            page = requestedPage,
            type = "videos",
            sort = sort.apiValue,
        )
    }

    suspend fun searchImage(
        query: String,
        page: Int,
        sort: SearchSort = SearchSort.DATE_DESCENDING,
    ): Pager<Image> = searchMedia(page, sort) { requestedPage ->
        iwaraAPI.searchImage(
            query = query,
            page = requestedPage,
            type = "images",
            sort = sort.apiValue,
        )
    }

    suspend fun searchUser(query: String, page: Int): Pager<User> =
        iwaraAPI.searchUser(query = query, page = page, type = "users")

    internal suspend fun <T> searchMedia(
        page: Int,
        sort: SearchSort,
        fetchPage: suspend (Int) -> Pager<T>,
    ): Pager<T> {
        require(page >= 0) { "Search page must not be negative" }
        if (sort != SearchSort.DATE_ASCENDING) return fetchPage(page)

        // Iwara only exposes descending media sorts. Map an ascending UI page to
        // the matching descending API page(s), then reverse the selected slice.
        val firstResponse = fetchPage(0)
        val limit = firstResponse.limit.takeIf { it > 0 } ?: 32
        val ascendingStart = page.toLong() * limit
        if (ascendingStart >= firstResponse.count) {
            return Pager(firstResponse.count, limit, page, emptyList())
        }

        val ascendingEnd = minOf(firstResponse.count.toLong(), ascendingStart + limit)
        val descendingStart = firstResponse.count.toLong() - ascendingEnd
        val descendingEnd = firstResponse.count.toLong() - ascendingStart
        val firstApiPage = (descendingStart / limit).toInt()
        val lastApiPage = ((descendingEnd - 1) / limit).toInt()
        val pages = (firstApiPage..lastApiPage).associateWith { apiPage ->
            if (apiPage == 0) firstResponse else fetchPage(apiPage)
        }
        val selectedDescending = buildList {
            for (apiPage in firstApiPage..lastApiPage) {
                pages.getValue(apiPage).results.forEachIndexed { index, item ->
                    val descendingIndex = apiPage.toLong() * limit + index
                    if (descendingIndex in descendingStart until descendingEnd) add(item)
                }
            }
        }
        return Pager(
            count = firstResponse.count,
            limit = limit,
            page = page,
            results = selectedDescending.asReversed(),
        )
    }
}

enum class SearchSort(val apiValue: String) {
    DATE_ASCENDING("date"),
    DATE_DESCENDING("date"),
    LIKES("likes"),
    VIEWS("views"),
}
