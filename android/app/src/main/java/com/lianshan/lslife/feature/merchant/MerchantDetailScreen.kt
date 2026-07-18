package com.lianshan.lslife.feature.merchant

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.core.model.Product
import com.lianshan.lslife.ui.components.ErrorBox
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.NetworkImage
import com.lianshan.lslife.ui.components.PriceText
import com.lianshan.lslife.ui.components.PrimaryButton
import com.lianshan.lslife.ui.components.SoftCard
import com.lianshan.lslife.ui.components.TagPill
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantDetailScreen(
    merchantId: String,
    onBack: () -> Unit,
    onCheckedOut: (String) -> Unit,
    viewModel: MerchantDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(merchantId) { viewModel.load(merchantId) }
    LaunchedEffect(state.checkedOutOrderId) { state.checkedOutOrderId?.let(onCheckedOut) }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = scheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(state.merchant?.name ?: "商家详情", maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = scheme.surface),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (state.merchant != null) {
                Surface(color = scheme.surface, tonalElevation = 3.dp, shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.lg, vertical = Dimens.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            PriceText(state.payable)
                            Text(
                                "含配送费 ¥${state.merchant!!.deliveryFee}",
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                        PrimaryButton(
                            text = if (state.totalCount > 0) "去结算(${state.totalCount})" else "请选择商品",
                            onClick = viewModel::checkout,
                            enabled = state.totalCount > 0 && !state.checkingOut,
                            loading = state.checkingOut,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
    ) { padding ->
        when {
            state.loading -> LoadingBox(Modifier.padding(padding).fillMaxSize())
            state.error != null -> ErrorBox(
                state.error!!,
                onRetry = { viewModel.load(merchantId) },
                modifier = Modifier.padding(padding).fillMaxSize(),
            )
            else -> {
                val m = state.merchant!!
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(Dimens.lg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.md),
                ) {
                    item {
                        SoftCard {
                            NetworkImage(
                                url = m.banner,
                                contentDescription = m.name,
                                modifier = Modifier.fillMaxWidth().height(Dimens.bannerHeight),
                                contentScale = ContentScale.Crop,
                            )
                            Column(
                                modifier = Modifier.padding(Dimens.md),
                                verticalArrangement = Arrangement.spacedBy(Dimens.sm),
                            ) {
                                Text(m.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                                ) {
                                    Surface(color = scheme.secondaryContainer, shape = CircleShape) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        ) {
                                            Icon(Icons.Filled.Star, null, Modifier.size(14.dp), tint = scheme.secondary)
                                            Text("${m.rating}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(
                                        "月售${m.sales} · ${m.distance}km",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = scheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    if (m.deliveryFee == 0.0) "免配送费 · 约 ${m.deliveryTime} 分钟送达"
                                    else "配送费 ¥${m.deliveryFee} · 约 ${m.deliveryTime} 分钟",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = scheme.onSurfaceVariant,
                                )
                                Text(m.description, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                                    m.tags.take(4).forEach { TagPill(it) }
                                }
                            }
                        }
                    }
                    item {
                        Text("精选服务", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(m.items, key = { it.id }) { p ->
                        ProductRow(
                            product = p,
                            qty = state.quantities[p.id] ?: 0,
                            onAdd = { viewModel.changeQty(p.id, 1) },
                            onRemove = { viewModel.changeQty(p.id, -1) },
                        )
                    }
                    item { Spacer(Modifier.height(Dimens.sm)) }
                }
            }
        }
    }
}

@Composable
private fun ProductRow(product: Product, qty: Int, onAdd: () -> Unit, onRemove: () -> Unit) {
    SoftCard {
        Row(Modifier.padding(Dimens.md), horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
            NetworkImage(product.image, product.name, Modifier.size(Dimens.thumbLg))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2)
                Text(product.desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                Text("月售${product.sales}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PriceText(product.price)
                    Spacer(Modifier.weight(1f))
                    QuantityControl(qty, onAdd, onRemove)
                }
            }
        }
    }
}

@Composable
private fun QuantityControl(qty: Int, onAdd: () -> Unit, onRemove: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (qty > 0) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp).background(scheme.surfaceVariant, CircleShape),
            ) {
                Icon(Icons.Filled.Remove, "减", Modifier.size(16.dp))
            }
            Text("$qty", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = Dimens.sm))
        }
        IconButton(
            onClick = onAdd,
            modifier = Modifier.size(32.dp).background(scheme.primary, CircleShape),
        ) {
            Icon(Icons.Filled.Add, "加", Modifier.size(16.dp), tint = scheme.onPrimary)
        }
    }
}
