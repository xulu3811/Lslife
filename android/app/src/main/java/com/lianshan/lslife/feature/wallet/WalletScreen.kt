package com.lianshan.lslife.feature.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lianshan.lslife.core.model.WalletTransaction
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onNavigateBack: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRechargeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.rechargeSuccess) {
        if (uiState.rechargeSuccess) {
            showRechargeDialog = false
            viewModel.clearRechargeSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的钱包") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE52F2F),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // Hero Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE52F2F))
                    .padding(bottom = Dimens.lg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.lg, vertical = Dimens.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "总资产 (元)",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(Dimens.sm))
                    Text(
                        text = "¥${"%.2f".format(uiState.balance)}",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(Dimens.md))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${uiState.points}", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text(text = "积分", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                        }
                        // Placeholder for other assets like coupons
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "0", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text(text = "优惠券", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Action Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.md)
                    .offset(y = (-20).dp),
                shape = RoundedCornerShape(Dimens.md),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.lg),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WalletActionButton(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "充值",
                        onClick = { showRechargeDialog = true }
                    )
                    WalletActionButton(
                        icon = Icons.Default.Payment,
                        label = "提现",
                        onClick = { /* TODO: Withdraw */ }
                    )
                }
            }

            // Transaction History
            Text(
                text = "资金明细",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm)
            )

            if (uiState.isLoading && uiState.transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无流水明细", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = Dimens.md, vertical = Dimens.sm),
                    verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                ) {
                    items(uiState.transactions) { tx ->
                        TransactionItem(tx)
                    }
                }
            }
        }
    }

    if (showRechargeDialog) {
        RechargeDialog(
            onDismiss = { showRechargeDialog = false },
            onConfirm = { amount -> viewModel.recharge(amount) },
            isLoading = uiState.isRecharging
        )
    }
}

@Composable
fun WalletActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(Dimens.sm))
            .padding(Dimens.sm)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .background(Color(0xFFFFF0F0), CircleShape)
                .size(48.dp)
        ) {
            Icon(icon, contentDescription = label, tint = Color(0xFFE52F2F))
        }
        Spacer(modifier = Modifier.height(Dimens.xs))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TransactionItem(tx: WalletTransaction) {
    val isIncome = tx.amount > 0
    val amountColor = if (isIncome) Color(0xFFE52F2F) else Color.Black
    val amountPrefix = if (isIncome) "+" else ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.description ?: tx.bizType,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tx.createdAt.take(16).replace("T", " "), // Format basic date
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(
                text = "$amountPrefix${tx.amount}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    isLoading: Boolean
) {
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("钱包充值") },
        text = {
            Column {
                Text("请输入充值金额 (模拟)", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(Dimens.sm))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("金额") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    amountText.toDoubleOrNull()?.let {
                        if (it > 0) onConfirm(it)
                    }
                },
                enabled = !isLoading && amountText.toDoubleOrNull()?.let { it > 0 } == true,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE52F2F))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("充值")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color.Gray)
            }
        }
    )
}
