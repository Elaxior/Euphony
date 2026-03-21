package com.example.euphony.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request as OkHttpRequest
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response as OkHttpResponse
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.util.concurrent.TimeUnit

class DownloaderImpl private constructor(
    private val client: OkHttpClient
) : Downloader() {

    companion object {
        // Updated User-Agent to latest Chrome version (February 2026)
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"

        @Volatile
        private var instance: DownloaderImpl? = null

        fun getInstance(): DownloaderImpl {
            return instance ?: synchronized(this) {
                instance ?: DownloaderImpl(
                    OkHttpClient.Builder()
                        .readTimeout(30, TimeUnit.SECONDS)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .build()
                ).also { instance = it }
            }
        }
    }

    @Throws(
        ReCaptchaException::class,
        java.io.IOException::class
    )
    override fun execute(request: Request): Response {
        val requestBuilder = OkHttpRequest.Builder()
            .url(request.url())
            .addHeader("User-Agent", USER_AGENT)

        // Add custom headers
        request.headers().forEach { (key, values) ->
            values.forEach { value ->
                requestBuilder.addHeader(key, value)
            }
        }

        // Add request body if present (FIXED for OkHttp 4.x)
        val dataToSend = request.dataToSend()
        when (request.httpMethod()) {
            "GET" -> {
                requestBuilder.get()
            }
            "POST" -> {
                val body = if (dataToSend != null && dataToSend.isNotEmpty()) {
                    dataToSend.toRequestBody(null)
                } else {
                    ByteArray(0).toRequestBody(null)
                }
                requestBuilder.post(body)
            }
            "HEAD" -> {
                requestBuilder.head()
            }
            else -> {
                requestBuilder.method(request.httpMethod(), null)
            }
        }

        val response: OkHttpResponse = client.newCall(requestBuilder.build()).execute()

        if (response.code == 429) {
            response.close()
            throw ReCaptchaException("reCaptcha Challenge requested", request.url())
        }

        val responseBodyString = response.body?.string() ?: ""
        val responseHeaders = mutableMapOf<String, MutableList<String>>()

        response.headers.names().forEach { name ->
            responseHeaders[name] = response.headers(name).toMutableList()
        }

        return Response(
            response.code,
            response.message,
            responseHeaders,
            responseBodyString,
            response.request.url.toString()
        )
    }
}
