package com.lianshan.lslife.feature.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.lianshan.lslife.core.model.Post
import com.lianshan.lslife.ui.components.CategoryIconView
import com.lianshan.lslife.ui.components.EmptyState
import com.lianshan.lslife.ui.theme.Dimens
import com.lianshan.lslife.ui.components.PriceText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    viewModel: DiscoverViewModel = hiltViewModel(),
    onNavigateToCategory: (String, String) -> Unit, // Kept for signature, but we navigate to post
    onSearchClick: () -> Unit,
    onOpenPost: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Search Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.md, start = Dimens.md, end = Dimens.md, bottom = Dimens.xs)
                .height(36.dp)
                .clickable { onSearchClick() },
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF5F6F8),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = Dimens.md)
            ) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "搜索本地商户、商品、服务",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Top Horizontal Categories (Level 1)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.sm),
            contentPadding = PaddingValues(horizontal = Dimens.md),
            horizontalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            itemsIndexed(state.topCategories) { index, category ->
                val selected = state.selectedTabIndex == index
                Column(
                    modifier = Modifier
                        .width(60.dp)
                        .clickable { viewModel.onTabSelected(index) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = if (selected) Color(0xFFE8F5E9) else Color.Transparent, // Light green hint
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CategoryIconView(
                            iconUrl = category.iconUrl,
                            iconName = category.icon,
                            categoryName = category.name,
                            size = 22.dp,
                            tint = if (selected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = category.name,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) Color.Black else Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFFF5F6F8), thickness = 1.dp)

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left Vertical Menu (Level 2)
            LazyColumn(
                modifier = Modifier
                    .width(86.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFF5F6F8))
            ) {
                val displaySubs = listOf(com.lianshan.lslife.core.model.CategoryNode(id = "all", name = "全部", icon = "all")) + state.subCategories
                items(displaySubs) { subCat ->
                    val selected = state.selectedSubCategory == subCat.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(if (selected) Color.White else Color.Transparent)
                            .clickable { viewModel.onSubCategory(subCat.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(16.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                                    .align(Alignment.CenterStart)
                            )
                        }
                        Text(
                            text = subCat.name,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Color.Black else Color.DarkGray
                        )
                    }
                }
            }

            // Right Vertical List (Posts)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.White),
                contentPadding = PaddingValues(start = Dimens.sm, end = Dimens.sm, top = Dimens.sm, bottom = Dimens.xxl * 2)
            ) {
                // Sorting Filters
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Dimens.sm),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterText(text = "推荐", selected = state.sort == "default") { viewModel.onSort("default") }
                        Spacer(modifier = Modifier.width(Dimens.md))
                        FilterText(text = "最新", selected = state.sort == "newest") { viewModel.onSort("newest") }
                        Spacer(modifier = Modifier.width(Dimens.md))
                        FilterText(text = "价格", selected = state.sort == "price_asc") { viewModel.onSort("price_asc") }
                    }
                }

                // Posts List
                if (state.loading && state.page == 1) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (state.posts.isEmpty()) {
                    item {
                        EmptyState(
                            title = "暂无相关商品或服务",
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    }
                } else {
                    items(state.posts) { post ->
                        O2OPostCard(post = post, onClick = { onOpenPost(post.id) })
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp, modifier = Modifier.padding(vertical = Dimens.xs))
                    }
                    
                    if (state.hasMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.md),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = { viewModel.loadMore() }) {
                                    Text(if (state.loadingMore) "加载中..." else "加载更多")
                                }
                            }
                        }
                    } else if (state.posts.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(Dimens.md),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("—— 到底了 ——", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterText(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun O2OPostCard(post: Post, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = Dimens.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image Left
        val imageUrl = post.images.firstOrNull()
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F6F8))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F6F8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = Color.LightGray)
            }
        }

        Spacer(modifier = Modifier.width(Dimens.sm))

        // Content Right
        Column(
            modifier = Modifier.weight(1f).heightIn(min = 80.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = post.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Publisher Info or simple tag
                Text(
                    text = post.user?.nickname ?: post.merchant?.name ?: "同城服务",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                PriceText(amount = post.price ?: 0.0)
                
                // + Button (O2O style)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
