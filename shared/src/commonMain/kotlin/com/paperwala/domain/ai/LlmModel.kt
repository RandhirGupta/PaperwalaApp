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

enum class LlmModel(
    val displayName: String,
    val fileName: String,
    val downloadUrl: String,
    val sizeDescription: String,
    val sizeBytes: Long,
    val speedDescription: String,
    val qualityDescription: String,
    val requiredRamMb: Int
) {
    PHI_3_MINI(
        displayName = "Phi-3-mini",
        fileName = "phi-3-mini-4k-instruct-q4_k_m.gguf",
        downloadUrl = "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q4.gguf",
        sizeDescription = "2.3 GB",
        sizeBytes = 2_400_000_000L,
        speedDescription = "~8 tok/s",
        qualityDescription = "Best quality",
        requiredRamMb = 3500
    ),

    LLAMA_3_2_3B(
        displayName = "Llama 3.2 3B",
        fileName = "llama-3.2-3b-instruct-q4_k_m.gguf",
        downloadUrl = "https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF/resolve/main/Llama-3.2-3B-Instruct-Q4_K_M.gguf",
        sizeDescription = "1.9 GB",
        sizeBytes = 2_000_000_000L,
        speedDescription = "~12 tok/s",
        qualityDescription = "Fast + good quality",
        requiredRamMb = 3000
    ),

    LLAMA_3_2_1B(
        displayName = "Llama 3.2 1B",
        fileName = "llama-3.2-1b-instruct-q4_k_m.gguf",
        downloadUrl = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf",
        sizeDescription = "0.8 GB",
        sizeBytes = 800_000_000L,
        speedDescription = "~30 tok/s",
        qualityDescription = "Fastest, basic quality",
        requiredRamMb = 1500
    ),

    GEMMA_2_2B(
        displayName = "Gemma 2 2B",
        fileName = "gemma-2-2b-it-q4_k_m.gguf",
        downloadUrl = "https://huggingface.co/bartowski/gemma-2-2b-it-GGUF/resolve/main/gemma-2-2b-it-Q4_K_M.gguf",
        sizeDescription = "1.5 GB",
        sizeBytes = 1_600_000_000L,
        speedDescription = "~16 tok/s",
        qualityDescription = "Balanced",
        requiredRamMb = 2500
    );

    companion object {
        fun fromString(value: String): LlmModel {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: PHI_3_MINI
        }
    }
}
