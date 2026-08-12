package me.rerere.awara.data.repo

import kotlinx.coroutines.runBlocking
import me.rerere.awara.data.source.IwaraAPI
import me.rerere.awara.util.SerializationConverterFactory
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class MediaRepoSearchTest {
    private lateinit var server: MockWebServer
    private lateinit var repo: MediaRepo

    @Before
    fun setUp() {
        server = MockWebServer()
        val client = OkHttpClient()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(SerializationConverterFactory.create())
            .build()
            .create(IwaraAPI::class.java)
        repo = MediaRepo(client, api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `search uses plural API types required by iwara`() = runBlocking {
        enqueueEmptyPage()
        repo.searchVideo("mmd", 0)
        server.takeRequest().requestUrl?.let { url ->
            assertEquals("videos", url.queryParameter("type"))
            assertEquals("date", url.queryParameter("sort"))
        }

        enqueueEmptyPage()
        repo.searchImage("mmd", 0)
        server.takeRequest().requestUrl?.let { url ->
            assertEquals("images", url.queryParameter("type"))
            assertEquals("date", url.queryParameter("sort"))
        }

        enqueueEmptyPage()
        repo.searchUser("mmd", 0)
        assertEquals("users", server.takeRequest().requestUrl?.queryParameter("type"))
    }

    @Test
    fun `media search sends likes and views sort values`() = runBlocking {
        enqueueEmptyPage()
        repo.searchVideo("mmd", 0, SearchSort.LIKES)
        assertEquals("likes", server.takeRequest().requestUrl?.queryParameter("sort"))

        enqueueEmptyPage()
        repo.searchImage("mmd", 0, SearchSort.VIEWS)
        assertEquals("views", server.takeRequest().requestUrl?.queryParameter("sort"))
    }

    @Test
    fun `ascending date maps descending API pages into global oldest first order`() = runBlocking {
        val requestedPages = mutableListOf<Int>()
        val descendingPages = mapOf(
            0 to (35 downTo 4).toList(),
            1 to listOf(3, 2, 1),
        )

        val first = repo.searchMedia(page = 0, sort = SearchSort.DATE_ASCENDING) { page ->
            requestedPages += page
            me.rerere.awara.data.source.Pager(
                page = page,
                count = 35,
                limit = 32,
                results = descendingPages.getValue(page),
            )
        }
        assertEquals((1..32).toList(), first.results)
        assertEquals(listOf(0, 1), requestedPages)

        requestedPages.clear()
        val second = repo.searchMedia(page = 1, sort = SearchSort.DATE_ASCENDING) { page ->
            requestedPages += page
            me.rerere.awara.data.source.Pager(
                page = page,
                count = 35,
                limit = 32,
                results = descendingPages.getValue(page),
            )
        }
        assertEquals(listOf(33, 34, 35), second.results)
        assertEquals(listOf(0), requestedPages)
        assertTrue(second.results.zipWithNext().all { (left, right) -> left < right })
    }

    private fun enqueueEmptyPage() {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""{"page":0,"count":0,"limit":32,"results":[]}""")
        )
    }
}
