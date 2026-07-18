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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

private val categories = listOf(
    "all" to "全部",
    "job" to "招聘",
    "house" to "租售",
    "housekeeping" to "家政",
    "maintenance" to "维修",
    "moving" to "货运",
    "veggies" to "果蔬",
    "secondhand" to "闲置",
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
                        start = Dimens.lg,
                        end = Dimens.lg,
                        top = Dimens.md,
                        bottom = Dimens.sm,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.sm),
                ) {
                    categories.chunked(4).forEach { categoryRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                        ) {
                            categoryRow.forEach { (id, name) ->
                                CategoryPill(
                                    label = name,
                                    selected = state.category == id,
                                    onClick = { viewModel.onCategory(id) },
                                    modifier = Modifier.weight(1f),
                                )
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
