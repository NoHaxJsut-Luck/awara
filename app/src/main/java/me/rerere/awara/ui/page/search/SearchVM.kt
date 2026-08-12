package me.rerere.awara.ui.page.search

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.rerere.awara.data.entity.Image
import me.rerere.awara.data.entity.User
import me.rerere.awara.data.entity.Video
import me.rerere.awara.data.repo.MediaRepo
import me.rerere.awara.data.repo.SearchSort
import me.rerere.awara.data.source.Pager
import me.rerere.awara.data.source.onError
import me.rerere.awara.data.source.onException
import me.rerere.awara.data.source.onSuccess
import me.rerere.awara.data.source.runAPICatching
import me.rerere.awara.data.source.stringResource
import me.rerere.awara.ui.component.common.UiState

class SearchVM(
    private val mediaRepo: MediaRepo
) : ViewModel() {
    private var searchJob: Job? = null

    var state by mutableStateOf(SearchState())
        private set
    var query by mutableStateOf("")

    fun search() {
        executeSearch(page = 1)
    }

    fun retry() {
        executeSearch(page = state.page)
    }

    private fun executeSearch(page: Int) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) {
            searchJob?.cancel()
            state = SearchState(
                searchType = state.searchType,
                searchSort = state.searchSort,
            )
            return
        }

        searchJob?.cancel()
        val requestedType = state.searchType
        val requestedSort = state.searchSort
        val requestedPage = page.coerceAtLeast(1)
        state = state.copy(
            uiState = UiState.Loading,
            page = requestedPage,
            count = 0,
            videoList = emptyList(),
            imageList = emptyList(),
            userList = emptyList()
        )
        searchJob = viewModelScope.launch {
            val result = runAPICatching {
                when (requestedType) {
                    "video" -> SearchResult.Videos(
                        mediaRepo.searchVideo(normalizedQuery, requestedPage - 1, requestedSort)
                    )
                    "image" -> SearchResult.Images(
                        mediaRepo.searchImage(normalizedQuery, requestedPage - 1, requestedSort)
                    )
                    "user" -> SearchResult.Users(
                        mediaRepo.searchUser(normalizedQuery, requestedPage - 1)
                    )
                    else -> error("Unsupported search type: $requestedType")
                }
            }
            if (!isActive) return@launch

            result.onSuccess { searchResult ->
                state = when (searchResult) {
                    is SearchResult.Videos -> state.withVideos(searchResult.pager)
                    is SearchResult.Images -> state.withImages(searchResult.pager)
                    is SearchResult.Users -> state.withUsers(searchResult.pager)
                }
            }.onError {
                state = state.copy(uiState = UiState.Error(
                    message = {
                        Text(stringResource(error = it))
                    }
                ))
            }.onException {
                state = state.copy(uiState = UiState.Error(
                    message = {
                        Text(it.exception.localizedMessage ?: "Unknown Error")
                    }
                ))
            }
        }
    }

    fun jumpToPage(page: Int) {
        executeSearch(page = page)
    }

    fun updateSearchType(type: String) {
        if (type == state.searchType) return

        searchJob?.cancel()
        state = SearchState(searchType = type, searchSort = state.searchSort)
        if (query.isNotBlank()) {
            executeSearch(page = 1)
        }
    }

    fun updateSearchSort(sort: SearchSort) {
        if (sort == state.searchSort) return

        searchJob?.cancel()
        state = SearchState(searchType = state.searchType, searchSort = sort)
        if (query.isNotBlank()) {
            executeSearch(page = 1)
        }
    }

    data class SearchState(
        val uiState: UiState = UiState.Initial,
        val searchType: String = "video",
        val searchSort: SearchSort = SearchSort.DATE_DESCENDING,
        val page: Int = 1,
        val count: Int = 0,
        val videoList: List<Video> = emptyList(),
        val imageList: List<Image> = emptyList(),
        val userList: List<User> = emptyList(),
    ) {
        fun withVideos(pager: Pager<Video>) = copy(
            uiState = if (pager.results.isEmpty()) UiState.Empty else UiState.Success,
            count = pager.count,
            videoList = pager.results
        )

        fun withImages(pager: Pager<Image>) = copy(
            uiState = if (pager.results.isEmpty()) UiState.Empty else UiState.Success,
            count = pager.count,
            imageList = pager.results
        )

        fun withUsers(pager: Pager<User>) = copy(
            uiState = if (pager.results.isEmpty()) UiState.Empty else UiState.Success,
            count = pager.count,
            userList = pager.results
        )
    }

    private sealed interface SearchResult {
        data class Videos(val pager: Pager<Video>) : SearchResult
        data class Images(val pager: Pager<Image>) : SearchResult
        data class Users(val pager: Pager<User>) : SearchResult
    }
}
