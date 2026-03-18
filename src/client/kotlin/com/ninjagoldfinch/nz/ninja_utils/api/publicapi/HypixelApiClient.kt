package com.ninjagoldfinch.nz.ninja_utils.api.publicapi

import com.google.gson.Gson
import com.ninjagoldfinch.nz.ninja_utils.api.publicapi.dto.BazaarResponse
import com.ninjagoldfinch.nz.ninja_utils.api.publicapi.dto.ElectionResponse
import com.ninjagoldfinch.nz.ninja_utils.config.ApiCategory
import com.ninjagoldfinch.nz.ninja_utils.config.DebugCategory
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture

/**
 * Client for Hypixel public API endpoints that don't require an API key.
 * All requests are made asynchronously to avoid blocking the game thread.
 */
object HypixelApiClient {
    private val logger = ModLogger.category("HypixelAPI")
    private val gson = Gson()
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    private const val BASE_URL = "https://api.hypixel.net"
    private const val MAX_RETRIES = 2
    private const val RETRY_BASE_MS = 2000L

    // Caches
    val electionCache = ApiCache<ElectionResponse>(3600_000L) // 1 hour
    val bazaarCache = ApiCache<BazaarResponse>(ApiCategory.bazaarCacheTtl * 1000L)

    // Track errors for warning system
    var consecutiveErrors: Int = 0
        private set

    fun fetchElection(): CompletableFuture<ElectionResponse?> {
        electionCache.get()?.let { return CompletableFuture.completedFuture(it) }

        return requestAsync("$BASE_URL/v2/resources/skyblock/election", ElectionResponse::class.java)
            .thenApply { response ->
                response?.let { electionCache.put(it) }
                response
            }
    }

    fun fetchBazaar(): CompletableFuture<BazaarResponse?> {
        bazaarCache.get()?.let { return CompletableFuture.completedFuture(it) }

        return requestAsync("$BASE_URL/v2/skyblock/bazaar", BazaarResponse::class.java)
            .thenApply { response ->
                response?.let { bazaarCache.put(it) }
                response
            }
    }

    private fun <T> requestAsync(url: String, responseType: Class<T>, attempt: Int = 0): CompletableFuture<T?> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build()

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply { response ->
                if (response.statusCode() == 200) {
                    consecutiveErrors = 0
                    val body = response.body()
                    if (DebugCategory.logApiResponses) {
                        logger.debug("Response from $url: ${body.take(200)}...")
                    }
                    try {
                        gson.fromJson(body, responseType)
                    } catch (e: Exception) {
                        logger.error("Failed to parse response from $url", e)
                        null
                    }
                } else {
                    consecutiveErrors++
                    logger.warn("HTTP ${response.statusCode()} from $url (attempt ${attempt + 1})")
                    null
                }
            }
            .exceptionally { e ->
                consecutiveErrors++
                logger.error("Request failed: $url (attempt ${attempt + 1})", e)
                null
            }
            .thenCompose { result ->
                if (result == null && attempt < MAX_RETRIES) {
                    val delayMs = RETRY_BASE_MS * (1L shl attempt)
                    CompletableFuture.supplyAsync(
                        { null as T? },
                        CompletableFuture.delayedExecutor(delayMs, java.util.concurrent.TimeUnit.MILLISECONDS)
                    ).thenCompose {
                        logger.debug("Retrying $url (attempt ${attempt + 2}/${ MAX_RETRIES + 1})")
                        requestAsync(url, responseType, attempt + 1)
                    }
                } else {
                    CompletableFuture.completedFuture(result)
                }
            }
    }

    fun invalidateAll() {
        electionCache.invalidate()
        bazaarCache.invalidate()
        consecutiveErrors = 0
    }
}
