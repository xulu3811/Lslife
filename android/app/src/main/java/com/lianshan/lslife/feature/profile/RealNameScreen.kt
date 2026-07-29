package com.lianshan.lslife.feature.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.lianshan.lslife.ui.components.PrimaryButton
import com.lianshan.lslife.ui.theme.Dimens

fun validateIdCard(idCard: String): Boolean {
    if (idCard.length != 18) return false
    val weights = intArrayOf(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2)
    val checkCodes = charArrayOf('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2')
    var sum = 0
    for (i in 0 until 17) {
        val char = idCard[i]
        if (!char.isDigit()) return false
        sum += (char - '0') * weights[i]
    }
    val checkCode = checkCodes[sum % 11]
    val lastChar = idCard[17].uppercaseChar()
    return lastChar == checkCode
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealNameScreen(
    signature: String,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val user = state.user
    val isPending = user?.realNameStatus == "pending"
    val isVerified = user?.realNameStatus == "verified"

    var realName by remember { mutableStateOf(if (isVerified || isPending) user?.realName ?: "" else signature) }
    var idNumber by remember { mutableStateOf(if (isVerified || isPending) "*****************" else "") }
    
    var frontUri by remember { mutableStateOf<Uri?>(null) }
    var backUri by remember { mutableStateOf<Uri?>(null) }
    var handheldUri by remember { mutableStateOf<Uri?>(null) }
    
    var idError by remember { mutableStateOf(false) }

    val pickFrontMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { frontUri = it ?: frontUri }
    val pickBackMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { backUri = it ?: backUri }
    val pickHandheldMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { handheldUri = it ?: handheldUri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("实名资料填写") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(Dimens.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg)
        ) {
            if (isVerified) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Dimens.lg)) {
                        Text("认证状态：已实名", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("您已通过实名认证，可享受平台的完整功能。", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (isPending) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Dimens.lg)) {
                        Text("认证状态：人工审核中", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        Text("您提交的身份资料正在人工审核中，请耐心等待1-3个工作日。", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                Text(
                    "请完善您的身份信息并上传相应的照片证明。为确保验证通过，请保证照片清晰可见，无反光遮挡。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = realName,
                onValueChange = { realName = it },
                label = { Text("真实姓名") },
                placeholder = { Text("须与签名及身份证一致") },
                leadingIcon = { Icon(Icons.Filled.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = isVerified || isPending || signature.isNotBlank()
            )

            OutlinedTextField(
                value = idNumber,
                onValueChange = { 
                    idNumber = it
                    idError = if (it.length == 18) !validateIdCard(it) else false
                },
                label = { Text("身份证号码") },
                placeholder = { Text("请输入18位身份证号") },
                leadingIcon = { Icon(Icons.Filled.Badge, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = isVerified || isPending,
                isError = idError,
                supportingText = {
                    if (idError) {
                        Text("身份证格式错误，请检查校验位", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            if (!isVerified && !isPending) {
                
                // 正面
                ImagePickerBox(
                    label = "1. 身份证正面照 (必填)",
                    uri = frontUri,
                    onClick = { pickFrontMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                )
                
                // 反面
                ImagePickerBox(
                    label = "2. 身份证反面照 (必填)",
                    uri = backUri,
                    onClick = { pickBackMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                )

                // 手持
                ImagePickerBox(
                    label = "3. 手持身份证正面照 (必填)",
                    uri = handheldUri,
                    onClick = { pickHandheldMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                )

                Spacer(modifier = Modifier.height(Dimens.md))

                val canSubmit = realName == signature && realName.isNotBlank() && idNumber.length == 18 && !idError && frontUri != null && backUri != null && handheldUri != null && !state.realNameSubmitting
                
                PrimaryButton(
                    text = if (state.realNameSubmitting) "提交中..." else "提交实名认证审核",
                    onClick = { 
                        if (frontUri != null && backUri != null && handheldUri != null) {
                            viewModel.submitRealName(realName, idNumber, frontUri!!, backUri!!, handheldUri!!, context)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canSubmit
                )
                
                if (realName != signature) {
                    Text("提示：填写的姓名必须与上一页协议签名一致。", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                
                Spacer(Modifier.height(Dimens.lg))
            }
        }
    }
}

@Composable
fun ImagePickerBox(label: String, uri: Uri?, onClick: () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(Dimens.xs))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(Dimens.md))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            AsyncImage(
                model = uri,
                contentDescription = label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "上传照片", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(Dimens.sm))
                Text("点击拍摄/选择照片", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
