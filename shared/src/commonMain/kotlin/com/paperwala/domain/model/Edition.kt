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
package com.paperwala.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class Edition(
    val id: String,
    val date: LocalDate,
    val generatedAt: Instant,
    val headline: String,
    val sections: List<Section>,
    val totalReadTimeMinutes: Int,
    val articleCount: Int,
    val isRead: Boolean = false
)
