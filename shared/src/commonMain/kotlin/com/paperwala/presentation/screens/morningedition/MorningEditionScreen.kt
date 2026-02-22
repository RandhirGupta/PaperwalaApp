package com.paperwala.presentation.screens.morningedition

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
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
import com.paperwala.presentation.components.CompactArticleCard
import com.paperwala.presentation.components.NewspaperDivider
import com.paperwala.presentation.components.SectionHeader
import com.paperwala.presentation.screens.articledetail.ArticleDetailScreen
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
                    onArticleClick = { article ->
                        viewModel.markArticleAsRead(article.id)
                        navigator.push(ArticleDetailScreen(article))
                    }
                )
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
            CircularProgressIndicator(color = PaperwalaColors.MastheadRed)
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
    onArticleClick: (Article) -> Unit
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
        // Masthead
        item {
            NewspaperMasthead(edition)
        }

        // Above the Fold section
        if (aboveTheFold != null) {
            item {
                AboveTheFoldSection(
                    section = aboveTheFold,
                    onArticleClick = onArticleClick
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
private fun NewspaperMasthead(edition: Edition) {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val greeting = when (now.hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Masthead title
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

        // Greeting and meta
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
    onArticleClick: (Article) -> Unit
) {
    val articles = section.articles
    if (articles.isEmpty()) return

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
    onArticleClick: (Article) -> Unit
) {
    // 3D unfold animation
    val rotationX by animateFloatAsState(
        targetValue = if (hasAppeared) 0f else -90f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessLow
        )
    )
    val alpha by animateFloatAsState(
        targetValue = if (hasAppeared) 1f else 0f,
        animationSpec = tween(durationMillis = 400)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.rotationX = rotationX
                this.alpha = alpha
                this.cameraDistance = 12f * density
                this.transformOrigin = TransformOrigin(0.5f, 0f)
            }
            .animateContentSize()
    ) {
        SectionHeader(
            title = section.displayName,
            categoryName = section.category.name
        )

        // Horizontal scrolling cards for this section
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(section.articles) { article ->
                CompactArticleCard(
                    article = article,
                    onClick = { onArticleClick(article) }
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
            text = "End of today's edition",
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
