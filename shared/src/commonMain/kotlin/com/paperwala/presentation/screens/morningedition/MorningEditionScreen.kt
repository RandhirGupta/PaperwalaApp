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
package com.paperwala.presentation.screens.morningedition

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.paperwala.domain.model.Article
import com.paperwala.domain.model.Edition
import com.paperwala.domain.model.Section
import com.paperwala.presentation.animation.EditionLoadingAnimation
import com.paperwala.presentation.animation.articleEntrance
import com.paperwala.presentation.animation.heroEntrance
import com.paperwala.presentation.animation.mastheadEntrance
import com.paperwala.presentation.animation.newspaperUnfold
import com.paperwala.presentation.components.CompactArticleCard
import com.paperwala.presentation.components.NewspaperDivider
import com.paperwala.presentation.components.OfflineBanner
import com.paperwala.presentation.components.SectionHeader
import com.paperwala.presentation.components.StreakBadge
import com.paperwala.presentation.screens.articledetail.ArticleDetailScreen
import com.paperwala.presentation.screens.bookmarks.BookmarksScreen
import com.paperwala.presentation.screens.settings.SettingsScreen
import com.paperwala.presentation.screens.streaks.StreakDashboardScreen
import com.paperwala.presentation.theme.PaperwalaColors
import com.paperwala.util.ReadTimeCalculator
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class MorningEditionScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<MorningEditionViewModel>()
        val state by viewModel.state.collectAsState()

        Column(modifier = Modifier.fillMaxSize()) {
            OfflineBanner(isOffline = state.isOffline)

            PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    state.isLoading && state.edition == null -> LoadingState()
                    state.error != null && state.edition == null -> ErrorState(state.error!!)
                    state.edition != null -> EditionContent(
                        edition = state.edition!!,
                        currentStreak = state.currentStreak,
                        onArticleClick = { article ->
                            viewModel.markArticleAsRead(article.id)
                            navigator.push(ArticleDetailScreen(article))
                        },
                        onStreakClick = {
                            navigator.push(StreakDashboardScreen())
                        },
                        onBookmarksClick = {
                            navigator.push(BookmarksScreen())
                        },
                        onSettingsClick = {
                            navigator.push(SettingsScreen())
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            EditionLoadingAnimation(
                modifier = Modifier.size(160.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Preparing your morning edition...",
                style = MaterialTheme.typography.bodyLarge,
                color = PaperwalaColors.InkLightGray
            )
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = PaperwalaColors.InkLightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp)
        )
    }
}

@Composable
private fun EditionContent(
    edition: Edition,
    currentStreak: Int,
    onArticleClick: (Article) -> Unit,
    onStreakClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val scrollState = rememberLazyListState()
    val visibleSections = remember { mutableStateListOf<Int>() }

    // Track which sections have scrolled into view
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                visibleItems.forEach { item ->
                    // Section items start after masthead (1) + above-fold (1) + divider (1) = index 3+
                    val sectionIndex = item.index - 3
                    if (sectionIndex >= 0 && sectionIndex !in visibleSections) {
                        visibleSections.add(sectionIndex)
                    }
                }
            }
    }

    val aboveTheFold = edition.sections.firstOrNull()
    val remainingSections = if (edition.sections.size > 1) edition.sections.drop(1) else emptyList()

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Masthead with slide-down entrance
        item {
            NewspaperMasthead(
                edition = edition,
                currentStreak = currentStreak,
                onStreakClick = onStreakClick,
                onBookmarksClick = onBookmarksClick,
                onSettingsClick = onSettingsClick,
                modifier = Modifier.mastheadEntrance()
            )
        }

        // Above the Fold section with hero entrance
        if (aboveTheFold != null) {
            item {
                AboveTheFoldSection(
                    section = aboveTheFold,
                    onArticleClick = onArticleClick,
                    modifier = Modifier.heroEntrance(delayMs = 300)
                )
            }
        }

        // Divider before sections
        item { NewspaperDivider() }

        // Remaining sections with unfold animation
        itemsIndexed(remainingSections) { index, section ->
            val hasAppeared = index in visibleSections

            SectionWithUnfold(
                section = section,
                hasAppeared = hasAppeared,
                index = index,
                onArticleClick = onArticleClick
            )
        }

        // Edition footer
        item {
            EditionFooter(edition)
        }
    }
}

