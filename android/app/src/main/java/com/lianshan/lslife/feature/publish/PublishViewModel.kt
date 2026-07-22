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
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

import kotlinx.coroutines.Dispatchers
import java.io.File

data class PublishUiState(
    val publisherType: String = "INDIVIDUAL",
    val merchantId: String? = null,
    val listingType: String = "GOODS",
    val category: String = "second_hand",
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val images: List<String> = emptyList(),
    val brand: String = "未选择",
    val parameters: String = "",
    val purchaseDate: String = "",
    val condition: String = "全新",
    val shipping: String = "包邮",
    val location: String = "连山壮族瑶族自治县",
    val attr1Value: String = "",
    val attr2Value: String = "",
    val quota: Quota? = null,
    val submitting: Boolean = false,
    val message: String? = null,
    val success: Boolean = false,
    val editingPostId: String? = null,
)

@HiltViewModel
class PublishViewModel @Inject constructor(
    private val repo: LsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(PublishUiState())
    val state: StateFlow<PublishUiState> = _state

    fun loadQuota() {
        viewModelScope.launch {
            repo.quota().onSuccess { q -> _state.update { it.copy(quota = q) } }
        }
    }

    fun loadPost(id: String) {
        viewModelScope.launch {
            repo.post(id).onSuccess { post ->
                val attrs = post.attributes
                val config = publishCategoryConfigs.find { it.id == post.category } ?: publishCategoryConfigs.first()
                
                _state.update {
                    it.copy(
                        editingPostId = post.id,
                        publisherType = post.publisherType,
                        merchantId = post.merchantId,
                        listingType = post.listingType,
                        category = post.category,
                        title = post.title,
                        description = post.description,
                        price = post.price?.toString() ?: "",
                        images = post.images,
                        location = post.locationName ?: "连山壮族瑶族自治县",
                        brand = attrs["brand"] ?: "未选择",
                        parameters = attrs["parameters"] ?: "",
                        purchaseDate = attrs["purchaseDate"] ?: "",
                        condition = attrs["condition"] ?: "全新",
                        shipping = attrs["shipping"] ?: "包邮",
                        attr1Value = attrs[config.attr1Label ?: "attr1"] ?: "",
                        attr2Value = attrs[config.attr2Label ?: "attr2"] ?: "",
                    )
                }
            }
        }
    }

    fun onCategory(c: String) = _state.update { it.copy(category = c) }
    fun onTitle(v: String) = _state.update { it.copy(title = v) }
    fun onDescription(v: String) = _state.update { it.copy(description = v) }
    fun onPrice(v: String) = _state.update { it.copy(price = v.filter { c -> c.isDigit() || c == '.' }) }
    fun onImagesSelected(uris: List<String>) {
        val current = _state.value.images
        _state.update { it.copy(images = current + uris) }
    }
    fun removeImage(uri: String) = _state.update { it.copy(images = it.images - uri) }
    fun onBrand(b: String) = _state.update { it.copy(brand = b) }
    fun onParameters(p: String) = _state.update { it.copy(parameters = p) }
    fun onPurchaseDate(d: String) = _state.update { it.copy(purchaseDate = d) }
    fun onCondition(c: String) = _state.update { it.copy(condition = c) }
    fun onShipping(s: String) = _state.update { it.copy(shipping = s) }
    fun onLocation(l: String) = _state.update { it.copy(location = l) }
    
    fun onAttr1Value(a: String) = _state.update { it.copy(attr1Value = a) }
    fun onAttr2Value(a: String) = _state.update { it.copy(attr2Value = a) }
    fun onPublisherType(type: String, merchantId: String? = null) = _state.update { 
        it.copy(publisherType = type, merchantId = merchantId) 
    }
    fun onListingType(type: String) = _state.update { it.copy(listingType = type) }
    
    fun generateAiDescription() {
        val s = _state.value
        val hint = if (s.title.isNotBlank()) s.title else s.category
        val draft = s.description
        _state.update { it.copy(description = "正在 AI 优化中...") }
        viewModelScope.launch {
            repo.aiGenerateDescription(hint, s.category, draft).onSuccess { res ->
                val newTitle = res["title"] ?: ""
                val newDesc = res["description"] ?: ""
                val newBrand = res["brand"]?.takeIf { it.isNotBlank() } ?: s.brand
                val newParams = res["parameters"] ?: ""
                val newDate = res["purchaseDate"] ?: ""
                
                _state.update { 
                    it.copy(
                        title = newTitle.ifBlank { s.title },
                        description = newDesc.ifBlank { draft },
                        brand = newBrand,
                        parameters = newParams,
                        purchaseDate = newDate
                    ) 
                }
            }.onFailure {
                _state.update { it.copy(description = draft, message = "AI 优化失败") }
            }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null, success = false) }
    fun setMessage(msg: String) = _state.update { it.copy(message = msg) }

    fun submit() {
        val s = _state.value
        if (s.title.isBlank() || s.description.isBlank()) {
            _state.update { it.copy(message = "请填写标题和描述") }
            return
        }
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            
            // 并发上传图片，同时使用 ImageCompressor 进行压缩
            val uploadedUrls = try {
                coroutineScope {
                    s.images.map { uri ->
                        async {
                            if (uri.startsWith("http")) {
                                uri
                            } else {
                                val bytes = ImageCompressor.compress(context, uri)
                                val reqFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                                val part = MultipartBody.Part.createFormData("image", "upload.jpg", reqFile)
                                val res = repo.uploadImage(part)
                                if (res.isSuccess) {
                                    res.getOrNull()?.url ?: throw Exception("图片上传返回空地址")
                                } else {
                                    throw Exception(res.exceptionOrNull()?.message ?: "图片上传失败")
                                }
                            }
                        }
                    }.awaitAll()
                }
            } catch (e: Exception) {
                _state.update { it.copy(submitting = false, message = "图片上传失败: ${e.message}") }
                return@launch
            }
            
            val config = publishCategoryConfigs.find { it.id == s.category } ?: publishCategoryConfigs.first()
            val attributes = mutableMapOf<String, String>()
            
            if (config.guidedFill) {
                if (s.brand != "未选择") attributes["brand"] = s.brand
                if (s.parameters.isNotBlank()) attributes["parameters"] = s.parameters
                if (s.purchaseDate.isNotBlank()) attributes["purchaseDate"] = s.purchaseDate
                if (s.condition.isNotBlank()) attributes["condition"] = s.condition
            } else {
                if (s.attr1Value.isNotBlank()) attributes[config.attr1Label ?: "attr1"] = s.attr1Value
                if (s.attr2Value.isNotBlank()) attributes[config.attr2Label ?: "attr2"] = s.attr2Value
            }
            attributes["shipping"] = s.shipping

            val listingType = if (s.category in listOf("second_hand", "veggies")) "GOODS" else "SERVICE"

            val req = CreatePostRequest(
                category = s.category,
                title = s.title.ifBlank { "闲置好物" },
                description = s.description,
                price = s.price.toDoubleOrNull(),
                images = uploadedUrls,
                publisherType = s.publisherType,
                merchantId = s.merchantId,
                listingType = listingType,
                attributes = attributes,
                locationName = s.location,
            )
            
            val result = if (s.editingPostId != null) {
                repo.updatePost(s.editingPostId, req)
            } else {
                repo.createPost(req)
            }
            
            result.onSuccess {
                _state.update { PublishUiState(message = if (s.editingPostId != null) "修改成功，已提交审核" else "发布成功", success = true) }
                loadQuota()
            }.onFailure { e ->
                _state.update { it.copy(submitting = false, message = e.message ?: "发布失败") }
            }
        }
    }
}

