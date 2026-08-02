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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Icon
import coil.compose.AsyncImage
import com.lianshan.lslife.core.model.Post

@Composable
fun PostListCard(post: Post, onClick: () -> Unit = {}, onAddCartClick: () -> Unit = {}) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = scheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp, // Very clean, no shadow
    ) {
        Column {
            val cover = post.images.firstOrNull()
            if (cover != null) {
                AsyncImage(
                    model = cover,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp) // 降低极限高度，让图片显得不那么庞大
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(androidx.compose.ui.graphics.Color(0xFFF7F7F7)), 
                    contentScale = ContentScale.FillWidth, // 宽度填满，高度按比例自适应（实现不对称瀑布流的关键）
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp) // Default height if no image
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(scheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(32.dp)) 
                }
            }
            
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
                Text(
                    post.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 12.sp, // 字体略微调小
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Price tag and Add Button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (post.price != null && post.price > 0) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Text("¥", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color(0xFFE1251B), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(bottom = 1.dp, end = 1.dp))
                            Text(
                                "%.2f".format(post.price),
                                style = MaterialTheme.typography.titleMedium,
                                color = androidx.compose.ui.graphics.Color(0xFFE1251B),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        Text(
                            "面议",
                            style = MaterialTheme.typography.labelMedium,
                            color = androidx.compose.ui.graphics.Color(0xFFE1251B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(androidx.compose.ui.graphics.Color(0xFFE1251B))
                            .clickable(onClick = onAddCartClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "购买", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                
                Spacer(Modifier.height(6.dp))
                
                // Publisher
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (post.publisherType == "MERCHANT" && post.merchant != null) {
                        Text("${post.merchant.name} 自营", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    } else {
                        val nickname = post.user?.nickname ?: "连山用户"
                        Text(nickname, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
