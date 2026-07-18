package com.lianshan.lslife.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.lianshan.lslife.ui.components.PrimaryButton
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealNameScreen(onBack: () -> Unit) {
    var realName by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("实名认证") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg)
        ) {
            Text(
                "为保障您的账户安全与平台规范，请提供真实的身份信息。我们承诺严格保密您的隐私数据。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = realName,
                onValueChange = { realName = it },
                label = { Text("真实姓名") },
                placeholder = { Text("请输入您的真实姓名") },
                leadingIcon = { Icon(Icons.Filled.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = idNumber,
                onValueChange = { idNumber = it },
                label = { Text("身份证号码") },
                placeholder = { Text("请输入18位身份证号") },
                leadingIcon = { Icon(Icons.Filled.Badge, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "提交认证",
                onClick = { /* 提交逻辑 */ },
                modifier = Modifier.fillMaxWidth(),
                enabled = realName.isNotBlank() && idNumber.length == 18
            )
            Spacer(Modifier.height(Dimens.lg))
        }
    }
}
