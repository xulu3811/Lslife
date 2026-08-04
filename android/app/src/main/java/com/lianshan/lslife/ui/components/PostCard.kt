package com.lianshan.lslife.ui.components

import androidx.compose.runtime.Composable
import com.lianshan.lslife.core.model.Post
import com.lianshan.lslife.core.model.TradeMode

@Composable
fun PostListCard(
    post: Post,
    onClick: () -> Unit = {},
    onAddCartClick: () -> Unit = {},
    onPhoneClick: () -> Unit = onClick,
    onChatClick: () -> Unit = onClick
) {
    if (post.tradeMode == TradeMode.INFO_PUBLISH || post.tradeMode == TradeMode.INFO) {
        InfoPublishCard(
            post = post,
            onClick = onClick,
            onPhoneClick = onPhoneClick,
            onChatClick = onChatClick
        )
    } else {
        O2OProductCard(
            post = post,
            onClick = onClick,
            onAddCartClick = onAddCartClick
        )
    }
}
