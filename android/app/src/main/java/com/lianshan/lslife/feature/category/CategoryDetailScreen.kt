package com.lianshan.lslife.feature.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.feature.search.AdvancedFilterBottomSheet
import com.lianshan.lslife.ui.components.CategoryPill
import com.lianshan.lslife.ui.components.CategoryIconView
import com.lianshan.lslife.ui.components.EmptyState
import com.lianshan.lslife.ui.components.ErrorBox
import com.lianshan.lslife.ui.components.PostListCard
import com.lianshan.lslife.ui.components.SkeletonCard
import com.lianshan.lslife.ui.theme.Dimens

private val sorts = listOf(
    "default" to "推荐",
    "latest" to "最新",
    "price_asc" to "价格最低",
    "price_desc" to "价格最高",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    viewModel: CategoryDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyStaggeredGridState()

    if (state.showFilterBottomSheet) {
        AdvancedFilterBottomSheet(
            schema = state.currentSchema,
            categoryTree = state.categoryTree,
            selectedCategory = if (state.selectedSubCategory == "all") state.parentCategoryId else state.selectedSubCategory,
            publisherType = state.publisherType,
            listingType = state.listingType,
            minPrice = state.minPrice,
            maxPrice = state.maxPrice,
            attributesFilter = state.attributesFilter,
            onPublisherTypeChange = viewModel::updatePublisherType,
            onListingTypeChange = viewModel::updateListingType,
            onCategoryChange = { c -> viewModel.onSubCategory(c ?: "all") },
            onPriceChange = viewModel::updatePrice,
            onAttributeToggle = viewModel::updateAttributeFilter,
            onReset = viewModel::clearAttributesFilter,
            onDismiss = { viewModel.setShowFilterBottomSheet(false) },
            onConfirm = { viewModel.setShowFilterBottomSheet(false) }
        )
    }

    LaunchedEffect(listState, state.loading, state.loadingMore, state.hasMore) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null) {
                    val totalItems = listState.layoutInfo.totalItemsCount
                    if (totalItems - lastIndex <= 3 && state.hasMore && !state.loading && !state.loadingMore) {
                        viewModel.loadMore()
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.parentCategoryName, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (state.subCategories.isNotEmpty()) {
                val displayCategories = remember(state.subCategories) {
                    listOf(com.lianshan.lslife.core.model.CategoryNode(id = "all", name = "全部", icon = "all")) + state.subCategories
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.sm),
                    modifier = Modifier.padding(vertical = Dimens.xs)
                ) {
                    displayCategories.chunked(4).forEach { categoryRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            categoryRow.forEach { item ->
                                val selected = state.selectedSubCategory == item.id
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { 
                                            viewModel.onSubCategory(item.id)
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                                shape = MaterialTheme.shapes.medium
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CategoryIconView(
                                            iconUrl = item.iconUrl,
                                            iconName = item.icon,
                                            categoryName = item.name,
                                            size = 36.dp,
                                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            repeat(4 - categoryRow.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (state.leafCategories.isNotEmpty()) {
                val displayLeafCategories = remember(state.leafCategories) {
                    listOf(com.lianshan.lslife.core.model.CategoryNode(id = "all", name = "全部", icon = "all")) + state.leafCategories
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Dimens.lg),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimens.sm)
                ) {
                    lazyItems(displayLeafCategories) { item ->
                        val selected = state.selectedLeafCategory == item.id
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.onLeafCategory(item.id) },
                            label = { Text(item.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Row(
                        modifier = Modifier.padding(horizontal = Dimens.lg),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                    ) {
                        sorts.forEach { (id, name) ->
                            CategoryPill(
                                label = name,
                                selected = state.sort == id,
                                onClick = { viewModel.onSort(id) },
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        val activeFiltersCount = state.attributesFilter.size + 
                            (if (state.minPrice != null || state.maxPrice != null) 1 else 0) +
                            (if (state.publisherType != null) 1 else 0) +
                            (if (state.listingType != null) 1 else 0)
                        ElevatedFilterChip(
                            selected = activeFiltersCount > 0,
                            onClick = { viewModel.setShowFilterBottomSheet(true) },
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("筛选")
                                    if (activeFiltersCount > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                            Text(activeFiltersCount.toString(), color = MaterialTheme.colorScheme.onPrimary)
                                        }
                                    }
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.FilterList, contentDescription = "筛选", modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.elevatedFilterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                    Spacer(Modifier.height(Dimens.sm))
                }
            }

            when {
                state.loading && state.posts.isEmpty() -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = Dimens.lg, vertical = Dimens.md),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.listGap),
                        verticalItemSpacing = Dimens.listGap,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(6) {
                            SkeletonCard()
                        }
                    }
                }
                state.error != null && state.posts.isEmpty() -> {
                    ErrorBox(state.error!!, onRetry = { viewModel.load() })
                }
                else -> PullToRefreshBox(
                    isRefreshing = state.refreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        state = listState,
                        contentPadding = PaddingValues(
                            start = Dimens.lg,
                            end = Dimens.lg,
                            top = Dimens.sm,
                            bottom = Dimens.xl,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.listGap),
                        verticalItemSpacing = Dimens.listGap,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.posts, key = { it.id }) { post ->
                            val context = androidx.compose.ui.platform.LocalContext.current
                            PostListCard(
                                post = post,
                                onClick = { onOpenPost(post.id) },
                                onAddCartClick = {
                                    viewModel.addToCart(post.id)
                                    android.widget.Toast.makeText(context, "已加入购物车", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        
                        if (state.posts.isEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                EmptyState(
                                    title = "没有找到相关内容",
                                    subtitle = "试试看其他分类吧！",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(Dimens.xxl * 6),
                                )
                            }
                        }

                        if (state.loadingMore) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = Dimens.md),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                }
                            }
                        } else if (!state.hasMore && state.posts.isNotEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = Dimens.lg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "—— 到底了 ——",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
