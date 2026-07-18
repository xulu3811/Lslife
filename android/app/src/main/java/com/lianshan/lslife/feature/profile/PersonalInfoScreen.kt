package com.lianshan.lslife.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lianshan.lslife.ui.components.PrimaryButton
import com.lianshan.lslife.ui.components.SoftCard
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(onBack: () -> Unit) {
    var avatar by remember { mutableStateOf("👨") }
    var nickname by remember { mutableStateOf("连山用户7665") }
    val userId = "LS-2468001"
    val phone = "138****0000"
    val registerDate = "2026-07-01"

    var showEditSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人信息") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            SoftCard {
                Column(Modifier.padding(horizontal = Dimens.md)) {
                    InfoRow(
                        label = "头像",
                        value = avatar,
                        isEditable = true,
                        onClick = { showEditSheet = true },
                        isAvatar = true
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.xs))
                    InfoRow(
                        label = "昵称",
                        value = nickname,
                        isEditable = true,
                        onClick = { showEditSheet = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.xs))
                    InfoRow("用户ID", userId)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.xs))
                    InfoRow("手机号", phone)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.xs))
                    InfoRow("注册时间", registerDate)
                }
            }
        }
    }

    if (showEditSheet) {
        ProfileEditSheet(
            currentAvatar = avatar,
            currentNickname = nickname,
            onDismiss = { showEditSheet = false },
            onSave = { newAvatar, newNickname ->
                avatar = newAvatar
                nickname = newNickname
                showEditSheet = false
            }
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    isEditable: Boolean = false,
    onClick: (() -> Unit)? = null,
    isAvatar: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isEditable && onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = Dimens.md, horizontal = Dimens.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
            if (isAvatar) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(value, fontSize = 24.sp)
                }
            } else {
                Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
            if (isEditable) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileEditSheet(
    currentAvatar: String,
    currentNickname: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var tempAvatar by remember { mutableStateOf(currentAvatar) }
    var tempNickname by remember { mutableStateOf(currentNickname) }

    val presetAvatars = listOf("👨", "👩", "👦", "👧", "👾", "🤖", "🦊", "🐶", "🐱", "🐼")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.lg, vertical = Dimens.md)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg)
        ) {
            Text(
                "编辑个人信息",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                Text("选择头像", style = MaterialTheme.typography.labelLarge)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                    contentPadding = PaddingValues(vertical = Dimens.xs)
                ) {
                    items(presetAvatars) { avatarEmoji ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    if (tempAvatar == avatarEmoji) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { tempAvatar = avatarEmoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(avatarEmoji, fontSize = 32.sp)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = tempNickname,
                onValueChange = { tempNickname = it },
                label = { Text("昵称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Dimens.sm))

            PrimaryButton(
                text = "保存更改",
                onClick = { onSave(tempAvatar, tempNickname) },
                modifier = Modifier.fillMaxWidth(),
                enabled = tempNickname.isNotBlank()
            )
            Spacer(modifier = Modifier.height(Dimens.lg))
        }
    }
}
