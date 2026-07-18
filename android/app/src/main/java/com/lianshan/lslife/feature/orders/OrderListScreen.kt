package com.lianshan.lslife.feature.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.core.model.Order
import com.lianshan.lslife.ui.components.CategoryPill
import com.lianshan.lslife.ui.components.EmptyState
import com.lianshan.lslife.ui.components.ErrorBox
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.NetworkImage
import com.lianshan.lslife.ui.components.PriceText
import com.lianshan.lslife.ui.components.SoftCard
import com.lianshan.lslife.ui.components.StatusChip
import com.lianshan.lslife.ui.components.StatusTone
import com.lianshan.lslife.ui.theme.Dimens

private enum class OrderFilter(val label: String) {
    All("全部"),
    Active("进行中"),
    Done("已完成"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    onTrack: (String) -> Unit,
    viewModel: OrdersViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf(OrderFilter.All) }
    LaunchedEffect(Unit) { viewModel.load() }
    val scheme = MaterialTheme.colorScheme

    val filtered = when (filter) {
        OrderFilter.All -> state.orders
        OrderFilter.Active -> state.orders.filter {
            it.status in setOf("pending", "paid", "preparing", "delivering")
        }
        OrderFilter.Done -> state.orders.filter { it.status in setOf("delivered", "cancelled") }
    }

    Scaffold(
        containerColor = scheme.background,
        topBar = {
            TopAppBar(
                title = { Text("我的订单") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = scheme.surface),
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = Dimens.lg, vertical = Dimens.md),
                horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
            ) {
                items(OrderFilter.entries) { f ->
                    CategoryPill(f.label, selected = filter == f, onClick = { filter = f })
                }
            }

            when {
                state.loading -> LoadingBox()
                state.error != null -> ErrorBox(state.error!!, onRetry = viewModel::load)
                filtered.isEmpty() -> EmptyState(
                    title = "还没有订单",
                    subtitle = "下单后可在这里查看配送进度",
                    icon = Icons.Filled.ReceiptLong,
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = Dimens.lg, vertical = Dimens.sm),
                    verticalArrangement = Arrangement.spacedBy(Dimens.md),
                ) {
                    items(filtered, key = { it.id }) { order ->
                        OrderRow(order) { onTrack(order.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderRow(order: Order, onClick: () -> Unit) {
    SoftCard(onClick = onClick) {
        Column(Modifier.padding(Dimens.md), verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.md),
            ) {
                NetworkImage(order.merchantLogo, order.merchantName, Modifier.size(44.dp))
                Text(
                    order.merchantName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                OrderStatusChip(order.status)
            }
            Text(
                order.items.joinToString("、") { "${it.name}x${it.quantity}" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                PriceText(order.totalAmount)
                Spacer(Modifier.weight(1f))
                Text(
                    "单号 ${order.orderNo}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (order.status in setOf("paid", "preparing", "delivering")) {
                Text(
                    "点击查看实时配送",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
fun OrderStatusChip(status: String) {
    val (text, tone) = when (status) {
        "pending" -> "待支付" to StatusTone.Error
        "paid", "preparing" -> "备餐中" to StatusTone.Warning
        "delivering" -> "配送中" to StatusTone.Success
        "delivered" -> "已送达" to StatusTone.Primary
        "cancelled" -> "已取消" to StatusTone.Neutral
        else -> status to StatusTone.Neutral
    }
    StatusChip(text, tone)
}
