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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class LocalLlmEngine(private val context: Context) {

    private var nativeHandle: Long = 0L
    private var modelLoaded = false

    actual suspend fun loadModel(modelPath: String) = withContext(Dispatchers.Default) {
        ensureNativeLibrary()
        nativeHandle = nativeLoadModel(modelPath)
        if (nativeHandle == 0L) {
            throw IllegalStateException("Failed to load model from $modelPath")
        }
        modelLoaded = true
    }

    actual suspend fun generate(prompt: String, maxTokens: Int): String =
        withContext(Dispatchers.Default) {
            if (!modelLoaded || nativeHandle == 0L) {
                throw IllegalStateException("Model not loaded")
            }
            nativeGenerate(nativeHandle, prompt, maxTokens)
        }

    actual fun isModelLoaded(): Boolean = modelLoaded && nativeHandle != 0L

    actual fun unloadModel() {
        if (nativeHandle != 0L) {
            nativeFreeModel(nativeHandle)
            nativeHandle = 0L
            modelLoaded = false
        }
    }

    private fun ensureNativeLibrary() {
        if (!nativeLibraryLoaded) {
            try {
                System.loadLibrary("llama-android")
                nativeLibraryLoaded = true
            } catch (e: UnsatisfiedLinkError) {
                throw IllegalStateException(
                    "llama-android native library not found. " +
                        "Build llama.cpp for Android and place libllama-android.so in jniLibs/",
                    e
                )
            }
        }
    }

    private external fun nativeLoadModel(modelPath: String): Long
    private external fun nativeGenerate(handle: Long, prompt: String, maxTokens: Int): String
    private external fun nativeFreeModel(handle: Long)

    companion object {
        private var nativeLibraryLoaded = false
    }
}
