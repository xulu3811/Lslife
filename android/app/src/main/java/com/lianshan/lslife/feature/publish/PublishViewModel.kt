package com.lianshan.lslife.feature.publish

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.Quota
import com.lianshan.lslife.core.network.CreatePostRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublishUiState(
    val category: String = "second_hand",
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val images: List<Uri> = emptyList(),
    val brand: String = "未选择",
    val condition: String = "全新",
    val shipping: String = "包邮",
    val location: String = "连山壮族瑶族自治县",
    val quota: Quota? = null,
    val submitting: Boolean = false,
    val message: String? = null,
    val success: Boolean = false,
)

@HiltViewModel
class PublishViewModel @Inject constructor(
    private val repo: LsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PublishUiState())
    val state: StateFlow<PublishUiState> = _state

    fun loadQuota() {
        viewModelScope.launch {
            repo.quota().onSuccess { q -> _state.update { it.copy(quota = q) } }
        }
    }

    fun onCategory(c: String) = _state.update { it.copy(category = c) }
    fun onTitle(v: String) = _state.update { it.copy(title = v) }
    fun onDescription(v: String) = _state.update { it.copy(description = v) }
    fun onPrice(v: String) = _state.update { it.copy(price = v.filter { c -> c.isDigit() || c == '.' }) }
    fun onImagesSelected(uris: List<Uri>) = _state.update { it.copy(images = it.images + uris) }
    fun removeImage(uri: Uri) = _state.update { it.copy(images = it.images - uri) }
    fun onBrand(b: String) = _state.update { it.copy(brand = b) }
    fun onCondition(c: String) = _state.update { it.copy(condition = c) }
    fun onShipping(s: String) = _state.update { it.copy(shipping = s) }
    fun onLocation(l: String) = _state.update { it.copy(location = l) }
    
    fun generateAiDescription() {
        val s = _state.value
        val hint = if (s.title.isNotBlank()) s.title else s.category
        _state.update { it.copy(description = "正在 AI 生成中...") }
        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            _state.update { 
                it.copy(description = "我这里有一款非常不错的$hint，平时很爱惜，功能一切正常，配件齐全。\n因为近期用不上了，所以低价转让给有需要的朋友。\n\n支持验货，喜欢的话可以直接拍下或者私聊我！") 
            }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null, success = false) }

    fun submit() {
        val s = _state.value
        if (s.title.isBlank() || s.description.isBlank()) {
            _state.update { it.copy(message = "请填写标题和描述") }
            return
        }
        val extraInfo = buildString {
            if (s.brand != "未选择") append("【品牌】${s.brand}\n")
            if (s.condition.isNotBlank()) append("【成色】${s.condition}\n")
            append("【发货】${s.shipping}\n")
            append("【位置】${s.location}\n")
        }
        val finalDescription = if (extraInfo.isNotBlank()) "${s.description}\n\n$extraInfo" else s.description
        
        val stringImages = s.images.map { it.toString() }

        viewModelScope.launch {
            _state.update { it.copy(submitting = true) }
            repo.createPost(
                CreatePostRequest(
                    category = s.category,
                    title = s.title.ifBlank { "闲置好物" },
                    description = finalDescription,
                    price = s.price.toDoubleOrNull(),
                    images = stringImages,
                ),
            ).onSuccess {
                _state.update { PublishUiState(message = "发布成功", success = true) }
                loadQuota()
            }.onFailure { e ->
                _state.update { it.copy(submitting = false, message = e.message ?: "发布失败") }
            }
        }
    }
}
