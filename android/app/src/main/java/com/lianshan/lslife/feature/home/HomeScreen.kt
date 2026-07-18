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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.ui.components.BrandHero
import com.lianshan.lslife.ui.components.CategoryPill
import com.lianshan.lslife.ui.components.EmptyState
import com.lianshan.lslife.ui.components.ErrorBox
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.MerchantListCard
import com.lianshan.lslife.ui.components.RecommendCard
import com.lianshan.lslife.ui.components.SectionHeader
import com.lianshan.lslife.ui.components.WarmSearchField
import com.lianshan.lslife.ui.theme.Dimens

private data class CategoryItem(
    val id: String,
    val name: String,
    val icon: String
)

private val categories = listOf(
    CategoryItem("all", "全部", "✨"),
    CategoryItem("job", "招聘", "💼"),
    CategoryItem("house", "房租租售", "🏠"),
    CategoryItem("housekeeping", "家政保洁", "🧹"),
    CategoryItem("maintenance", "水电维修", "🔧"),
    CategoryItem("moving", "货运搬家", "🚚"),
    CategoryItem("veggies", "水果蔬菜", "🍎"),
    CategoryItem("secondhand", "个人闲置", "🛍️"),
)

private val sorts = listOf(
    "default" to "推荐",
    "distance" to "距离",
    "rating" to "评分",
    "sales" to "销量",
)

@Composable
fun HomeScreen(
    onOpenMerchant: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        BrandHero(
            title = "连山 · 壮瑶同城",
            subtitle = "本地生活服务 · 温暖直达",
        ) {
            WarmSearchField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = "搜索商户、商品、服务…",
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                Column(
                    modifier = Modifier.padding(
                        start = Dimens.sm,
                        end = Dimens.sm,
                        top = Dimens.md,
                        bottom = Dimens.sm,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.sm),
                ) {
                    categories.chunked(4).forEach { categoryRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            categoryRow.forEach { item ->
                                val selected = state.category == item.id
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.onCategory(item.id) }
                                        .padding(vertical = Dimens.xs),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = item.icon, fontSize = 24.sp)
                                    }
                                    Spacer(Modifier.height(Dimens.xs))
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.padding(horizontal = Dimens.lg),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                ) {
                    sorts.forEach { (id, name) ->
                        CategoryPill(
                            label = name,
                            selected = state.sort == id,
                            onClick = { viewModel.onSort(id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(Dimens.sm))
            }
        }

        when {
            state.loading -> LoadingBox()
            state.error != null -> ErrorBox(state.error!!, onRetry = { viewModel.load() })
            else -> LazyColumn(
                contentPadding = PaddingValues(
                    start = Dimens.lg,
                    end = Dimens.lg,
                    top = Dimens.sm,
                    bottom = Dimens.xl,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.listGap),
                modifier = Modifier.fillMaxSize(),
            ) {
                if (state.recommended.isNotEmpty()) {
                    item {
                        SectionHeader(title = "今日推荐")
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
                            items(state.recommended) { m ->
                                RecommendCard(m) { onOpenMerchant(m.id) }
                            }
                        }
                    }
                    item {
                        Spacer(Modifier.height(Dimens.xs))
                        Text(
                            "附近服务",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                items(state.merchants, key = { it.id }) { m ->
                    MerchantListCard(m) { onOpenMerchant(m.id) }
                }

                if (state.merchants.isEmpty()) {
                    item {
                        EmptyState(
                            title = "没有找到匹配的商户",
                            subtitle = "换个关键词或分类试试",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.xxl * 6),
                        )
                    }
                }
            }
        }
    }
}
