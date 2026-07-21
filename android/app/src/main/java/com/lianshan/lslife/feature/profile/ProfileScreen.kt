package com.lianshan.lslife.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.NetworkImage
import com.lianshan.lslife.ui.components.PrimaryButton
import com.lianshan.lslife.ui.components.SoftCard
import com.lianshan.lslife.ui.components.StatusChip
import com.lianshan.lslife.ui.components.StatusTone
import com.lianshan.lslife.ui.theme.Dimens

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
    onOpenPersonalInfo: () -> Unit,
    onOpenMembership: () -> Unit,
    onOpenAddress: () -> Unit,
    onOpenMessage: () -> Unit,
    onOpenRealName: () -> Unit,
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
        containerColor = scheme.background,
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(scheme.primary, scheme.primary.copy(alpha = 0.9f), scheme.primaryContainer),
                        ),
                    )
                    .clickable(onClick = onOpenPersonalInfo)
                    .statusBarsPadding()
                    .padding(Dimens.xl),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                ) {
                    NetworkImage(user?.avatar, "头像", Modifier.size(72.dp).clip(CircleShape))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            user?.nickname ?: "-",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onPrimary,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                            StatusChip(tierLabel(user?.membershipTier), StatusTone.Warning)
                            StatusChip(
                                if (user?.realNameStatus == "verified") "已实名" else "未实名",
                                if (user?.realNameStatus == "verified") StatusTone.Success else StatusTone.Neutral,
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(Dimens.lg),
                verticalArrangement = Arrangement.spacedBy(Dimens.md),
            ) {
                SoftCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(Dimens.lg),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        StatCell("余额", "¥%.1f".format(user?.walletBalance ?: 0.0))
                        StatCell("积分", "${user?.points ?: 0}")
                        StatCell("未读", "${state.unread}")
                    }
                }

                PrimaryButton(
                    text = "升级会员 · 提升发布额度",
                    onClick = onOpenMembership,
                    modifier = Modifier.fillMaxWidth(),
                )

                SoftCard {
                    Column {
                        ProfileMenuRow(Icons.Filled.Place, "收货地址", "管理常用地址", onClick = onOpenAddress)
                        ProfileMenuRow(Icons.Filled.Notifications, "消息通知", "${state.unread} 条未读", onClick = onOpenMessage)
                        ProfileMenuRow(
                            Icons.Filled.VerifiedUser,
                            "实名认证",
                            if (user?.realNameStatus == "verified") "已完成" else "去认证",
                            onClick = onOpenRealName,
                        )
                        ProfileMenuRow(
                            Icons.Filled.WorkspacePremium,
                            "会员权益",
                            tierLabel(user?.membershipTier),
                            onClick = onOpenMembership,
                        )
                        ProfileMenuRow(
                            Icons.Filled.Settings,
                            "设置",
                            "主题 · 通知 · 隐私与关于",
                            onClick = onOpenSettings,
                            showDivider = false,
                        )
                    }
                }

                OutlinedButton(
                    onClick = viewModel::logout,
                    modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeight),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text("退出登录")
                }

                Text(
                    "© 2026 连山壮族瑶族自治县 · 智慧同城生活平台",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(Dimens.lg))
            }
        }
    }
}

@Composable
private fun StatCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    showDivider: Boolean = true,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = Dimens.md, vertical = Dimens.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 68.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            )
        }
    }
}

private fun tierLabel(tier: String?) = when (tier) {
    "vip" -> "超级会员"
    "premium" -> "至尊会员"
    else -> "普通用户"
}
