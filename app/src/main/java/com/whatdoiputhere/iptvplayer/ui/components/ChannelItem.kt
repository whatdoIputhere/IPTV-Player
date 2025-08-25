package com.whatdoiputhere.iptvplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.whatdoiputhere.iptvplayer.model.Channel

@Composable
fun channelItem(
    channel: Channel,
    onChannelClick: (Channel) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val outerPaddingH = if (compact) 8.dp else 14.dp
    val outerPaddingV = if (compact) 6.dp else 3.dp
    val innerPadding = if (compact) 12.dp else 14.dp
    val imageSize = if (compact) 40.dp else 44.dp

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onChannelClick(channel) }
                .padding(horizontal = outerPaddingH, vertical = outerPaddingV),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(innerPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (channel.logo.isNotEmpty()) {
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(channel.logo)
                            .size(96, 54)
                            .crossfade(false)
                            .build(),
                    contentDescription = "Channel logo",
                    modifier =
                        Modifier
                            .size(imageSize)
                            .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Surface(
                    modifier =
                        Modifier
                            .size(imageSize)
                            .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = channel.name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (channel.group.isNotEmpty()) {
                    Text(
                        text = channel.group,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (channel.country.isNotEmpty() || channel.language.isNotEmpty()) {
                    Text(
                        text =
                            listOfNotNull(
                                channel.country.takeIf { it.isNotEmpty() },
                                channel.language.takeIf { it.isNotEmpty() },
                            ).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
