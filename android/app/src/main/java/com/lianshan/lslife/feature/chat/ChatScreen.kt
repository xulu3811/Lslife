package com.lianshan.lslife.feature.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import android.util.Base64
import android.util.Log
import androidx.compose.ui.res.painterResource
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.core.model.ChatMessage
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.theme.PrimaryRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    sessionId: String,
    targetUserId: String,
    targetName: String,
    initPostId: String? = null,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedMsgForRecall by remember { mutableStateOf<ChatMessage?>(null) }
    var isVoiceMode by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var isCancelled by remember { mutableStateOf(false) }
    val audioManager = remember { AudioManager(context) }
    
    val recordPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isVoiceMode = true
        } else {
            android.widget.Toast.makeText(context, "需要麦克风权限才能发送语音", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose { audioManager.release() }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.sendLocation(24.77, 112.08, "连山壮族瑶族自治县", "广东省清远市")
        } else {
            android.widget.Toast.makeText(context, "需要定位权限", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri) ?: return@launch
                    val fileName = "chat_temp_${java.util.UUID.randomUUID()}.jpg"
                    val cacheFile = java.io.File(context.cacheDir, fileName)
                    cacheFile.outputStream().use { output ->
                        inputStream.copyTo(output)
                    }
                    val absolutePath = cacheFile.absolutePath
                    withContext(Dispatchers.Main) {
                        viewModel.sendImage(absolutePath)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    LaunchedEffect(sessionId, targetUserId, initPostId) {
        viewModel.initSession(sessionId, targetUserId, initPostId)
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    if (selectedMsgForRecall != null) {
        AlertDialog(
            onDismissRequest = { selectedMsgForRecall = null },
            title = { Text("消息操作") },
            text = { Text("是否撤回这条消息？（仅支持撤回1分钟内发送的消息）") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedMsgForRecall?.let { viewModel.recallMessage(it.id) }
                        selectedMsgForRecall = null
                    }
                ) {
                    Text("撤回", color = PrimaryRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMsgForRecall = null }) {
                    Text("取消", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text(targetName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            pickMedia.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "发送图片", tint = Color.Gray)
                    }
                    IconButton(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                viewModel.sendLocation(24.77, 112.08, "连山壮族瑶族自治县", "广东省清远市")
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = "发送位置", tint = Color.Gray)
                    }
                    IconButton(
                        onClick = {
                            if (!isVoiceMode) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    isVoiceMode = true
                                } else {
                                    recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            } else {
                                isVoiceMode = false
                            }
                        }
                    ) {
                        Icon(
                            if (isVoiceMode) Icons.Filled.Keyboard else Icons.Filled.Mic,
                            contentDescription = "切换语音",
                            tint = Color.Gray
                        )
                    }

                    if (isVoiceMode) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        
                                        isRecording = true
                                        isCancelled = false
                                        down.consume()
                                        
                                        val startSuccess = audioManager.startRecording()
                                        if (startSuccess) {
                                            var pointerEvent = awaitPointerEvent()
                                            while (pointerEvent.changes.any { it.pressed }) {
                                                val change = pointerEvent.changes.first()
                                                val dragOffset = change.position.y - down.position.y
                                                isCancelled = dragOffset < -100f
                                                change.consume()
                                                pointerEvent = awaitPointerEvent()
                                            }
                                        }
                                        
                                        isRecording = false
                                        val result = audioManager.stopRecording(cancel = isCancelled)
                                        if (result != null && !isCancelled) {
                                            viewModel.sendVoice(result.first, result.second)
                                        } else if (isCancelled) {
                                            android.widget.Toast.makeText(context, "已取消发送", android.widget.Toast.LENGTH_SHORT).show()
                                        } else if (startSuccess) {
                                            android.widget.Toast.makeText(context, "录音太短", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                            color = if (isRecording) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = when {
                                        isCancelled -> "松开手指，取消发送"
                                        isRecording -> "松开 发送 / 上滑 取消"
                                        else -> "按住 说话"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.size(48.dp)) // Empty space for alignment
                    } else {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("发送消息...") },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                if (text.isNotBlank()) {
                                    viewModel.sendMessage(text)
                                    text = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(if (text.isNotBlank()) PrimaryRed else Color.LightGray, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "发送",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.loading && state.messages.isEmpty()) {
            LoadingBox(Modifier.padding(padding).fillMaxSize())
            return@Scaffold
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom),
            reverseLayout = true
        ) {
            items(state.messages.reversed()) { message ->
                val isMe = message.senderId == state.currentUserId
                if (message.type == "recalled" || message.isRecalled) {
                    RecalledMessagePill(text = message.content)
                } else {
                    ChatBubble(
                        message = message,
                        isMe = isMe,
                        audioManager = audioManager,
                        onLongPress = {
                            if (isMe && isWithinOneMinute(message.createdAt)) {
                                selectedMsgForRecall = message
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RecalledMessagePill(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color.Gray.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = text,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: ChatMessage,
    isMe: Boolean,
    audioManager: AudioManager,
    onLongPress: () -> Unit
) {
    val shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isMe) 16.dp else 4.dp,
        bottomEnd = if (isMe) 4.dp else 16.dp
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(color = if (isMe) PrimaryRed else MaterialTheme.colorScheme.surfaceVariant)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongPress
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            if (message.type == "image") {
                var model: Any = message.content
                if (message.content.startsWith("base64:")) {
                    try {
                        model = Base64.decode(message.content.removePrefix("base64:"), Base64.DEFAULT)
                    } catch (e: Exception) {
                        Log.e("ChatScreen", "Base64 decode failed", e)
                    }
                }
                coil.compose.AsyncImage(
                    model = model,
                    contentDescription = "图片",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .widthIn(max = 240.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    error = painterResource(id = android.R.drawable.stat_notify_error)
                )
            } else if (message.type == "post_card") {
                // Parse JSON
                val cardData = try {
                    org.json.JSONObject(message.content)
                } catch (e: Exception) {
                    null
                }
                if (cardData != null) {
                    Column(
                        modifier = Modifier.width(220.dp)
                    ) {
                        val imageUrl = cardData.optString("image")
                        if (imageUrl.isNotBlank()) {
                            coil.compose.AsyncImage(
                                model = imageUrl,
                                contentDescription = "商品缩略图",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(
                            text = cardData.optString("title"),
                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "¥${cardData.optDouble("price", 0.0)}",
                            color = if (isMe) Color.White else PrimaryRed,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                } else {
                    Text("不支持的卡片消息", color = Color.Gray)
                }
            } else if (message.type == "location") {
                val locData = try { org.json.JSONObject(message.content) } catch (e: Exception) { null }
                if (locData != null) {
                    Column(modifier = Modifier.width(220.dp)) {
                        Text(
                            text = locData.optString("name", "位置信息"),
                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = locData.optString("address", "未知地址"),
                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Place, contentDescription = "Map Placeholder", tint = PrimaryRed, modifier = Modifier.size(36.dp))
                        }
                    }
                } else {
                    Text("不支持的位置卡片", color = Color.Gray)
                }
            } else if (message.type == "voice") {
                val voiceData = try {
                    org.json.JSONObject(message.content)
                } catch (e: Exception) { null }
                if (voiceData != null) {
                    val duration = voiceData.optInt("duration", 1)
                    val url = voiceData.optString("url")
                    val currentPlayingUrl by audioManager.currentPlayingUrl.collectAsStateWithLifecycle()
                    val isPlaying by audioManager.isPlaying.collectAsStateWithLifecycle()
                    val isThisPlaying = (currentPlayingUrl == url && isPlaying)
                    
                    Row(
                        modifier = Modifier
                            .widthIn(min = 60.dp, max = (60 + duration * 5).dp)
                            .clickable { audioManager.playAudio(url) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        if (isMe) {
                            Text("${duration}\"", color = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                if (isThisPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                if (isThisPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = "Play",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${duration}\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    Text("语音格式错误", color = Color.Gray)
                }
            } else {
                Text(
                    text = message.content,
                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

private fun isWithinOneMinute(createdAtStr: String): Boolean {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = format.parse(createdAtStr) ?: return true
        val elapsed = System.currentTimeMillis() - date.time
        elapsed in 0..60_000
    } catch (e: Exception) {
        try {
            val format2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = format2.parse(createdAtStr) ?: return true
            val elapsed = System.currentTimeMillis() - date.time
            elapsed in 0..60_000
        } catch (e2: Exception) {
            true // 默认允许尝试撤回，由服务端最终校验
        }
    }
}
