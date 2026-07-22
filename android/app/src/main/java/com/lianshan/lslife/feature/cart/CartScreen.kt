package com.lianshan.lslife.feature.cart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.core.model.CartEntry
import com.lianshan.lslife.ui.components.EmptyState
import com.lianshan.lslife.ui.components.ErrorBox
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.NetworkImage
import com.lianshan.lslife.ui.components.PriceText
import com.lianshan.lslife.ui.components.SoftCard
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onOpenMerchant: (String) -> Unit,
    onCheckout: (merchantId: String?, sellerId: String?) -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = scheme.background,
        topBar = {
            TopAppBar(
                title = { Text("购物车") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = scheme.surface),
            )
        },
    ) { padding ->
        when {
            state.loading -> LoadingBox(Modifier.padding(padding).fillMaxSize())
            state.error != null -> ErrorBox(state.error!!, onRetry = viewModel::load, modifier = Modifier.padding(padding).fillMaxSize())
            state.entries.isEmpty() -> EmptyState(
                title = "购物车还是空的",
                subtitle = "去首页逛逛心仪的本地服务吧",
                icon = Icons.Filled.ShoppingCart,
                modifier = Modifier.padding(padding).fillMaxSize(),
            )
            else -> {
                // Group by merchantId or sellerId
                val grouped = state.entries.groupBy { it.merchantId ?: it.sellerId ?: "unknown" }
                Column(Modifier.padding(padding).fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(Dimens.lg),
                        verticalArrangement = Arrangement.spacedBy(Dimens.md),
                    ) {
                        grouped.forEach { (groupId, entries) ->
                            val isMerchant = entries.firstOrNull()?.merchantId != null
                            val shopName = entries.firstOrNull()?.product?.merchant?.name ?: entries.firstOrNull()?.post?.user?.nickname ?: "未知卖家"
                            
                            item(key = "header-$groupId") {
                                SoftCard(onClick = { 
                                    if (isMerchant) onCheckout(groupId, null) else onCheckout(null, groupId)
                                }) {
                                    Column(Modifier.padding(Dimens.md), verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                                        Text(
                                            "【$shopName】的商品 · 点击去结算",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = scheme.primary,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        entries.forEach { entry ->
                                            CartRow(
                                                entry = entry,
                                                onAdd = { viewModel.changeQty(entry, 1) },
                                                onRemove = { viewModel.changeQty(entry, -1) },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Surface(color = scheme.surface, tonalElevation = 3.dp, shadowElevation = 8.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(Dimens.lg),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("合计", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                                PriceText(state.total)
                            }
                            Text(
                                "请点击上方卡片进入结算",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartRow(
    entry: CartEntry,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val name = entry.product?.name ?: entry.post?.title ?: "商品"
    val price = entry.product?.price ?: entry.post?.price ?: 0.0
    val image = entry.product?.image ?: entry.post?.images?.firstOrNull() ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.xs),
        horizontalArrangement = Arrangement.spacedBy(Dimens.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NetworkImage(image, name, Modifier.size(Dimens.thumbMd))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
            PriceText(price)
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Remove, "减", tint = scheme.onSurfaceVariant)
        }
        Text("${entry.quantity}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        IconButton(onClick = onAdd) {
            Icon(Icons.Filled.Add, "加", tint = scheme.primary)
        }
    }
}
