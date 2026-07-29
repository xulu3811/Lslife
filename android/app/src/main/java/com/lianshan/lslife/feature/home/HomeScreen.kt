package com.lianshan.lslife.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.ui.components.BrandHero
import com.lianshan.lslife.ui.components.CategoryPill
import com.lianshan.lslife.ui.components.EmptyState
import com.lianshan.lslife.ui.components.ErrorBox
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.MerchantListCard
import com.lianshan.lslife.ui.components.PostListCard
import com.lianshan.lslife.ui.components.RecommendCard
import com.lianshan.lslife.ui.components.SectionHeader
import com.lianshan.lslife.ui.components.WarmSearchField
import com.lianshan.lslife.ui.components.SkeletonCard
import com.lianshan.lslife.ui.components.CategoryIconView
import com.lianshan.lslife.ui.theme.Dimens
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Badge
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.lianshan.lslife.feature.search.AdvancedFilterBottomSheet

private data class CategoryItem(
    val id: String,
    val name: String,
    val icon: String,
    val iconUrl: String? = null
)

private val defaultCategories = listOf(
    CategoryItem("cat_idle", "个人闲置", "shopping-bag", "/assets/icons/3d_flat_secondhand.png"),
    CategoryItem("cat_house", "房屋租售", "home", "/assets/icons/3d_flat_housing.png"),
    CategoryItem("cat_service", "家政保洁", "cleaning-services", "/assets/icons/3d_flat_cleaning.png"),
    CategoryItem("cat_maintenance", "水电维修", "build", "/assets/icons/3d_flat_repair.png"),
    CategoryItem("cat_veggies", "水果蔬菜", "shopping-basket", "/assets/icons/3d_flat_produce.png"),
    CategoryItem("cat_job", "招聘求职", "work", "/assets/icons/3d_flat_jobs.png"),
    CategoryItem("cat_car_rental", "租车服务", "local-shipping", "/assets/icons/3d_flat_car_rental.png"),
    CategoryItem("cat_part_time", "兼职零工", "schedule", "/assets/icons/3d_flat_parttime.png"),
)

