package me.rerere.awara.ui.page.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.rerere.awara.R
import me.rerere.awara.ui.component.common.BackButton
import me.rerere.awara.ui.component.common.SelectButton
import me.rerere.awara.ui.component.common.SelectOption
import me.rerere.awara.ui.component.common.UiState
import me.rerere.awara.ui.component.common.UiStateBox
import me.rerere.awara.ui.component.ext.DynamicStaggeredGridCells
import me.rerere.awara.ui.component.iwara.MediaCard
import me.rerere.awara.ui.component.iwara.PaginationBar
import me.rerere.awara.ui.component.iwara.UserCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchPage(vm: SearchVM = koinViewModel()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.search))
                },
                navigationIcon = {
                    BackButton()
                }
            )
        },
        bottomBar = {
            PaginationBar(
                page = vm.state.page,
                limit = 32,
                total = vm.state.count,
                onPageChange = {
                     vm.jumpToPage(it)
                },
                contentPadding = WindowInsets.navigationBars.asPaddingValues()
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DockedSearchBar(
                query = vm.query,
                onQueryChange = { vm.query = it },
                onSearch = { vm.search() },
                active = false,
                onActiveChange = {},
                content = {},
                leadingIcon = {
                    SelectButton(
                        value = vm.state.searchType,
                        options = SearchOptions,
                        onValueChange = {
                            vm.updateSearchType(it)
                        }
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { vm.search() },
                        enabled = vm.query.isNotBlank() && vm.state.uiState != UiState.Loading
                    ) {
                        Icon(Icons.Outlined.Search, null)
                    }
                }
            )

            UiStateBox(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = vm.state.uiState,
                onErrorRetry = {
                    vm.retry()
                }
            ) {
                LazyVerticalStaggeredGrid(
                    columns = DynamicStaggeredGridCells(150.dp, 2, 4),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp,
                    modifier = Modifier.matchParentSize()
                ) {
                    when (vm.state.searchType) {
                        "video" -> {
                            items(vm.state.videoList) {
                                MediaCard(media = it)
                            }
                        }

                        "image" -> {
                            items(vm.state.imageList) {
                                MediaCard(media = it)
                            }
                        }

                        "user" -> {
                            items(vm.state.userList) {
                                UserCard(user = it)
                            }
                        }
                    }
                }
            }
        }
    }
}

private val SearchOptions = listOf(
    SelectOption(
        value = "video",
        label = {
            Text(stringResource(R.string.video))
        }
    ),
    SelectOption(
        value = "image",
        label = {
            Text(stringResource(R.string.image))
        }
    ),
    SelectOption(
        value = "user",
        label = {
            Text(stringResource(R.string.user))
        }
    )
)
