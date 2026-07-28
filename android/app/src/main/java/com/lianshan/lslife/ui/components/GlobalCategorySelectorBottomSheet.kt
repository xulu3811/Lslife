package com.lianshan.lslife.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lianshan.lslife.core.model.CategoryNode
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GlobalCategorySelectorBottomSheet(
    categoryTree: List<CategoryNode>,
    onDismissRequest: () -> Unit,
    onCategorySelected: (categoryId: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Left column level-1 category selection
    var selectedL1 by remember(categoryTree) { mutableStateOf(categoryTree.firstOrNull()) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f) // Occupy 85% of screen height for global selector
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.md, vertical = Dimens.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "全部分类",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "关闭")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            // Dual pane layout
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                // Left Pane: Level 1 Categories
                LazyColumn(
                    modifier = Modifier
                        .width(100.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    items(categoryTree) { l1Node ->
                        val isSelected = selectedL1?.id == l1Node.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.background else Color.Transparent
                                )
                                .clickable { selectedL1 = l1Node },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.primary)
                                        .align(Alignment.CenterStart)
                                )
                            }
                            Text(
                                text = l1Node.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Right Pane: Level 2 & 3 Categories
                val l2Children = selectedL1?.children ?: emptyList()
                if (l2Children.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无细分分类",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = Dimens.md)
                    ) {
                        item {
                            // "All in this category" button
                            selectedL1?.let { rootNode ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = Dimens.md)
                                        .clip(MaterialTheme.shapes.small)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable { onCategorySelected(rootNode.id) }
                                        .padding(Dimens.sm),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "查看全部${rootNode.name}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        items(l2Children) { l2Node ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Dimens.md)
                            ) {
                                // L2 Title
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = l2Node.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (l2Node.children.isEmpty()) {
                                        Text(
                                            text = "选择 >",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable { onCategorySelected(l2Node.id) }.padding(4.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(Dimens.sm))

                                // L3 Grid using FlowRow
                                if (l2Node.children.isNotEmpty()) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                                        verticalArrangement = Arrangement.spacedBy(Dimens.md),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        l2Node.children.forEach { l3Node ->
                                            Column(
                                                modifier = Modifier
                                                    .width(72.dp)
                                                    .clip(MaterialTheme.shapes.small)
                                                    .clickable { onCategorySelected(l3Node.id) }
                                                    .padding(vertical = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .background(
                                                            MaterialTheme.colorScheme.surfaceVariant,
                                                            shape = MaterialTheme.shapes.medium
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (!l3Node.iconUrl.isNullOrBlank()) {
                                                        CategoryIconView(iconUrl = l3Node.iconUrl, iconName = l3Node.icon, size = 32.dp)
                                                    } else if (!l3Node.icon.isNullOrBlank()) {
                                                        Text(l3Node.icon, fontSize = 24.sp)
                                                    } else {
                                                        Text(l3Node.name.take(1), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = l3Node.name,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}