private val sorts = listOf(
    "default" to "推荐",
    "latest" to "最新",
    "price_asc" to "价格最低",
    "price_desc" to "价格最高",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenMerchant: (String) -> Unit,
    onOpenPost: (String) -> Unit,
    onSearchClick: () -> Unit,
    onMessageClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            Toast.makeText(context, "扫码结果: ${result.contents}", Toast.LENGTH_LONG).show()
        }
    }
    val listState = rememberLazyListState()
    var selectedParentCategory by androidx.compose.runtime.remember { 
        androidx.compose.runtime.mutableStateOf<com.lianshan.lslife.core.model.CategoryNode?>(null) 
    }

    val displayCategories = androidx.compose.runtime.remember(state.categoryTree) {
        if (state.categoryTree.isEmpty()) {
            defaultCategories
        } else {
            state.categoryTree.map { node ->
                CategoryItem(
                    id = node.id,
                    name = node.name,
                    icon = node.icon ?: "📁",
                    iconUrl = node.iconUrl
                )
            }
        }
    }

    if (selectedParentCategory != null) {
        com.lianshan.lslife.ui.components.CategorySelectorBottomSheet(
            parentCategory = selectedParentCategory!!,
            onDismissRequest = { selectedParentCategory = null },
            onCategorySelected = { categoryId ->
                selectedParentCategory = null
                viewModel.onCategory(categoryId)
            }
        )
    }

    if (state.showFilterBottomSheet) {
        AdvancedFilterBottomSheet(
            schema = state.currentSchema,
            categoryTree = state.categoryTree,
            selectedCategory = state.category,
            publisherType = state.publisherType,
            listingType = state.listingType,
            minPrice = state.minPrice,
            maxPrice = state.maxPrice,
            attributesFilter = state.attributesFilter,
            onPublisherTypeChange = viewModel::updatePublisherType,
            onListingTypeChange = viewModel::updateListingType,
            onCategoryChange = { c -> viewModel.onCategory(c ?: "all") },
            onPriceChange = viewModel::updatePrice,
            onAttributeToggle = viewModel::updateAttributeFilter,
            onReset = viewModel::clearAttributesFilter,
            onDismiss = { viewModel.setShowFilterBottomSheet(false) },
            onConfirm = { viewModel.setShowFilterBottomSheet(false) }
        )
    }

    // Infinite scroll
    LaunchedEffect(listState, state.loading, state.loadingMore, state.hasMore) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null) {
                    val totalItems = listState.layoutInfo.totalItemsCount
                    // Load more when user scrolls to the last 3 items
                    if (totalItems - lastIndex <= 3 && state.hasMore && !state.loading && !state.loadingMore) {
                        viewModel.loadMore()
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 48.dp), // Increased top padding to avoid status bar overlap
    ) {
        Column(modifier = Modifier.padding(horizontal = Dimens.lg)) {
            // Location and Action Icons (Image 3 Style)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.sm), // Reduced bottom padding
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp) // Slightly smaller icon
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "连山壮族瑶族自治县 >",
                    style = MaterialTheme.typography.titleSmall, // Smaller font size
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = "消息",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp).clickable { onMessageClick() }
                )
                Spacer(modifier = Modifier.width(Dimens.md))
                Icon(
                    Icons.Filled.QrCodeScanner,
                    contentDescription = "扫一扫",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp).clickable {
                        val options = ScanOptions()
                        options.setPrompt("请将二维码置于框内")
                        options.setBeepEnabled(true)
                        scanLauncher.launch(options)
                    }
                )
            }
            
            // Search Bar (Image 3 Style with Image 1 colors)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable { onSearchClick() }, // Reduced from 48.dp
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = Dimens.md, end = 4.dp)
                ) {
                    Text(
                        text = "搜索本地商户、商品、服务",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onSearchClick,
                        modifier = Modifier.height(32.dp), // Reduced from 40.dp
                        shape = MaterialTheme.shapes.extraLarge,
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("搜索", style = MaterialTheme.typography.labelMedium) // Smaller label
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimens.sm)) // Reduced from lg
            
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.sm), // Reduced from md
                modifier = Modifier.padding(vertical = Dimens.sm)
            ) {
                displayCategories.chunked(4).forEach { categoryRow ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        categoryRow.forEach { item ->
                            val selected = state.category == item.id
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { 
                                        val node = state.categoryTree.find { it.id == item.id }
                                        if (node != null && node.children.isNotEmpty()) {
                                            selectedParentCategory = node
                                        } else {
                                            viewModel.onCategory(item.id)
                                        }
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                            shape = MaterialTheme.shapes.medium
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CategoryIconView(
                                        iconUrl = item.iconUrl,
                                        iconName = item.icon,
                                        size = 24.dp,
                                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.labelSmall,
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
            Spacer(modifier = Modifier.height(Dimens.lg))
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
                        (if (state.category != "all") 1 else 0) +
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
            state.loading && state.merchants.isEmpty() && state.posts.isEmpty() -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = Dimens.lg, vertical = Dimens.md),
                    verticalArrangement = Arrangement.spacedBy(Dimens.listGap),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(6) {
                        SkeletonCard()
                    }
                }
            }
            state.error != null && state.merchants.isEmpty() && state.posts.isEmpty() -> {
                ErrorBox(state.error!!, onRetry = { viewModel.load() })
            }
            else -> PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = Dimens.lg,
                        end = Dimens.lg,
                        top = Dimens.sm,
                        bottom = Dimens.xl,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.listGap),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (!state.isUgcMode && state.recommended.isNotEmpty()) {
                        item {
                            SectionHeader(title = "今日推荐")
                        }
                        item {
                            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
                                items(state.recommended) { m ->
                                    RecommendCard(m) { onOpenMerchant(m.id) }
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(Dimens.md))
                            Text(
                                "周边服务推荐",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(Dimens.sm))
                        }
                    }

                    if (state.isUgcMode) {
                        item {
                            Text(
                                if (state.category == "all" || state.category.isEmpty()) "最新同城动态" else "同城信息",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Spacer(modifier = Modifier.height(Dimens.sm))
                        }
                        items(state.posts, key = { it.id }) { post ->
                            com.lianshan.lslife.ui.components.PostListCard(post) { onOpenPost(post.id) }
                        }
                        if (state.posts.isEmpty()) {
                            item {
                                com.lianshan.lslife.ui.components.EmptyState(
                                    title = if (state.category == "all" || state.category.isEmpty()) "暂无同城推荐内容" else "还没有发布内容",
                                    subtitle = if (state.category == "all" || state.category.isEmpty()) "快去「发布」发一条闲置或同城动态吧！" else "去「发布」发一条同城动态吧",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(Dimens.xxl * 6),
                                )
                            }
                        }
                    } else {
                        items(state.merchants, key = { it.id }) { m ->
                            com.lianshan.lslife.ui.components.MerchantListCard(m) { onOpenMerchant(m.id) }
                        }
                        if (state.merchants.isEmpty()) {
                            item {
                                com.lianshan.lslife.ui.components.EmptyState(
                                    title = "没有找到匹配的商户",
                                    subtitle = "换个关键词或分类试试",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(Dimens.xxl * 6),
                                )
                            }
                        }
                    }

                    if (state.loadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Dimens.md),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    } else if (!state.hasMore && (!state.isUgcMode && state.merchants.isNotEmpty() || state.isUgcMode && state.posts.isNotEmpty())) {
                        item {
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
