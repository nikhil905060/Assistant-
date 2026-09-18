package com.astute.ai.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.addJsonObject

class GeminiClient(private val apiKey: String) {
    private val client = OkHttpClient()
    private val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

    suspend fun analyzeCommand(prompt: String): String = withContext(Dispatchers.IO) {
        val jsonPayload = buildJsonObject {
            putJsonArray("contents") {
                addJsonObject {
                    putJsonArray("parts") {
                        addJsonObject {
                            put("text", "Interpret command: $prompt. Return intent in JSON format.")
                        }
                    }
                }
            }
        }.toString()

        val req = Request.Builder()
            .url(url)
            .post(jsonPayload.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(req).execute().use { res ->
            res.body?.string() ?: "{}"
        }
    }
}
