package com.paperwala.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paperwala.domain.model.TopicCategory
import com.paperwala.presentation.theme.PaperwalaColors
import com.paperwala.presentation.theme.categoryColor

@Composable
fun TopicChip(
    topic: TopicCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) categoryColor(topic.name).copy(alpha = 0.15f)
        else MaterialTheme.colorScheme.surfaceVariant
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) categoryColor(topic.name)
        else PaperwalaColors.DividerColor
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) categoryColor(topic.name)
        else MaterialTheme.colorScheme.onSurfaceVariant
    )

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = topic.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = textColor
            )
        }
    }
}
