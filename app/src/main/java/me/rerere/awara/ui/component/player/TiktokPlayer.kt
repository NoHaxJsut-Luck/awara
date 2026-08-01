package me.rerere.awara.ui.component.player

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import me.rerere.awara.ui.hooks.ForceSystemBarColor

private const val TAG = "TiktokPlayer"

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun TiktokPlayer(
    generateVideoUrl: suspend () -> String
) {
    ForceSystemBarColor(appearanceLight = false)
    val pagerState = rememberPagerState()

    VerticalPager(
        state = pagerState,
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize(),
        pageCount = Int.MAX_VALUE,
    ) { index ->
        val playerState = rememberPlayerState {
            ExoPlayer.Builder(it)
                .setHandleAudioBecomingNoisy(true)
                .build()
                .apply {
                    playWhenReady = false
                }
        }
        playerState.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

        LaunchedEffect(Unit) {
            playerState.updatePlayerItems(listOf(
                PlayerState.PlayerItem("Source", androidx.media3.common.MediaItem.fromUri(generateVideoUrl())
            )))
            playerState.prepare()
            Log.i(TAG, "TiktokPlayer: prepared player #$index")
        }

        LaunchedEffect(pagerState.currentPage) {
            Log.i(TAG, "TiktokPlayer: ${pagerState.currentPage}")

            if (pagerState.currentPage == index) {
                playerState.play()
            } else {
                playerState.pause()
            }
        }

        PlayerBase(
            state = playerState,
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxSize()
        )
    }
}
