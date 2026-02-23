/*
 * Copyright 2026 Randhir Gupta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paperwala.domain.ai

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class ModelManager(private val context: Context) {

    private val modelsDir: File
        get() = File(context.filesDir, "models").also { it.mkdirs() }

    private val modelFile: File
        get() = File(modelsDir, MODEL_FILENAME)

    actual suspend fun downloadModel(onProgress: (Float) -> Unit): Boolean =
        withContext(Dispatchers.IO) {
            // Check available storage (need ~3GB free)
            val freeSpace = modelsDir.usableSpace
            if (freeSpace < REQUIRED_SPACE_BYTES) {
                throw IllegalStateException(
                    "Insufficient storage. Need ${REQUIRED_SPACE_BYTES / (1024 * 1024)}MB, " +
                        "available: ${freeSpace / (1024 * 1024)}MB"
                )
            }

            val tempFile = File(modelsDir, "$MODEL_FILENAME.tmp")
            try {
                val client = HttpClient()
                client.prepareGet(MODEL_DOWNLOAD_URL).execute { response ->
                    val totalBytes = response.contentLength() ?: EXPECTED_MODEL_SIZE
                    var downloadedBytes = 0L
                    val channel = response.bodyAsChannel()
                    val buffer = ByteArray(8192)

                    tempFile.outputStream().use { output ->
                        while (!channel.isClosedForRead) {
                            val bytesRead = channel.readAvailable(buffer)
                            if (bytesRead > 0) {
                                output.write(buffer, 0, bytesRead)
                                downloadedBytes += bytesRead
                                onProgress(downloadedBytes.toFloat() / totalBytes)
                            }
                        }
                    }
                }
                client.close()

                // Rename temp to final
                tempFile.renameTo(modelFile)
                true
            } catch (e: Exception) {
                tempFile.delete()
                throw e
            }
        }

    actual fun getModelPath(): String? {
        return if (modelFile.exists() && modelFile.length() > 0) {
            modelFile.absolutePath
        } else {
            null
        }
    }

    actual fun isModelDownloaded(): Boolean {
        return modelFile.exists() && modelFile.length() > MIN_VALID_MODEL_SIZE
    }

    actual fun deleteModel() {
        modelFile.delete()
    }

    actual fun getModelSizeBytes(): Long {
        return if (modelFile.exists()) modelFile.length() else 0L
    }

    companion object {
        private const val MODEL_FILENAME = "phi-3-mini-4k-instruct-q4_k_m.gguf"
        private const val MODEL_DOWNLOAD_URL =
            "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q4.gguf"
        private const val EXPECTED_MODEL_SIZE = 2_400_000_000L // ~2.3 GB
        private const val REQUIRED_SPACE_BYTES = 3_000_000_000L // 3 GB free required
        private const val MIN_VALID_MODEL_SIZE = 100_000_000L // 100 MB minimum to be valid
    }
}
