package com.lianshan.lslife.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lianshan.lslife.ui.components.PrimaryButton
import com.lianshan.lslife.ui.components.SoftCard
import com.lianshan.lslife.ui.components.StatusChip
import com.lianshan.lslife.ui.components.StatusTone
import com.lianshan.lslife.ui.theme.Dimens
import java.util.UUID

data class Address(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val detail: String,
    val isDefault: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(onBack: () -> Unit) {
    val addresses = remember {
        mutableStateListOf(
            Address(name = "张三", phone = "138****1234", detail = "广东省清远市连山壮族瑶族自治县吉田镇城南大道1号", isDefault = true),
            Address(name = "李四", phone = "139****5678", detail = "广东省清远市连山壮族瑶族自治县太保镇镇政府旁边", isDefault = false)
        )
    }

    var showEditSheet by remember { mutableStateOf(false) }
    var editingAddress by remember { mutableStateOf<Address?>(null) }
    var addressToDelete by remember { mutableStateOf<Address?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("收货地址") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                },
                actions = {
                    IconButton(onClick = {
                        editingAddress = null
                        showEditSheet = true
                    }) { Icon(Icons.Filled.Add, "新增地址") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
            contentPadding = PaddingValues(vertical = Dimens.md)
        ) {
            items(addresses, key = { it.id }) { address ->
                AddressCard(
                    address = address,
                    onEdit = {
                        editingAddress = address
                        showEditSheet = true
                    },
                    onDelete = {
                        addressToDelete = address
                    }
                )
            }
        }
    }

    if (showEditSheet) {
        AddressEditSheet(
            initialAddress = editingAddress,
            onDismiss = { showEditSheet = false },
            onSave = { savedAddress ->
                if (savedAddress.isDefault) {
                    // 清除其他默认地址
                    val iterator = addresses.listIterator()
                    while (iterator.hasNext()) {
                        val a = iterator.next()
                        if (a.isDefault && a.id != savedAddress.id) {
                            iterator.set(a.copy(isDefault = false))
                        }
                    }
                }

                val index = addresses.indexOfFirst { it.id == savedAddress.id }
                if (index != -1) {
                    addresses[index] = savedAddress
                } else {
                    addresses.add(0, savedAddress) // 新增地址放前面
                }
                showEditSheet = false
            }
        )
    }

    if (addressToDelete != null) {
        AlertDialog(
            onDismissRequest = { addressToDelete = null },
            title = { Text("删除地址") },
            text = { Text("确定要删除该收货地址吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        addresses.remove(addressToDelete)
                        addressToDelete = null
                    }
                ) {
                    Text("确定删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { addressToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun AddressCard(
    address: Address,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    SoftCard {
        Column(modifier = Modifier.fillMaxWidth().padding(Dimens.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                    Text(address.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(address.phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (address.isDefault) {
                        StatusChip("默认", StatusTone.Success)
                    }
                }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, "编辑", tint = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "删除", tint = MaterialTheme.colorScheme.error) }
                }
            }
            Spacer(modifier = Modifier.height(Dimens.xs))
            Text(address.detail, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressEditSheet(
    initialAddress: Address?,
    onDismiss: () -> Unit,
    onSave: (Address) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var name by remember { mutableStateOf(initialAddress?.name ?: "") }
    var phone by remember { mutableStateOf(initialAddress?.phone ?: "") }
    var detail by remember { mutableStateOf(initialAddress?.detail ?: "") }
    var isDefault by remember { mutableStateOf(initialAddress?.isDefault ?: false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.lg, vertical = Dimens.md)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Text(
                if (initialAddress == null) "新增收货地址" else "编辑收货地址",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("收货人") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("手机号码") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = detail,
                onValueChange = { detail = it },
                label = { Text("详细地址") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("设为默认地址", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = isDefault, onCheckedChange = { isDefault = it })
            }

            Spacer(modifier = Modifier.height(Dimens.sm))

            PrimaryButton(
                text = "保存地址",
                onClick = {
                    val finalAddress = initialAddress?.copy(
                        name = name,
                        phone = phone,
                        detail = detail,
                        isDefault = isDefault
                    ) ?: Address(
                        name = name,
                        phone = phone,
                        detail = detail,
                        isDefault = isDefault
                    )
                    onSave(finalAddress)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && phone.isNotBlank() && detail.isNotBlank()
            )
            Spacer(modifier = Modifier.height(Dimens.lg))
        }
    }
}
