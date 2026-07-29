package com.lianshan.lslife.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@Composable
fun CategoryIconView(
    iconUrl: String?,
    iconName: String?,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    contentDescription: String? = null
) {
    val resolvedUrl = androidx.compose.runtime.remember(iconUrl) {
        when {
            iconUrl.isNullOrBlank() -> null
            iconUrl.startsWith("http://") || iconUrl.startsWith("https://") -> iconUrl
            iconUrl.startsWith("/") -> {
                val baseUrl = com.lianshan.lslife.BuildConfig.API_BASE_URL
                    .removeSuffix("/api/")
                    .removeSuffix("/api")
                    .removeSuffix("/")
                "$baseUrl$iconUrl"
            }
            else -> iconUrl
        }
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        if (!resolvedUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(resolvedUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                colorFilter = null, // Ensure NO monochrome tint is ever applied to 3D flat colored icons
                modifier = Modifier.size(size),
                loading = {
                    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(size * 0.6f),
                            strokeWidth = 2.dp,
                            color = tint
                        )
                    }
                },
                error = {
                    Icon(
                        imageVector = resolveVectorIcon(iconName),
                        contentDescription = contentDescription,
                        tint = tint,
                        modifier = Modifier.size(size)
                    )
                }
            )
        } else if (!iconName.isNullOrBlank() && isEmoji(iconName)) {
            Text(
                text = iconName,
                fontSize = (size.value * 0.75f).sp,
                fontWeight = FontWeight.Normal
            )
        } else {
            Icon(
                imageVector = resolveVectorIcon(iconName),
                contentDescription = contentDescription ?: iconName,
                tint = tint,
                modifier = Modifier.size(size)
            )
        }
    }
}

private fun isEmoji(str: String): Boolean {
    val trimmed = str.trim()
    if (trimmed.isEmpty()) return false
    // If it is a short string (1-4 characters) and contains no standard ASCII letters, treat as Emoji
    if (trimmed.length <= 4 && trimmed.none { it in 'a'..'z' || it in 'A'..'Z' }) {
        return true
    }
    return false
}

private fun resolveVectorIcon(name: String?): ImageVector {
    val key = name?.trim()?.lowercase() ?: ""
    return when {
        key == "all" || key == "全部" -> Icons.Filled.GridView
        key.contains("shopping-bag") || key.contains("second_hand") || key.contains("idle") || key.contains("闲置") -> Icons.Filled.ShoppingBag
        key.contains("briefcase") || key.contains("job") || key.contains("work") || key.contains("招聘") || key.contains("求职") -> Icons.Filled.Work
        key.contains("timer") || key.contains("time") || key.contains("part_time") || key.contains("clock") || key.contains("schedule") || key.contains("兼职") -> Icons.Filled.Schedule
        key.contains("apartment") || key.contains("building") || key.contains("secondhand_house") || key.contains("resale") || key.contains("二手房") -> Icons.Filled.Apartment
        key.contains("store") || key.contains("shop") || key.contains("shop_rent") || key.contains("commercial") || key.contains("旺铺") -> Icons.Filled.Storefront
        key.contains("home") || key.contains("house") || key.contains("housing") || key.contains("房屋") || key.contains("租售") -> Icons.Filled.Home
        key.contains("wrench") || key.contains("service") || key.contains("repair") || key.contains("maintenance") || key.contains("维修") || key.contains("水电") -> Icons.Filled.Build
        key.contains("housekeeping") || key.contains("clean") || key.contains("家政") || key.contains("保洁") -> Icons.Filled.CleaningServices
        key.contains("moving") || key.contains("shipping") || key.contains("truck") || key.contains("car") || key.contains("car_rental") || key.contains("租车") || key.contains("顺风车") -> Icons.Filled.LocalShipping
        key.contains("apple") || key.contains("veggies") || key.contains("fruit") || key.contains("food") || key.contains("produce") || key.contains("生鲜") || key.contains("水果") || key.contains("蔬菜") -> Icons.Filled.ShoppingBasket
        key.contains("phone") || key.contains("electronics") -> Icons.Filled.Smartphone
        key.contains("laptop") || key.contains("computer") -> Icons.Filled.Laptop
        key.contains("dress") || key.contains("clothing") || key.contains("shoes") -> Icons.Filled.Checkroom
        key.contains("book") || key.contains("novel") -> Icons.AutoMirrored.Filled.MenuBook
        key.contains("sparkles") || key.contains("awesome") || key.contains("ai") -> Icons.Filled.AutoAwesome
        else -> Icons.Filled.Folder
    }
}
