package com.jramberger.mysteries.extension.en.novelfull

import com.jramberger.mysteries.extension.ext.asJsoup
import com.jramberger.mysteries.extension.model.Language
import com.jramberger.mysteries.extension.model.Novel
import com.jramberger.mysteries.extension.model.NovelMetadata
import com.jramberger.mysteries.extension.source.NovelSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber

class NovelFull : NovelSource {
    override val id: Long
        get() = 0

    override val name: String
        get() = "NovelFull"

    override val language: Language
        get() = Language.EN

    override val baseUrl: String
        get() = "https://novelfull.com"

    override fun getPopularNovels(
        client: OkHttpClient,
        requestBuilder: Request.Builder,
        pageNumber: Int,
    ): List<NovelMetadata> {
        val request =
            requestBuilder
                .url("$baseUrl/most-popular?page=$pageNumber")
                .build()

        val response = client.newCall(request).execute()
        Timber.i("Response code: ${response.code}")

        val document = response.asJsoup()

        return document.select("div.list:not(.list-side):not(.list-cat) > div.row").map { row ->
            NovelMetadata(
                id = row.selectFirst("h3.truyen-title > a")?.attr("href") ?: "",
                title = row.selectFirst("h3.truyen-title")?.text() ?: "",
                coverUrl =
                    row.selectFirst("img.cover")?.attr("src")?.let {
                        if (it.startsWith("/")) {
                            baseUrl + it
                        } else {
                            it
                        }
                    },
            )
        }
    }

    override fun getNovelDetails(
        client: OkHttpClient,
        requestBuilder: Request.Builder,
        novelId: String,
    ): StateFlow<Novel> {
        val novel =
            Novel(
                id = novelId,
            )

        val flow = MutableStateFlow(novel)

        CoroutineScope(Dispatchers.IO).launch {
            val request =
                requestBuilder
                    .url("$baseUrl/$novelId")
                    .build()

            val response = client.newCall(request).execute()
            Timber.i("Response code: ${response.code}")

            val document = response.asJsoup()
            novel.title = document.selectFirst("h3.title")?.text()
            novel.coverUrl = document.selectFirst("div.books > div.book > img")?.attr("src")

            document.selectFirst("div.info").let { info ->
                if (info == null) return@let

                novel.authors = info.select("div:nth-child(1) > a").map { it.text() }
            }

            flow.emit(novel)
        }

        return flow
    }
}
