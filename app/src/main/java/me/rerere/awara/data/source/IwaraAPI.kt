package me.rerere.awara.data.source

import me.rerere.awara.data.dto.AccessTokenRes
import me.rerere.awara.data.dto.LoginReq
import me.rerere.awara.data.dto.LoginRes
import me.rerere.awara.data.dto.ProfileDto
import me.rerere.awara.data.dto.Self
import me.rerere.awara.data.entity.Comment
import me.rerere.awara.data.entity.CommentCreationDto
import me.rerere.awara.data.entity.FavoriteImage
import me.rerere.awara.data.entity.FavoriteVideo
import me.rerere.awara.data.entity.Follower
import me.rerere.awara.data.entity.Following
import me.rerere.awara.data.entity.Image
import me.rerere.awara.data.entity.Playlist
import me.rerere.awara.data.entity.PlaylistCreationDto
import me.rerere.awara.data.entity.PlaylistPager
import me.rerere.awara.data.entity.Tag
import me.rerere.awara.data.entity.User
import me.rerere.awara.data.entity.Video
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface IwaraAPI {
    @POST("/user/login")
    suspend fun login(
        @Body loginReq: LoginReq
    ): LoginRes

    @POST("/user/token")
    suspend fun renewToken(): AccessTokenRes

    @GET("/user")
    suspend fun getSelfProfile(): Self

    @GET("/profile/{username}")
    suspend fun getProfile(
        @Path("username") username: String
    ): ProfileDto

    @GET("/videos")
    suspend fun getVideoList(
        @QueryMap queryMap: Map<String, String>
    ): Pager<Video>

    @GET("/images")
    suspend fun getImageList(
        @QueryMap queryMap: Map<String, String>
    ): Pager<Image>

    @GET("/video/{id}")
    suspend fun getVideo(
        @Path("id") id: String
    ): Video

    @GET("/video/{id}/related")
    suspend fun getRelatedVideo(
        @Path("id") id: String
    ): Pager<Video>

    @POST("/video/{id}/like")
    suspend fun likeVideo(
        @Path("id") id: String
    ): Response<Unit>

    @DELETE("/video/{id}/like")
    suspend fun unlikeVideo(
        @Path("id") id: String
    ): Response<Unit>

    @POST("/image/{id}/like")
    suspend fun likeImage(
        @Path("id") id: String
    ): Response<Unit>

    @DELETE("/image/{id}/like")
    suspend fun unlikeImage(
        @Path("id") id: String
    ): Response<Unit>

    @GET("/video/{id}/comments")
    suspend fun getVideoComments(
        @Path("id") id: String,
        @QueryMap queryMap: Map<String, String>
    ): Pager<Comment>

    @POST("/video/{id}/comments")
    suspend fun postVideoComment(
        @Path("id") id: String,
        @Body comment: CommentCreationDto
    ): Comment

    @GET("/image/{id}")
    suspend fun getImage(
        @Path("id") id: String
    ): Image

    @POST("/user/{id}/followers")
    suspend fun followUser(
        @Path("id") id: String
    ): Response<Unit>

    @DELETE("/user/{id}/followers")
    suspend fun unfollowUser(
        @Path("id") id: String
    ): Response<Unit>

    @GET("/autocomplete/tags")
    suspend fun autoCompleteTags(
        @Query("query") query: String
    ): Pager<Tag>

    @GET("/playlists")
    suspend fun getPlaylists(
        @QueryMap queryMap: Map<String, String>
    ): Pager<Playlist>

    @GET("/playlist/{id}")
    suspend fun getPlaylist(
        @Path("id") id: String,
        @Query("page") page: Int,
    ): PlaylistPager

    @GET("/light/playlists")
    suspend fun getLightPlaylists(
        @Query("id") id: String
    ): List<Playlist>

    @POST("/playlist/{id}/{videoId}")
    suspend fun addVideoToPlaylist(
        @Path("id") id: String,
        @Path("videoId") videoId: String
    ): Response<Unit>

    @DELETE("/playlist/{id}/{videoId}")
    suspend fun removeVideoFromPlaylist(
        @Path("id") id: String,
        @Path("videoId") videoId: String
    ): Response<Unit>

    @POST("/playlists")
    suspend fun createPlaylist(
        @Body dto: PlaylistCreationDto
    ): Playlist

    @GET("/user/{userId}/followers")
    suspend fun getUserFollowers(
        @Path("userId") userId: String,
        @QueryMap queryMap: Map<String, String>
    ): Pager<Follower>

    @GET("/user/{userId}/following")
    suspend fun getUserFollowing(
        @Path("userId") userId: String,
        @QueryMap queryMap: Map<String, String>
    ): Pager<Following>

    @GET("/user/{userId}/friends")
    suspend fun getUserFriends(
        @Path("userId") userId: String,
        @QueryMap queryMap: Map<String, String>
    ): Pager<User>

    @GET("/user/{userId}/friends/requests")
    suspend fun getUserFriendRequests(
        @Path("userId") userId: String,
        @QueryMap queryMap: Map<String, String>
    ): Pager<User>

    @GET("/favorites/videos")
    suspend fun getFavoriteVideos(
        @Query("page") page: Int
    ): Pager<FavoriteVideo>

    @GET("/favorites/images")
    suspend fun getFavoriteImages(
        @Query("page") page: Int
    ): Pager<FavoriteImage>

    @GET("/search")
    suspend fun searchVideo(
        @Query("type") type: String,
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("sort") sort: String,
    ): Pager<Video>

    @GET("/search")
    suspend fun searchImage(
        @Query("type") type: String,
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("sort") sort: String,
    ): Pager<Image>

    @GET("/search")
    suspend fun searchUser(
        @Query("type") type: String,
        @Query("query") query: String,
        @Query("page") page: Int
    ): Pager<User>
}
