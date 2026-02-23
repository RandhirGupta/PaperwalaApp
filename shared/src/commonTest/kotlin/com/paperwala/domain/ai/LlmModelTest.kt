package com.paperwala.domain.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class LlmModelTest {

    @Test
    fun allModelsHaveUniqueFileNames() {
        val fileNames = LlmModel.entries.map { it.fileName }
        assertEquals(fileNames.size, fileNames.toSet().size, "Model file names must be unique")
    }

    @Test
    fun allModelsHaveNonEmptyDisplayNames() {
        LlmModel.entries.forEach { model ->
            assertTrue(model.displayName.isNotBlank(), "${model.name} should have a non-empty displayName")
        }
    }

    @Test
    fun allModelsHaveValidDownloadUrls() {
        LlmModel.entries.forEach { model ->
            assertTrue(
                model.downloadUrl.startsWith("https://"),
                "${model.name} downloadUrl should start with https://"
            )
            assertTrue(
                model.downloadUrl.contains(".gguf"),
                "${model.name} downloadUrl should point to a .gguf file"
            )
        }
    }

    @Test
    fun allModelsHavePositiveSizeBytes() {
        LlmModel.entries.forEach { model ->
            assertTrue(model.sizeBytes > 0, "${model.name} sizeBytes should be positive")
        }
    }

    @Test
    fun allModelsHavePositiveRamRequirement() {
        LlmModel.entries.forEach { model ->
            assertTrue(model.requiredRamMb > 0, "${model.name} requiredRamMb should be positive")
        }
    }

    @Test
    fun fromStringReturnsCorrectModelForExactMatch() {
        LlmModel.entries.forEach { model ->
            assertEquals(model, LlmModel.fromString(model.name))
        }
    }

    @Test
    fun fromStringIsCaseInsensitive() {
        assertEquals(LlmModel.PHI_3_MINI, LlmModel.fromString("phi_3_mini"))
        assertEquals(LlmModel.LLAMA_3_2_3B, LlmModel.fromString("llama_3_2_3b"))
        assertEquals(LlmModel.GEMMA_2_2B, LlmModel.fromString("Gemma_2_2B"))
    }

    @Test
    fun fromStringDefaultsToPhiForUnknownValue() {
        assertEquals(LlmModel.PHI_3_MINI, LlmModel.fromString("unknown_model"))
        assertEquals(LlmModel.PHI_3_MINI, LlmModel.fromString(""))
    }

    @Test
    fun fourModelsExist() {
        assertEquals(4, LlmModel.entries.size)
    }

    @Test
    fun modelsAreOrderedBySize() {
        // PHI_3_MINI is largest, LLAMA_3_2_1B is smallest
        assertTrue(LlmModel.PHI_3_MINI.sizeBytes > LlmModel.LLAMA_3_2_1B.sizeBytes)
        assertTrue(LlmModel.LLAMA_3_2_3B.sizeBytes > LlmModel.LLAMA_3_2_1B.sizeBytes)
    }

    @Test
    fun fileNamesEndWithGguf() {
        LlmModel.entries.forEach { model ->
            assertTrue(
                model.fileName.endsWith(".gguf"),
                "${model.name} fileName should end with .gguf"
            )
        }
    }
}
