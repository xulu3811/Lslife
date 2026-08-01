package com.lianshan.lslife.feature.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.core.model.CartEntry
import com.lianshan.lslife.ui.components.*
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onOpenMerchant: (String) -> Unit,
    onCheckout: (merchantId: String?, sellerId: String?, entryIds: String?, deliveryMethod: String) -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = Color(0xFFF7F7F7),
        topBar = {
            TopAppBar(
                title = { Text("购物车", fontWeight = FontWeight.Bold) },
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
                Column(Modifier.padding(padding).fillMaxSize()) {
                    // Delivery/Pickup Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = Dimens.lg, vertical = Dimens.md),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.md)
                    ) {
                        TabButton(
                            text = "配送",
                            selected = state.deliveryMethod == "DELIVERY",
                            onClick = { viewModel.setDeliveryMethod("DELIVERY") },
                            modifier = Modifier.weight(1f)
                        )
                        TabButton(
                            text = "自提",
                            selected = state.deliveryMethod == "PICKUP",
                            onClick = { viewModel.setDeliveryMethod("PICKUP") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Promotional Banner
                    if (state.deliveryMethod == "PICKUP") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF4F4))
                                .padding(vertical = Dimens.sm, horizontal = Dimens.lg)
                        ) {
                            Text(
                                "满£29.00享自提包邮, 还差£12.00",
                                color = Color(0xFFE52F2F),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    // Cart Items
                    val grouped = state.entries.groupBy { it.merchantId ?: it.sellerId ?: "unknown" }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(Dimens.md),
                        verticalArrangement = Arrangement.spacedBy(Dimens.md),
                    ) {
                        grouped.forEach { (groupId, entries) ->
                            val shopName = entries.firstOrNull()?.product?.merchant?.name ?: entries.firstOrNull()?.post?.user?.nickname ?: "未知卖家"
                            
                            item(key = "header-$groupId") {
                                SoftCard {
                                    Column(Modifier.padding(Dimens.md), verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val groupEntryIds = entries.map { it.id }.toSet()
                                            val allSelected = state.selectedEntryIds.containsAll(groupEntryIds) && groupEntryIds.isNotEmpty()
                                            JoybuyCheckbox(
                                                checked = allSelected,
                                                onCheckedChange = { viewModel.toggleGroupSelection(groupId) }
                                            )
                                            Spacer(modifier = Modifier.width(Dimens.sm))
                                            Text(
                                                shopName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                        entries.forEach { entry ->
                                            CartRow(
                                                entry = entry,
                                                selected = state.selectedEntryIds.contains(entry.id),
                                                onToggleSelect = { viewModel.toggleEntrySelection(entry) },
                                                onAdd = { viewModel.changeQty(entry, 1) },
                                                onRemove = { viewModel.changeQty(entry, -1) },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Bar
                    Surface(color = Color.White, shadowElevation = 8.dp) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.md, vertical = Dimens.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val allIds = state.entries.map { it.id }.toSet()
                            val isAllSelected = state.selectedEntryIds.containsAll(allIds) && allIds.isNotEmpty()
                            JoybuyCheckbox(
                                checked = isAllSelected,
                                onCheckedChange = {
                                    if (isAllSelected) {
                                        state.entries.firstOrNull()?.let { viewModel.toggleGroupSelection(it.merchantId ?: it.sellerId ?: "") } // Simplified logic
                                    } else {
                                        state.entries.firstOrNull()?.let { viewModel.toggleGroupSelection(it.merchantId ?: it.sellerId ?: "") }
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(Dimens.xs))
                            Text("全选", style = MaterialTheme.typography.bodyMedium)
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = Dimens.md)) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("合计: ", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "£${"%.2f".format(state.total)}",
                                        color = Color(0xFFE52F2F),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                if (state.savedAmount > 0) {
                                    Text("已省: £${"%.2f".format(state.savedAmount)}", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                            
                            val selectedCount = state.selectedEntryIds.size
                            Button(
                                onClick = {
                                    val selectedEntries = state.entries.filter { it.id in state.selectedEntryIds }
                                    val firstSelected = selectedEntries.firstOrNull()
                                    if (firstSelected != null) {
                                        val entryIdsStr = state.selectedEntryIds.joinToString(",")
                                        onCheckout(firstSelected.merchantId, firstSelected.sellerId, entryIdsStr, state.deliveryMethod)
                                    }
                                },
                                enabled = selectedCount > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE52F2F)),
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Text("去结算($selectedCount)", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bgColor = if (selected) Color(0xFFFFEBEB) else Color(0xFFF5F5F5)
    val textColor = if (selected) Color(0xFFE52F2F) else Color(0xFF333333)
    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
    
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontWeight = fontWeight, fontSize = 15.sp)
    }
}

@Composable
fun JoybuyCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Icon(
        imageVector = if (checked) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
        contentDescription = null,
        tint = if (checked) Color(0xFFE52F2F) else Color.LightGray,
        modifier = Modifier
            .size(22.dp)
            .clickable { onCheckedChange(!checked) }
    )
}

@Composable
private fun CartRow(
    entry: CartEntry,
    selected: Boolean,
    onToggleSelect: () -> Unit,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val name = entry.product?.name ?: entry.post?.title ?: "商品"
    val price = entry.product?.price ?: entry.post?.price ?: 0.0
    val originalPrice = entry.product?.originalPrice ?: entry.post?.price
    val image = entry.product?.image ?: entry.post?.images?.firstOrNull() ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelect() }
            .padding(vertical = Dimens.sm),
        verticalAlignment = Alignment.Top,
    ) {
        JoybuyCheckbox(
            checked = selected,
            onCheckedChange = { onToggleSelect() }
        )
        Spacer(modifier = Modifier.width(Dimens.sm))
        NetworkImage(image, name, Modifier.size(80.dp))
        Spacer(modifier = Modifier.width(Dimens.md))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2)
            Spacer(modifier = Modifier.height(Dimens.xs))
            
            // Tag placeholder
            Box(modifier = Modifier.background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text("不支持7天无理由", fontSize = 10.sp, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(Dimens.sm))
            
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("£${"%.2f".format(price)}", color = Color(0xFFE52F2F), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (originalPrice != null && originalPrice > price) {
                        Text(
                            "£${"%.2f".format(originalPrice)}", 
                            color = Color.Gray, 
                            fontSize = 12.sp,
                            style = androidx.compose.ui.text.TextStyle(textDecoration = TextDecoration.LineThrough)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Stepper
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                        Icon(if (entry.quantity <= 1) Icons.Filled.DeleteOutline else Icons.Filled.Remove, "减", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    Text("${entry.quantity}", modifier = Modifier.padding(horizontal = Dimens.sm), fontSize = 14.sp)
                    IconButton(onClick = onAdd, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Add, "加", tint = Color.Black, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
