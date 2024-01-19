package me.rerere.awara.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import me.rerere.awara.ui.component.common.DialogProvider
import me.rerere.awara.ui.component.common.MessageProvider
import me.rerere.awara.ui.page.favorites.FavoritesPage
import me.rerere.awara.ui.page.follow.FollowPage
import me.rerere.awara.ui.page.history.HistoryPage
import me.rerere.awara.ui.page.image.ImagePage
import me.rerere.awara.ui.page.index.IndexPage
import me.rerere.awara.ui.page.lab.LabPage
import me.rerere.awara.ui.page.login.LoginPage
import me.rerere.awara.ui.page.playlist.PlaylistDetailPage
import me.rerere.awara.ui.page.playlist.PlaylistsPage
import me.rerere.awara.ui.page.search.SearchPage
import me.rerere.awara.ui.page.setting.SettingPage
import me.rerere.awara.ui.page.user.UserPage
import me.rerere.awara.ui.page.video.VideoPage
import me.rerere.awara.ui.stores.LocalUserStore
import me.rerere.awara.ui.stores.UserStoreProvider
import me.rerere.awara.ui.stores.collectAsState
import me.rerere.awara.ui.theme.AwaraTheme


class RouterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val splashScreen = installSplashScreen().apply { setKeepOnScreenCondition { true } }
        super.onCreate(savedInstanceState)
        setContent {
            AwaraTheme {
                ContextProvider {
                    UserStoreProvider {
                        val userState = LocalUserStore.current.collectAsState()
                        LaunchedEffect(userState.refreshing) {
                            splashScreen.setKeepOnScreenCondition{ userState.refreshing }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!userState.refreshing) {
                                Routes()
                            } else {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ContextProvider(
        content: @Composable () -> Unit
    ) {
        MessageProvider {
            DialogProvider {
                content()
            }
        }
    }

    @Composable
    private fun Routes() {
        val navController = rememberAnimatedNavController()
        CompositionLocalProvider(
            LocalRouterProvider provides navController
        ) {
            AnimatedNavHost(
                modifier = Modifier
                    .fillMaxSize()
                    // 防止夜间模式下切换页面闪白屏
                    .background(MaterialTheme.colorScheme.background),
                navController = navController,
                startDestination = "index",
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { 1000 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { 1000 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(100))
                }
            ) {
                composable(
                    route = "index",
                    enterTransition = { null },
                    exitTransition = { null },
                ) {
                    IndexPage()
                }

                composable("login") {
                    LoginPage()
                }

                composable(
                    route = "video/{id}",
                    arguments = listOf(
                        navArgument("id") {
                            type = NavType.StringType
                        }
                    )
                ) {
                    VideoPage()
                }

                composable("image/{id}") {
                    ImagePage()
                }

                composable("user/{id}") {
                    UserPage()
                }

                composable(
                    route = "user/{userId}/follow",
                    arguments = listOf(
                        navArgument("userId") {
                            type = NavType.StringType
                        }
                    )
                ) {
                    FollowPage()
                }

                composable(
                    route = "playlists/{userId}",
                    arguments = listOf(
                        navArgument("userId") {
                            type = NavType.StringType
                        }
                    )
                ) {
                    PlaylistsPage()
                }

                composable(
                    route = "playlist/{id}",
                    arguments = listOf(
                        navArgument("id") {
                            type = NavType.StringType
                        }
                    )
                ) {
                    PlaylistDetailPage()
                }

                composable("favorites") {
                    FavoritesPage()
                }

                composable("setting") {
                    SettingPage()
                }

                composable("search") {
                    SearchPage()
                }

                composable("history") {
                    HistoryPage()
                }

                composable("lab") {
                    LabPage()
                }
            }
        }
    }
}