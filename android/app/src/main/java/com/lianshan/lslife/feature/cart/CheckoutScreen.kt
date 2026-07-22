package com.lianshan.lslife.feature.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.ui.components.ErrorBox
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.NetworkImage
import com.lianshan.lslife.ui.components.PriceText
import com.lianshan.lslife.ui.components.SoftCard
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    merchantId: String?,
    sellerId: String?,
    onBack: () -> Unit,
    onOrderCreated: (String) -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = scheme.background,
        topBar = {
            TopAppBar(
                title = { Text("确认订单") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = scheme.surface),
            )
        },
        bottomBar = {
            if (!state.loading && state.error == null && state.entries.isNotEmpty()) {
                Surface(color = scheme.surface, tonalElevation = 3.dp, shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(Dimens.lg),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("实付", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                            PriceText(state.totalAmount)
                        }
                        Button(
                            onClick = { viewModel.submitOrder(onOrderCreated) },
                            enabled = !state.isCreatingOrder && state.address != null,
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                        ) {
                            Text(if (state.isCreatingOrder) "提交中..." else "提交订单", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            state.loading -> LoadingBox(Modifier.padding(padding).fillMaxSize())
            state.error != null -> ErrorBox(state.error!!, onRetry = { }, modifier = Modifier.padding(padding).fillMaxSize())
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(Dimens.lg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.md),
                ) {
                    item {
                        SoftCard {
                            Row(
                                modifier = Modifier.padding(Dimens.lg).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimens.md)
                            ) {
                                Icon(Icons.Filled.LocationOn, "地址", tint = scheme.primary)
                                Column(modifier = Modifier.weight(1f)) {
                                    if (state.address != null) {
                                        Text("${state.address!!.name} ${state.address!!.phone}", fontWeight = FontWeight.Bold)
                                        Text(state.address!!.address, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                                    } else {
                                        Text("请先添加收货地址", color = scheme.error)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        SoftCard {
                            Column(Modifier.padding(Dimens.lg), verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
                                val shopName = state.entries.firstOrNull()?.product?.merchant?.name ?: state.entries.firstOrNull()?.post?.user?.nickname ?: "商家"
                                Text(shopName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                
                                state.entries.forEach { entry ->
                                    val name = entry.product?.name ?: entry.post?.title ?: "商品"
                                    val price = entry.product?.price ?: entry.post?.price ?: 0.0
                                    val image = entry.product?.image ?: entry.post?.images?.firstOrNull() ?: ""

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.xs),
                                        horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        NetworkImage(image, name, Modifier.size(Dimens.thumbMd))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                            Text("x${entry.quantity}", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                                        }
                                        PriceText(price * entry.quantity)
                                    }
                                }

                                Spacer(Modifier.height(Dimens.sm))
                                Divider()
                                Spacer(Modifier.height(Dimens.sm))

                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("商品小计")
                                    PriceText(state.itemsTotal)
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("配送费")
                                    PriceText(state.deliveryFee)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
