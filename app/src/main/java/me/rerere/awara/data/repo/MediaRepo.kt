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

    suspend fun searchVideo(query: String, page: Int): Pager<Video> = iwaraAPI.searchVideo(query = query, page = page, type = "video")

    suspend fun searchImage(query: String, page: Int): Pager<Image> = iwaraAPI.searchImage(query = query, page = page, type = "image")

    suspend fun searchUser(query: String, page: Int): Pager<User> = iwaraAPI.searchUser(query = query, page = page, type = "user")
}
