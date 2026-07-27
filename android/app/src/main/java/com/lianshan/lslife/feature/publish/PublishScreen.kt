package com.lianshan.lslife.feature.publish

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.filled.Folder
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
import com.lianshan.lslife.core.model.CategoryNode
import com.lianshan.lslife.core.model.DynamicField
import androidx.compose.ui.text.style.TextOverflow
import com.lianshan.lslife.ui.components.CategoryIconView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(
    postId: String? = null,
    viewModel: PublishViewModel = hiltViewModel(),
    onClose: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme

    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var showCategoryBottomSheet by remember { mutableStateOf(false) }

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

    LaunchedEffect(Unit) {
        viewModel.loadQuota()
        if (!postId.isNullOrBlank() && postId != "{postId}") {
            viewModel.loadPost(postId)
        }
    }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
    
    LaunchedEffect(state.success) {
        if (state.success) {
            onClose()
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
                    Spacer(Modifier.width(12.dp))
                    Text("发布信息", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { viewModel.submit() },
                        enabled = !state.submitting,
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                        modifier = Modifier.height(36.dp)
                    ) {
                        if (state.submitting) {
                            CircularProgressIndicator(color = scheme.onPrimary, modifier = Modifier.size(18.dp))
                        } else {
                            val isEditMode = !postId.isNullOrBlank() && postId != "{postId}"
                            Text(if (isEditMode) "确认修改" else "确认发布", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quota Banner
            state.quota?.let { q ->
                val isUnlimited = q.limit >= 999999
                val limitText = if (isUnlimited) "不设限制" else "${q.limit}条"
                val isFull = !isUnlimited && q.used >= q.limit
                Surface(
                    color = if (isFull) Color(0xFFFFF0F0) else Color(0xFFFFF8E1),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isFull) "⚠️ 本月发布名额已满 (${q.used}/$limitText)，请升级会员或下月再发" 
                                   else "💡 本月发布名额：已发布 ${q.used} / 总量 $limitText",
                            fontSize = 13.sp,
                            color = if (isFull) Color(0xFFD32F2F) else Color(0xFFF57F17),
                            fontWeight = FontWeight.Medium
                        )
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
                // Card 1: Category Picker
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryBottomSheet = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Folder, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("所属分类", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = state.selectedCategoryPath,
                                fontSize = 14.sp,
                                color = if (state.selectedCategory != null) scheme.primary else Color.Gray,
                                fontWeight = if (state.selectedCategory != null) FontWeight.Bold else FontWeight.Normal
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.LightGray)
                        }
                    }
                }

                // Card 2: Images, Title and Description
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
                                        Text("添加图片", fontSize = 11.sp, color = Color.Gray)
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
                                    Text("填写吸引人的标题...", color = Color.LightGray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                            textStyle = TextStyle(fontSize = 15.sp, color = scheme.onSurface),
                            cursorBrush = SolidColor(scheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                            decorationBox = { innerTextField ->
                                if (state.description.isEmpty()) {
                                    Text("描述一下宝贝或服务的细节、成色、转手原因...", color = Color.LightGray, fontSize = 15.sp)
                                }
                                innerTextField()
                            }
                        )

                        Spacer(Modifier.height(12.dp))

                        // AI Helper Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable(enabled = !state.aiOptimizing) { viewModel.generateAiDescription() }
                                .background(Color(0xFFF4F0FF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            if (state.aiOptimizing) {
                                CircularProgressIndicator(color = Color(0xFF673AB7), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("DeepSeek 实体提取中...", fontSize = 12.sp, color = Color(0xFF673AB7), fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = "AI", tint = Color(0xFF673AB7), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("AI 智能补全与属性提取 >", fontSize = 12.sp, color = Color(0xFF673AB7), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Card 3: Dynamic Form Engine (Rendered from state.dynamicFields)
                if (state.dynamicFields.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                text = "规格属性 (${state.selectedCategory?.name ?: "已选类目"})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface
                            )

                            state.dynamicFields.forEach { field ->
                                DynamicFieldRenderer(
                                    field = field,
                                    currentValue = state.dynamicFormValues[field.key] ?: "",
                                    onValueChange = { newValue ->
                                        viewModel.onDynamicFieldValueChange(field.key, newValue)
                                    }
                                )
                            }
                        }
                    }
                }

                // Card 4: Price & Location
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
                            Text("价格/期望", fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF5F5F5))

                        // Location
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("发布位置", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(state.location, fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // Category Multi-level Cascading Picker (ModalBottomSheet)
    if (showCategoryBottomSheet) {
        CategoryTreeBottomSheet(
            categoryTree = state.categoryTree,
            isLoading = state.loadingCategories,
            error = state.categoryError,
            onRetry = { viewModel.retryLoadCategories() },
            onDismiss = { showCategoryBottomSheet = false },
            onSelectLeaf = { node, path ->
                viewModel.onSelectLeafCategory(node, path)
                showCategoryBottomSheet = false
            }
        )
    }
}

/** 动态表单引擎单字段渲染组件 */
@Composable
private fun DynamicFieldRenderer(
    field: DynamicField,
    currentValue: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
            Text(field.label, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.width(70.dp))
            
            when (field.fieldType) {
                "SELECT" -> {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(field.options) { option ->
                            val isSelected = currentValue == option
                            Surface(
                                modifier = Modifier.clickable { onValueChange(option) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFE53935) else Color(0xFFF5F5F5),
                                contentColor = if (isSelected) Color.White else Color.DarkGray
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
                else -> { // TEXT / NUMBER / DATE
                    BasicTextField(
                        value = currentValue,
                        onValueChange = onValueChange,
                        textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF7F7F7), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        decorationBox = { inner ->
                            if (currentValue.isEmpty()) {
                                Text(
                                    text = field.placeholder ?: "请输入${field.label}",
                                    color = Color.LightGray,
                                    fontSize = 13.sp
                                )
                            }
                            inner()
                        }
                    )
                }
            }
        }
    }
}

/** 多级分类级联选择 BottomSheet */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryTreeBottomSheet(
    categoryTree: List<CategoryNode>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onSelectLeaf: (CategoryNode, String) -> Unit
) {
    val publishableTree = remember(categoryTree) {
        categoryTree.filter { it.id != "all" }
    }

    var selectedLevel1 by remember { mutableStateOf<CategoryNode?>(publishableTree.firstOrNull()) }

    LaunchedEffect(publishableTree) {
        if (publishableTree.isNotEmpty() && (selectedLevel1 == null || publishableTree.none { it.id == selectedLevel1?.id })) {
            selectedLevel1 = publishableTree.firstOrNull()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "选择发布分类",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            when {
                isLoading && publishableTree.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(12.dp))
                            Text("正在同步全城分类目录...", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
                error != null && publishableTree.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error, fontSize = 14.sp, color = Color.Red)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = onRetry) {
                                Text("重新加载")
                            }
                        }
                    }
                }
                publishableTree.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无可用分类", fontSize = 14.sp, color = Color.Gray)
                    }
                }
                else -> {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Column 1: Level 1 Categories
                        LazyColumn(
                            modifier = Modifier
                                .width(130.dp)
                                .fillMaxHeight()
                                .background(Color(0xFFF7F8FA), RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                        ) {
                            items(publishableTree, key = { it.id }) { node ->
                                val isSelected = selectedLevel1?.id == node.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedLevel1 = node }
                                        .background(if (isSelected) Color.White else Color.Transparent)
                                        .padding(horizontal = 12.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CategoryIconView(
                                        iconUrl = node.iconUrl,
                                        iconName = node.icon,
                                        size = 20.dp,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = node.name,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Column 2: Level 2 & Leaf Categories
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val l2Nodes = selectedLevel1?.children.orEmpty()
                            items(l2Nodes, key = { it.id }) { l2 ->
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text(
                                        text = l2.name,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                                    )

                                    if (l2.isLeaf) {
                                        Card(
                                            onClick = {
                                                val path = "${selectedLevel1?.name ?: ""} > ${l2.name}"
                                                onSelectLeaf(l2, path)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 48.dp)
                                                .padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFE5E7EB))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    CategoryIconView(
                                                        iconUrl = l2.iconUrl,
                                                        iconName = l2.icon,
                                                        size = 18.dp,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(end = 8.dp)
                                                    )
                                                    Text(l2.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1F2937))
                                                }
                                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    } else {
                                        l2.children.forEach { leaf ->
                                            Card(
                                                onClick = {
                                                    val path = "${selectedLevel1?.name ?: ""} > ${l2.name} > ${leaf.name}"
                                                    onSelectLeaf(leaf, path)
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(min = 48.dp)
                                                    .padding(vertical = 4.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFE5E7EB))
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        CategoryIconView(
                                                            iconUrl = leaf.iconUrl,
                                                            iconName = leaf.icon,
                                                            size = 18.dp,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.padding(end = 8.dp)
                                                        )
                                                        Text(leaf.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1F2937))
                                                    }
                                                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

