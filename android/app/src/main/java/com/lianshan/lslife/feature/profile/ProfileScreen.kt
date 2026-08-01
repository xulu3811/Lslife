package com.lianshan.lslife.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.LocalActivity
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.NetworkImage
import com.lianshan.lslife.ui.components.SoftCard
import com.lianshan.lslife.ui.theme.Dimens

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
    onOpenPersonalInfo: () -> Unit,
    onOpenMembership: () -> Unit,
    onOpenAddress: () -> Unit,
    onOpenMessage: () -> Unit,
    onOpenRealName: () -> Unit,
    onOpenMyPosts: () -> Unit,
    onOpenWallet: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.loggedOut) { if (state.loggedOut) onLoggedOut() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (state.loading) {
            LoadingBox(Modifier.padding(padding).fillMaxSize())
            return@Scaffold
        }
        val user = state.user
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Joybuy Style Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.lg, vertical = Dimens.xl)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NetworkImage(
                    user?.avatar, 
                    "头像", 
                    Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)) // Light gray background if no avatar
                        .clickable(onClick = onOpenPersonalInfo)
                )
                Spacer(modifier = Modifier.width(Dimens.md))
                Column(modifier = Modifier.weight(1f).clickable(onClick = onOpenPersonalInfo)) {
                    Text(
                        text = user?.nickname ?: "未登录",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user?.phone ?: "点击登录/查看个人信息",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
                
                // Top Right Notification Icon
                if (state.unread > 0) {
                    BadgedBox(
                        badge = { Badge { Text(state.unread.toString()) } }
                    ) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "消息",
                            tint = scheme.onBackground,
                            modifier = Modifier.size(24.dp).clickable(onClick = onOpenMessage)
                        )
                    }
                } else {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "消息",
                        tint = scheme.onBackground,
                        modifier = Modifier.size(24.dp).clickable(onClick = onOpenMessage)
                    )
                }
            }

            // --- Section 1: 订单 (Orders) ---
            SectionTitle(title = "订单", showChevron = true, onClick = { /* To full orders */ })
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.sm, vertical = Dimens.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OrderGridItem(icon = Icons.Outlined.Schedule, label = "待付款", modifier = Modifier.weight(1f))
                OrderGridItem(icon = Icons.Outlined.LocalShipping, label = "待收货", modifier = Modifier.weight(1f))
                OrderGridItem(icon = Icons.Outlined.CheckCircleOutline, label = "已完成", modifier = Modifier.weight(1f))
                OrderGridItem(icon = Icons.Outlined.ChatBubbleOutline, label = "评价", modifier = Modifier.weight(1f))
                OrderGridItem(icon = Icons.Outlined.HeadsetMic, label = "售后", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(Dimens.md))

            // --- Section 2: 钱包 (Wallet) ---
            SectionTitle(title = "钱包")
            ProfileMenuRow(
                icon = Icons.Outlined.AccountBalanceWallet,
                title = "余额",
                rightText = "¥%.2f".format(user?.walletBalance ?: 0.0),
                onClick = onOpenWallet,
                showDivider = true
            )
            ProfileMenuRow(
                icon = Icons.Outlined.LocalActivity,
                title = "积分",
                rightText = "${user?.points ?: 0}分",
                onClick = onOpenWallet,
                showDivider = true
            )
            ProfileMenuRow(
                icon = Icons.Outlined.WorkspacePremium,
                title = "会员权益",
                rightText = tierLabel(user?.membershipTier),
                onClick = onOpenMembership,
                showDivider = true
            )

            // --- Section 3: 个人与服务 (Personal Info & Services) ---
            SectionTitle(title = "个人与服务")
            ProfileMenuRow(
                icon = Icons.Outlined.Place,
                title = "收货地址",
                onClick = onOpenAddress,
                showDivider = true
            )
            ProfileMenuRow(
                icon = Icons.Outlined.Edit,
                title = "我的发布",
                onClick = onOpenMyPosts,
                showDivider = true
            )
            ProfileMenuRow(
                icon = Icons.Outlined.VerifiedUser,
                title = "实名认证",
                rightText = if (user?.realNameStatus == "verified") "已完成" else "去认证",
                onClick = onOpenRealName,
                showDivider = false
            )

            Spacer(modifier = Modifier.height(Dimens.md))

            // --- Section 4: 更多 (More) ---
            SectionTitle(title = "更多")
            ProfileMenuRow(
                icon = Icons.Outlined.Settings,
                title = "设置&隐私",
                onClick = onOpenSettings,
                showDivider = true
            )
            
            // Logout Row (Styled as a menu row to fit minimalist design, or keep as a row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = viewModel::logout)
                    .padding(horizontal = Dimens.lg, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("退出登录", style = MaterialTheme.typography.titleSmall, color = Color(0xFFE52F2F))
            }

            Spacer(modifier = Modifier.height(Dimens.xl))
            Text(
                "© 2026 连山壮族瑶族自治县 · 智慧同城生活平台",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(Dimens.xxl))
        }
    }
}

@Composable
private fun SectionTitle(title: String, showChevron: Boolean = false, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = Dimens.lg, vertical = Dimens.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        if (showChevron) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun OrderGridItem(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(26.dp), tint = MaterialTheme.colorScheme.onBackground)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}



@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    rightText: String? = null,
    onClick: () -> Unit = {},
    showDivider: Boolean = true,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = Dimens.lg, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            
            if (rightText != null) {
                Text(rightText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.lg) // Joybuy style thin full width divider
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
            )
        }
    }
}

private fun tierLabel(tier: String?) = when (tier) {
    "vip" -> "超级会员"
    "premium" -> "至尊会员"
    else -> "普通用户"
}
