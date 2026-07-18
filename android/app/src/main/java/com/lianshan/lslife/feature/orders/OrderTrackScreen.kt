package com.lianshan.lslife.feature.orders

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.ui.components.ErrorBox
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.PriceText
import com.lianshan.lslife.ui.components.SoftCard
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: OrderTrackViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(orderId) { viewModel.start(orderId) }
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = scheme.background,
        topBar = {
            TopAppBar(
                title = { Text("实时配送追踪") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = scheme.surface),
            )
        },
    ) { padding ->
        when {
            state.loading -> LoadingBox(Modifier.padding(padding).fillMaxSize())
            state.error != null -> ErrorBox(state.error!!, modifier = Modifier.padding(padding).fillMaxSize())
            else -> {
                val order = state.order!!
                val delivery = order.delivery
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(Dimens.lg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.md),
                ) {
                    val statusText = when (delivery?.status) {
                        "preparing" -> "商家备餐中，请耐心等待"
                        "delivering" -> "骑手正在全速配送中"
                        "delivered" -> "订单已送达，请享用"
                        else -> "订单已支付，准备出发"
                    }
                    Text(statusText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                    SoftCard {
                        Column(Modifier.padding(Dimens.md), verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
                            Timeline(
                                status = delivery?.status ?: order.status,
                            )
                            if (delivery != null) {
                                LinearProgressIndicator(
                                    progress = { delivery.progress / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = scheme.tertiary,
                                    trackColor = scheme.surfaceVariant,
                                )
                                Text(
                                    "预计还需 ${delivery.secondsRemaining} 秒",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = scheme.onSurfaceVariant,
                                )
                                RouteMap(progress = delivery.progress / 100f)
                            }
                        }
                    }

                    if (delivery != null) {
                        SoftCard {
                            Row(
                                modifier = Modifier.padding(Dimens.md),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(scheme.tertiaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Filled.LocalShipping, null, tint = scheme.tertiary)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(delivery.rider.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(delivery.rider.phone, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                                }
                                OutlinedButton(onClick = { /* dial */ }, shape = MaterialTheme.shapes.medium) {
                                    Text("联系骑手")
                                }
                            }
                        }
                    }

                    SoftCard {
                        Column(Modifier.padding(Dimens.md), verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                            Text("订单信息", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("收货地址：${order.deliveryAddress}", style = MaterialTheme.typography.bodyMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("实付金额", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                PriceText(order.totalAmount)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Timeline(status: String) {
    val steps = listOf(
        Triple("paid", "已支付", Icons.Filled.Storefront),
        Triple("preparing", "备餐中", Icons.Filled.Restaurant),
        Triple("delivering", "配送中", Icons.Filled.LocalShipping),
        Triple("delivered", "已送达", Icons.Filled.CheckCircle),
    )
    val activeIndex = when (status) {
        "paid" -> 0
        "preparing" -> 1
        "delivering" -> 2
        "delivered" -> 3
        else -> 0
    }
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
        steps.forEachIndexed { index, (key, label, icon) ->
            val active = index <= activeIndex
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon,
                        null,
                        tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RouteMap(progress: Float) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val track = MaterialTheme.colorScheme.outlineVariant
    SoftCard {
        Canvas(Modifier.fillMaxWidth().height(160.dp).padding(Dimens.xl)) {
            val start = Offset(0f, size.height)
            val end = Offset(size.width, 0f)
            drawLine(track, start, end, strokeWidth = 10f, cap = StrokeCap.Round)
            drawLine(
                tertiary,
                start,
                Offset(start.x + (end.x - start.x) * progress, start.y + (end.y - start.y) * progress),
                strokeWidth = 10f,
                cap = StrokeCap.Round,
            )
            drawCircle(primary, radius = 14f, center = start)
            drawCircle(primary.copy(alpha = 0.7f), radius = 14f, center = end)
            val rider = Offset(start.x + (end.x - start.x) * progress, start.y + (end.y - start.y) * progress)
            drawCircle(tertiary, radius = 18f, center = rider)
        }
    }
}
