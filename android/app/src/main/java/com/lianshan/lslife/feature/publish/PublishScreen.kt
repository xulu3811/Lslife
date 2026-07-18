package com.lianshan.lslife.feature.publish

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.ui.components.CategoryPill
import com.lianshan.lslife.ui.components.PrimaryButton
import com.lianshan.lslife.ui.components.SoftCard
import com.lianshan.lslife.ui.theme.Dimens

private val publishCategories = listOf(
    "job" to "招聘",
    "house" to "房租租售",
    "housekeeping" to "家政保洁",
    "maintenance" to "水电维修",
    "moving" to "货运搬家",
    "veggies" to "水果蔬菜",
    "secondhand" to "个人闲置",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(viewModel: PublishViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) { viewModel.loadQuota() }
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
                title = { Text("发布同城信息") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = scheme.surface),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            SoftCard {
                Column(Modifier.padding(Dimens.md), verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                    val quota = state.quota
                    val used = quota?.used ?: 0
                    val limit = (quota?.limit ?: 3).coerceAtLeast(1)
                    Text(
                        "本月发布额度",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "$used / $limit · ${quota?.tier ?: "free"} 会员",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                    LinearProgressIndicator(
                        progress = { used.toFloat() / limit.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        color = scheme.primary,
                        trackColor = scheme.surfaceVariant,
                    )
                }
            }

            Text("选择分类", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SoftCard {
                Column(Modifier.padding(Dimens.md)) {
                    // 使用 Flow 风格：两列网格按钮
                    publishCategories.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimens.xs),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                        ) {
                            row.forEach { (id, name) ->
                                CategoryPill(
                                    label = name,
                                    selected = state.category == id,
                                    onClick = { viewModel.onCategory(id) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            SoftCard {
                Column(Modifier.padding(Dimens.md), verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
                    Text("填写详情", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = viewModel::onTitle,
                        label = { Text("标题") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = fieldColors(),
                    )
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = viewModel::onDescription,
                        label = { Text("详细描述") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = fieldColors(),
                    )
                    OutlinedTextField(
                        value = state.price,
                        onValueChange = viewModel::onPrice,
                        label = { Text("价格/薪资（可选）") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = fieldColors(),
                    )
                }
            }

            PrimaryButton(
                text = "立即发布",
                onClick = viewModel::submit,
                enabled = !state.submitting,
                loading = state.submitting,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "发布内容将经过审核，请遵守社区规范，严禁违法违规信息。",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Dimens.lg))
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
)
