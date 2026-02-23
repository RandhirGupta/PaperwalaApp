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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSUserDomainMask

actual class ModelManager {

    private val documentsDir: String
        get() {
            val paths = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory, NSUserDomainMask, true
            )
            return (paths.firstOrNull() as? String) ?: ""
        }

    private val modelsDir: String
        get() = "$documentsDir/models"

    private fun modelPath(model: LlmModel): String = "$modelsDir/${model.fileName}"

    actual suspend fun downloadModel(model: LlmModel, onProgress: (Float) -> Unit): Boolean =
        withContext(Dispatchers.IO) {
            val fileManager = NSFileManager.defaultManager
            if (!fileManager.fileExistsAtPath(modelsDir)) {
                fileManager.createDirectoryAtPath(
                    modelsDir,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null
                )
            }

            // TODO: Implement download with NSURLSession and progress tracking
            throw UnsupportedOperationException(
                "iOS model download not yet implemented. " +
                    "Requires NSURLSession background download configuration."
            )
        }

    actual fun getModelPath(model: LlmModel): String? {
        val path = modelPath(model)
        return if (NSFileManager.defaultManager.fileExistsAtPath(path)) path else null
    }

    actual fun isModelDownloaded(model: LlmModel): Boolean {
        return NSFileManager.defaultManager.fileExistsAtPath(modelPath(model))
    }

    actual fun deleteModel(model: LlmModel) {
        NSFileManager.defaultManager.removeItemAtPath(modelPath(model), error = null)
    }

    actual fun getModelSizeBytes(model: LlmModel): Long {
        val fileManager = NSFileManager.defaultManager
        val path = modelPath(model)
        val attrs = fileManager.attributesOfItemAtPath(path, error = null) ?: return 0L
        return (attrs["NSFileSize"] as? Long) ?: 0L
    }
}
