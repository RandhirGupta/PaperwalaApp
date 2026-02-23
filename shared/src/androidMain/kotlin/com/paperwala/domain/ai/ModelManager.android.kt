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

    private fun modelFile(model: LlmModel): File = File(modelsDir, model.fileName)

    actual suspend fun downloadModel(model: LlmModel, onProgress: (Float) -> Unit): Boolean =
        withContext(Dispatchers.IO) {
            val freeSpace = modelsDir.usableSpace
            val requiredSpace = model.sizeBytes + 500_000_000L // model size + 500MB buffer
            if (freeSpace < requiredSpace) {
                throw IllegalStateException(
                    "Insufficient storage. Need ${requiredSpace / (1024 * 1024)}MB, " +
                        "available: ${freeSpace / (1024 * 1024)}MB"
                )
            }

            val file = modelFile(model)
            val tempFile = File(modelsDir, "${model.fileName}.tmp")
            try {
                val client = HttpClient()
                client.prepareGet(model.downloadUrl).execute { response ->
                    val totalBytes = response.contentLength() ?: model.sizeBytes
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

                tempFile.renameTo(file)
                true
            } catch (e: Exception) {
                tempFile.delete()
                throw e
            }
        }

    actual fun getModelPath(model: LlmModel): String? {
        val file = modelFile(model)
        return if (file.exists() && file.length() > 0) file.absolutePath else null
    }

    actual fun isModelDownloaded(model: LlmModel): Boolean {
        val file = modelFile(model)
        return file.exists() && file.length() > MIN_VALID_MODEL_SIZE
    }

    actual fun deleteModel(model: LlmModel) {
        modelFile(model).delete()
    }

    actual fun getModelSizeBytes(model: LlmModel): Long {
        val file = modelFile(model)
        return if (file.exists()) file.length() else 0L
    }

    companion object {
        private const val MIN_VALID_MODEL_SIZE = 100_000_000L
    }
}