@Composable
private fun NewspaperMasthead(
    edition: Edition,
    currentStreak: Int,
    onStreakClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val greeting = when (now.hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Action row: streak badge | bookmarks | settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StreakBadge(
                currentStreak = currentStreak,
                onClick = onStreakClick
            )
            Row {
                IconButton(onClick = onBookmarksClick) {
                    Icon(
                        Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmarks",
                        tint = PaperwalaColors.InkGray
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = PaperwalaColors.InkGray
                    )
                }
            }
        }

        Text(
            text = "PAPERWALA",
            style = MaterialTheme.typography.displayLarge,
            color = PaperwalaColors.MastheadRed,
            textAlign = TextAlign.Center
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 6.dp),
            thickness = 2.dp,
            color = PaperwalaColors.MastheadRed
        )

        Text(
            text = "${edition.date}",
            style = MaterialTheme.typography.labelLarge,
            color = PaperwalaColors.InkLightGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "~${edition.totalReadTimeMinutes} min read",
                style = MaterialTheme.typography.labelLarge,
                color = PaperwalaColors.InkLightGray
            )
            Text(
                text = "  \u00b7  ",
                style = MaterialTheme.typography.labelLarge,
                color = PaperwalaColors.InkLightGray
            )
            Text(
                text = "${edition.articleCount} stories",
                style = MaterialTheme.typography.labelLarge,
                color = PaperwalaColors.InkLightGray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider(
            thickness = 1.dp,
            color = PaperwalaColors.DividerColor
        )
    }
}

@Composable
private fun AboveTheFoldSection(
    section: Section,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier
) {
    val articles = section.articles
    if (articles.isEmpty()) return

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        // Hero article (first one)
        val hero = articles.first()
        HeroArticle(article = hero, onClick = { onArticleClick(hero) })

        if (articles.size > 1) {
            Spacer(modifier = Modifier.height(12.dp))

            // Two-column sub-articles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                articles.drop(1).take(2).forEach { article ->
                    SubArticle(
                        article = article,
                        onClick = { onArticleClick(article) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroArticle(article: Article, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(0.dp)
    ) {
        if (article.imageUrl != null) {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (article.summary.isNotBlank()) {
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    fontStyle = FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = article.sourceName,
                    style = MaterialTheme.typography.labelLarge,
                    color = PaperwalaColors.InkLightGray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "  \u00b7  ${ReadTimeCalculator.formatReadTime(article.readTimeMinutes)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = PaperwalaColors.InkLightGray
                )
            }
        }
    }
}

@Composable
private fun SubArticle(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp)
    ) {
        if (article.imageUrl != null) {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (article.summary.isNotBlank()) {
            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Text(
            text = "${article.sourceName} \u00b7 ${ReadTimeCalculator.formatReadTime(article.readTimeMinutes)}",
            style = MaterialTheme.typography.labelSmall,
            color = PaperwalaColors.InkLightGray
        )
    }
}

@Composable
private fun SectionWithUnfold(
    section: Section,
    hasAppeared: Boolean,
    index: Int,
    onArticleClick: (Article) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .newspaperUnfold(visible = hasAppeared, index = index)
            .animateContentSize()
    ) {
        SectionHeader(
            title = section.displayName,
            categoryName = section.category.name
        )

        // Horizontal scrolling cards with staggered entrance
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(section.articles) { articleIndex, article ->
                CompactArticleCard(
                    article = article,
                    onClick = { onArticleClick(article) },
                    modifier = Modifier.articleEntrance(
                        visible = hasAppeared,
                        index = articleIndex
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        NewspaperDivider()
    }
}

@Composable
private fun EditionFooter(edition: Edition) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(bottom = 16.dp),
            thickness = 2.dp,
            color = PaperwalaColors.DividerColor
        )

        Text(
            text = "End of today\u2019s edition",
            style = MaterialTheme.typography.titleMedium,
            color = PaperwalaColors.InkLightGray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${edition.articleCount} stories \u00b7 ${edition.totalReadTimeMinutes} min read",
            style = MaterialTheme.typography.labelLarge,
            color = PaperwalaColors.InkLightGray
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}
