package me.rerere.awara.data.repo

import kotlinx.coroutines.runBlocking
import me.rerere.awara.data.source.IwaraAPI
import me.rerere.awara.util.SerializationConverterFactory
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
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
        assertEquals("videos", server.takeRequest().requestUrl?.queryParameter("type"))

        enqueueEmptyPage()
        repo.searchImage("mmd", 0)
        assertEquals("images", server.takeRequest().requestUrl?.queryParameter("type"))

        enqueueEmptyPage()
        repo.searchUser("mmd", 0)
        assertEquals("users", server.takeRequest().requestUrl?.queryParameter("type"))
    }

    private fun enqueueEmptyPage() {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""{"page":0,"count":0,"limit":32,"results":[]}""")
        )
    }
}
