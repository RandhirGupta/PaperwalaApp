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
package com.paperwala.presentation.screens.articledetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.paperwala.domain.model.Article
import com.paperwala.presentation.theme.PaperwalaColors
import com.paperwala.presentation.theme.categoryColor
import com.paperwala.util.ReadTimeCalculator

class ArticleDetailScreen(
    private val article: Article
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = article.sourceName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Share */ }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Hero image
                if (article.imageUrl != null) {
                    AsyncImage(
                        model = article.imageUrl,
                        contentDescription = article.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Category badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = categoryColor(article.category.name).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = article.category.displayName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor(article.category.name),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Meta row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = article.sourceName,
                            style = MaterialTheme.typography.labelLarge,
                            color = PaperwalaColors.InkGray,
                            fontWeight = FontWeight.Bold
                        )
                        if (article.author != null) {
                            Text(
                                text = " \u00b7 ${article.author}",
                                style = MaterialTheme.typography.labelMedium,
                                color = PaperwalaColors.InkLightGray
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = ReadTimeCalculator.formatReadTime(article.readTimeMinutes),
                            style = MaterialTheme.typography.labelMedium,
                            color = PaperwalaColors.InkLightGray
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = PaperwalaColors.DividerColor
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // AI Summary as a pull-quote
                    if (article.summary.isNotBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = PaperwalaColors.PaperCream
                        ) {
                            Row(modifier = Modifier.padding(16.dp)) {
                                // Accent bar
                                Spacer(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(60.dp)
                                        .background(PaperwalaColors.MastheadRed)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "AI SUMMARY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PaperwalaColors.InkLightGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = article.summary,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = PaperwalaColors.InkDarkGray,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Full content
                    val content = article.fullContent ?: article.summary
                    if (content.isNotBlank()) {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Source link
                    if (article.sourceUrl.isNotBlank()) {
                        Text(
                            text = "Read full article at ${article.sourceName}",
                            style = MaterialTheme.typography.labelLarge,
                            color = PaperwalaColors.LinkBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}
