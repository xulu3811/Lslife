package com.lianshan.lslife.feature.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lianshan.lslife.feature.publish.getCategoryConfig
import com.lianshan.lslife.feature.publish.secondHandBrandSuggestions
import com.lianshan.lslife.feature.publish.secondHandConditionOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFilterBottomSheet(
    category: String?,
    minPrice: Double?,
    maxPrice: Double?,
    attributesFilter: Map<String, String>,
    onPriceChange: (Double?, Double?) -> Unit,
    onAttributeToggle: (String, String) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var minPriceText by remember(minPrice) { mutableStateOf(minPrice?.let { if (it % 1 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var maxPriceText by remember(maxPrice) { mutableStateOf(maxPrice?.let { if (it % 1 == 0.0) it.toInt().toString() else it.toString() } ?: "") }

    val currentCategory = category ?: "second_hand"
    val config = remember(currentCategory) { getCategoryConfig(currentCategory) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "高级筛选·属性规配",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = {
                    minPriceText = ""
                    maxPriceText = ""
                    onReset()
                }) {
                    Text("全部重置", color = MaterialTheme.colorScheme.primary)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // 价格区间 (Price Range)
            Text(
                text = "价格区间 (元)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = minPriceText,
                    onValueChange = { 
                        minPriceText = it.filter { c -> c.isDigit() || c == '.' }
                        onPriceChange(minPriceText.toDoubleOrNull(), maxPriceText.toDoubleOrNull())
                    },
                    placeholder = { Text("最低价", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Text("—", color = MaterialTheme.colorScheme.outline)
                OutlinedTextField(
                    value = maxPriceText,
                    onValueChange = { 
                        maxPriceText = it.filter { c -> c.isDigit() || c == '.' }
                        onPriceChange(minPriceText.toDoubleOrNull(), maxPriceText.toDoubleOrNull())
                    },
                    placeholder = { Text("最高价", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // 价格快捷药丸
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 16.dp)
            ) {
                val priceRanges = listOf(
                    "0-100" to (0.0 to 100.0),
                    "100-500" to (100.0 to 500.0),
                    "500-2000" to (500.0 to 2000.0),
                    "2000以上" to (2000.0 to null)
                )
                items(priceRanges) { (label, range) ->
                    val isSelected = (minPrice == range.first) && (maxPrice == range.second)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                minPriceText = ""
                                maxPriceText = ""
                                onPriceChange(null, null)
                            } else {
                                minPriceText = range.first?.toInt()?.toString() ?: ""
                                maxPriceText = range.second?.toInt()?.toString() ?: ""
                                onPriceChange(range.first, range.second)
                            }
                        },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // 动态规格属性筛选 (Dynamic Attributes)
            if (currentCategory in listOf("second_hand", "cat_idle") || category == null || category == "all") {
                // 个人闲置 / 全部分类：展示品牌与成色
                Text(
                    text = "品牌 (Brand)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
                OptFlowRow(
                    options = secondHandBrandSuggestions,
                    selectedVal = attributesFilter["brand"] ?: attributesFilter["品牌"],
                    onSelect = { onAttributeToggle("brand", it) }
                )

                Text(
                    text = "成色 (Condition)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                OptFlowRow(
                    options = secondHandConditionOptions,
                    selectedVal = attributesFilter["condition"] ?: attributesFilter["成色"],
                    onSelect = { onAttributeToggle("condition", it) }
                )
            } else {
                // 其他分类：基于 CategoryConfig 渲染
                if (config.attr1Label != null && config.attr1Options.isNotEmpty()) {
                    Text(
                        text = config.attr1Label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                    OptFlowRow(
                        options = config.attr1Options,
                        selectedVal = attributesFilter[config.attr1Label] ?: attributesFilter["attr1"],
                        onSelect = { onAttributeToggle(config.attr1Label, it) }
                    )
                }

                if (config.attr2Label != null && config.attr2Options.isNotEmpty()) {
                    Text(
                        text = config.attr2Label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    OptFlowRow(
                        options = config.attr2Options,
                        selectedVal = attributesFilter[config.attr2Label] ?: attributesFilter["attr2"],
                        onSelect = { onAttributeToggle(config.attr2Label, it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer action
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "查看筛选结果",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptFlowRow(
    options: List<String>,
    selectedVal: String?,
    onSelect: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEach { opt ->
            val isSelected = (selectedVal == opt)
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(opt) },
                label = { Text(opt) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}
