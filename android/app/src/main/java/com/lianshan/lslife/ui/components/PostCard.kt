package com.lianshan.lslife.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import coil.compose.AsyncImage
import com.lianshan.lslife.core.model.Post

@Composable
fun PostListCard(post: Post, onClick: () -> Unit = {}) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = scheme.surface,
        tonalElevation = 1.dp,
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            val cover = post.images.firstOrNull()
            if (cover != null) {
                AsyncImage(
                    model = cover,
                    contentDescription = null,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(12.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(scheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null, tint = scheme.onSurfaceVariant) 
                }
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f).height(110.dp)) {
                Text(post.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.weight(1f))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (post.publisherType == "MERCHANT" && post.merchant != null) {
                        Text(post.merchant.name, style = MaterialTheme.typography.labelSmall, color = scheme.primary, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "认证商家",
                            tint = androidx.compose.ui.graphics.Color(0xFFFBC02D),
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        val nickname = post.user?.nickname ?: "连山用户"
                        Text(nickname, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    if (post.price != null && post.price > 0) {
                        Text("¥", style = MaterialTheme.typography.labelMedium, color = scheme.error, fontWeight = FontWeight.Bold)
                        Text("${post.price}", style = MaterialTheme.typography.titleLarge, color = scheme.error, fontWeight = FontWeight.ExtraBold)
                    } else {
                        Text("面议", style = MaterialTheme.typography.titleMedium, color = scheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
