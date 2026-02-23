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

actual class LocalLlmEngine {

    private var modelLoaded = false

    actual suspend fun loadModel(modelPath: String) = withContext(Dispatchers.IO) {
        // TODO: Integrate llama.cpp via cinterop with llama.h
        // This requires building llama.cpp as a static library (.a) for iOS
        // and creating a .def file for Kotlin/Native C interop.
        //
        // Steps to enable:
        // 1. Build llama.cpp: cmake -DLLAMA_METAL=ON -DBUILD_SHARED_LIBS=OFF
        // 2. Create shared/src/nativeInterop/cinterop/llama.def
        // 3. Add cinterop config to build.gradle.kts iosMain targets
        throw UnsupportedOperationException(
            "iOS local LLM not yet configured. " +
                "Build llama.cpp for iOS and add cinterop definition."
        )
    }

    actual suspend fun generate(prompt: String, maxTokens: Int): String =
        withContext(Dispatchers.IO) {
            if (!modelLoaded) {
                throw IllegalStateException("Model not loaded")
            }
            // Placeholder for llama.cpp cinterop call
            throw UnsupportedOperationException("iOS local LLM not yet configured")
        }

    actual fun isModelLoaded(): Boolean = modelLoaded

    actual fun unloadModel() {
        modelLoaded = false
    }
}
