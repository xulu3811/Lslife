package com.lianshan.lslife.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lianshan.lslife.ui.components.PrimaryButton
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealNameAgreementScreen(
    onAgree: (String) -> Unit,
    onBack: () -> Unit
) {
    var agreed by remember { mutableStateOf(false) }
    var signature by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("实名认证免责协议", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.md)
        ) {
            Text(
                "为保障您的账户安全与平台规范，请仔细阅读以下协议，同意并签名后方可继续实名认证：",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.md))

            Surface(
                shape = RoundedCornerShape(Dimens.sm),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val scrollState = rememberScrollState()
                Text(
                    text = "甲方（本平台）与乙方（用户：包含商家和消费者）就实名认证及平台交易行为达成如下协议：\n\n" +
                            "1. 真实性承诺：乙方承诺所提供的实名认证信息（包括但不限于姓名、身份证号、照片等）真实、合法、有效。如因提供虚假信息导致的一切法律责任由乙方自行承担。\n" +
                            "2. 隐私保护：甲方承诺对乙方的实名信息严格保密，仅用于平台合规审查、交易纠纷处理及应国家执法机关要求配合调查，绝不泄露给任何无权第三方。\n" +
                            "3. 交易免责：乙方在平台内与他人达成的交易，属于双方自愿行为。甲方作为信息发布平台，不对任何私下交易、商品质量、款项安全承担担保责任。如发生纠纷，甲方仅提供有限的存证协查支持，乙方应自行评估交易风险并解决纠纷。\n" +
                            "4. 违规处置：如乙方利用平台从事非法活动，甲方有权随时封停其账号并向有关监管机关移交其保存的实名及交易电子存证。\n\n" +
                            "签署本协议即代表您充分理解并同意上述全部条款。",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(Dimens.md)
                        .verticalScroll(scrollState)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.md))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = agreed,
                    onCheckedChange = { agreed = it }
                )
                Text("我已仔细阅读并完全同意上述免责协议")
            }

            Spacer(modifier = Modifier.height(Dimens.sm))

            OutlinedTextField(
                value = signature,
                onValueChange = { signature = it },
                label = { Text("请输入真实姓名作为电子签名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Dimens.lg))

            PrimaryButton(
                text = "同意并继续",
                onClick = { onAgree(signature) },
                enabled = agreed && signature.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(Dimens.xl))
        }
    }
}
