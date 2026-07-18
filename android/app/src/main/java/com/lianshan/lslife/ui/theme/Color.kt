package com.lianshan.lslife.ui.theme

import androidx.compose.ui.graphics.Color

/** 温暖高级本地生活 · 赤陶橙品牌色 */
val Terracotta = Color(0xFFC45C26)
val TerracottaSoft = Color(0xFFE8A06A)
val TerracottaContainer = Color(0xFFF5E0D0)
val TerracottaContainerDark = Color(0xFF5C3018)

val WarmGold = Color(0xFF8B6914)
val WarmGoldLight = Color(0xFFD4B56A)

val MossGreen = Color(0xFF3D6B4F)
val MossGreenLight = Color(0xFF8FBF9A)

val WarmCream = Color(0xFFFAF6F1)
val WarmPaper = Color(0xFFFFFFFF)
val WarmSand = Color(0xFFF0E8DF)
val WarmInk = Color(0xFF2C241C)
val WarmMuted = Color(0xFF6B5E52)
val WarmOutline = Color(0xFFD9CFC4)

val WarmCharcoal = Color(0xFF1A1612)
val WarmCardDark = Color(0xFF252019)
val WarmSurfaceVariantDark = Color(0xFF3A322A)
val WarmInkDark = Color(0xFFF5EDE4)
val WarmMutedDark = Color(0xFFB8A99A)
val WarmOutlineDark = Color(0xFF5A4E44)

val ErrorRed = Color(0xFFB42318)
val ErrorRedLight = Color(0xFFF97066)
val WarningAmber = Color(0xFFC47E0A)

// 兼容旧引用 (逐步迁移到 colorScheme)
@Deprecated("Use MaterialTheme.colorScheme.primary", ReplaceWith("Terracotta"))
val LsRed = Terracotta
@Deprecated("Use MaterialTheme.colorScheme.primary", ReplaceWith("Terracotta"))
val LsRedDark = Color(0xFFA3481C)
@Deprecated("Use MaterialTheme.colorScheme.secondary", ReplaceWith("WarmGold"))
val LsIndigo = WarmGold
@Deprecated("Use MaterialTheme.colorScheme.tertiary", ReplaceWith("WarmGoldLight"))
val LsAmber = WarmGoldLight
@Deprecated("Use MaterialTheme.colorScheme.tertiary", ReplaceWith("MossGreen"))
val LsGreen = MossGreen
