package com.lianshan.lslife.feature.publish

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Constants are now imported from CategoryConfig.kt

@Composable
fun PublishScreen(
    viewModel: PublishViewModel = hiltViewModel(),
    onClose: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme

    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(9)) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                val absolutePaths = uris.mapNotNull { uri ->
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri) ?: return@mapNotNull null
                        val fileName = "publish_temp_${java.util.UUID.randomUUID()}.jpg"
                        val cacheFile = java.io.File(context.cacheDir, fileName)
                        cacheFile.outputStream().use { output ->
                            inputStream.copyTo(output)
                        }
                        cacheFile.absolutePath
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                withContext(Dispatchers.Main) {
                    if (absolutePaths.isNotEmpty()) {
                        viewModel.onImagesSelected(absolutePaths)
                    } else {
                        viewModel.setMessage("部分图片读取失败，请重试")
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) { viewModel.loadQuota() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
    
    LaunchedEffect(state.success) {
        if (state.success) {
            onClose() // Auto close on success
        }
    }

    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.White)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        modifier = Modifier.clickable { onClose() }
                    )
                    Text("发布信息", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("存草稿", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.clickable { })
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = viewModel::submit,
                        enabled = !state.submitting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD900), contentColor = Color.Black),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(if (state.submitting) "发布中" else "发布", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card 1: Images and Description
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Image Picker Row
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.images) { uri ->
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray)
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(20.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                            .clickable { viewModel.removeImage(uri) }
                                    )
                                }
                            }
                            if (state.images.size < 9) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF5F5F5))
                                            .clickable {
                                                pickMedia.launch(
                                                    androidx.activity.result.PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                                    )
                                                )
                                            },
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.Gray)
                                        Spacer(Modifier.height(4.dp))
                                        Text("添加优质\n首图更吸引人~", fontSize = 10.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        
                        // Title
                        BasicTextField(
                            value = state.title,
                            onValueChange = viewModel::onTitle,
                            textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = scheme.onSurface),
                            cursorBrush = SolidColor(scheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            decorationBox = { innerTextField ->
                                if (state.title.isEmpty()) {
                                    Text("填写商品名称会有更多赞哦~", color = Color.LightGray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                innerTextField()
                            }
                        )

                        HorizontalDivider(color = Color(0xFFF5F5F5))
                        Spacer(Modifier.height(12.dp))

                        // Description
                        BasicTextField(
                            value = state.description,
                            onValueChange = viewModel::onDescription,
                            textStyle = TextStyle(fontSize = 16.sp, color = scheme.onSurface),
                            cursorBrush = SolidColor(scheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            decorationBox = { innerTextField ->
                                if (state.description.isEmpty()) {
                                    Text("描述一下宝贝的品牌型号、货品来源...", color = Color.LightGray, fontSize = 16.sp)
                                }
                                innerTextField()
                            }
                        )

                        Spacer(Modifier.height(16.dp))

                        // AI Helper Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.generateAiDescription() }
                                .background(Color(0xFFF4F0FF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = "AI", tint = Color(0xFF673AB7), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("AI帮你写 >", fontSize = 12.sp, color = Color(0xFF673AB7), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Card 2: Selectors
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("分类/品牌/成色", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                        
                        val config = publishCategoryConfigs.find { it.id == state.category } ?: publishCategoryConfigs.first()
                        
                        // Category
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                            Text("分类", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.width(40.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(publishCategoryConfigs) { category ->
                                    PublishPill(
                                        label = category.name,
                                        selected = state.category == category.id,
                                        onClick = { viewModel.onCategory(category.id) }
                                    )
                                }
                            }
                        }

                        if (config.guidedFill) {
                            // Brand
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                                Text("品牌", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.width(40.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(secondHandBrandSuggestions) { brand ->
                                        PublishPill(
                                            label = brand,
                                            selected = state.brand == brand,
                                            onClick = { viewModel.onBrand(brand) }
                                        )
                                    }
                                }
                            }

                            // Condition
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("成色", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.width(40.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(secondHandConditionOptions) { condition ->
                                        PublishPill(
                                            label = condition,
                                            selected = state.condition == condition,
                                            onClick = { viewModel.onCondition(condition) }
                                        )
                                    }
                                }
                            }

                            // Parameters
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
                                Text("参数", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.width(40.dp))
                                BasicTextField(
                                    value = state.parameters,
                                    onValueChange = viewModel::onParameters,
                                    textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF7F7F7), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    decorationBox = { inner ->
                                        if (state.parameters.isEmpty()) Text("如：256G (AI 可自动提取)", color = Color.LightGray, fontSize = 13.sp)
                                        inner()
                                    }
                                )
                            }

                            // Purchase Date
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                                Text("购买日", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.width(40.dp))
                                BasicTextField(
                                    value = state.purchaseDate,
                                    onValueChange = viewModel::onPurchaseDate,
                                    textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF7F7F7), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    decorationBox = { inner ->
                                        if (state.purchaseDate.isEmpty()) Text("如：2023.5 (AI 可提取)", color = Color.LightGray, fontSize = 13.sp)
                                        inner()
                                    }
                                )
                            }
                        } else {
                            if (config.attr1Label != null) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                                    Text(config.attr1Label, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.width(40.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(config.attr1Options) { opt ->
                                            PublishPill(
                                                label = opt,
                                                selected = state.attr1Value == opt,
                                                onClick = { viewModel.onAttr1Value(opt) }
                                            )
                                        }
                                    }
                                }
                            }
                            if (config.attr2Label != null) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                                    Text(config.attr2Label, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.width(40.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(config.attr2Options) { opt ->
                                            PublishPill(
                                                label = opt,
                                                selected = state.attr2Value == opt,
                                                onClick = { viewModel.onAttr2Value(opt) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Card 3: Form Rows
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Price
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("价格", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BasicTextField(
                                    value = state.price,
                                    onValueChange = viewModel::onPrice,
                                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Red, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.End),
                                    decorationBox = { inner ->
                                        Row {
                                            Text("¥", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                            if (state.price.isEmpty()) {
                                                Text("0.00", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                            } else {
                                                inner()
                                            }
                                        }
                                    }
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.LightGray)
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF5F5F5))

                        // Shipping
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("发货方式", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(state.shipping, fontSize = 14.sp, color = Color.Gray)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.LightGray)
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF5F5F5))

                        // Location
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("所在位置", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(state.location, fontSize = 14.sp, color = Color.Gray)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.LightGray)
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PublishPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) Color(0xFFFFD900) else Color(0xFFF5F5F5)
    val fg = if (selected) Color.Black else Color.DarkGray
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = bg,
        contentColor = fg
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
