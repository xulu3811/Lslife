package com.lianshan.lslife.feature.publish

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.CategoryRepository
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.CategoryNode
import com.lianshan.lslife.core.model.DynamicField
import com.lianshan.lslife.core.model.Quota
import com.lianshan.lslife.core.network.CreatePostRequest
import com.lianshan.lslife.core.network.AiGenerateDescResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class PublishUiState(
    val publisherType: String = "INDIVIDUAL",
    val merchantId: String? = null,
    val listingType: String = "GOODS",
    
    // 分类树与动态 Schema
    val categoryTree: List<CategoryNode> = emptyList(),
    val loadingCategories: Boolean = false,
    val categoryError: String? = null,
    val selectedCategory: CategoryNode? = null,
    val selectedCategoryPath: String = "请选择分类",
    val categoryId: String = "second_hand",
    val requireCategorySelection: Boolean = false,
    val preSelectedLevel1Id: String? = null,
    val dynamicFields: List<DynamicField> = emptyList(),
    val dynamicFormValues: Map<String, String> = emptyMap(),

    val title: String = "",
    val description: String = "",
    val price: String = "",
    val images: List<String> = emptyList(),
    val location: String = "连山壮族瑶族自治县",
    val quota: Quota? = null,
    val submitting: Boolean = false,
    val aiOptimizing: Boolean = false,
    val message: String? = null,
    val success: Boolean = false,
    val editingPostId: String? = null,
)

@HiltViewModel
class PublishViewModel @Inject constructor(
    private val repo: LsRepository,
    private val categoryRepo: CategoryRepository,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val isEditMode = savedStateHandle.get<String>("postId")?.let { it.isNotBlank() && it != "{postId}" } == true
    private val initialCategoryId = savedStateHandle.get<String>("categoryId")?.takeIf { it.isNotBlank() && it != "{categoryId}" } ?: "second_hand"

    private val _state = MutableStateFlow(PublishUiState(categoryId = initialCategoryId))
    val state: StateFlow<PublishUiState> = _state

    init {
        observeCategories()
    }

    private fun observeCategories() {
        viewModelScope.launch {
            categoryRepo.getCategoryTree()
        }
        viewModelScope.launch {
            categoryRepo.categoryTree.collect { tree ->
                _state.update { it.copy(categoryTree = tree) }
                if (tree.isNotEmpty() && _state.value.selectedCategory == null) {
                    val initialId = _state.value.categoryId
                    val targetNodeAndPath = findExactNodeAndPath(tree, initialId)
                    if (targetNodeAndPath != null && targetNodeAndPath.first.isLeaf) {
                        onSelectLeafCategory(targetNodeAndPath.first, targetNodeAndPath.second)
                    } else if (!isEditMode) {
                        _state.update { it.copy(
                            requireCategorySelection = true, 
                            preSelectedLevel1Id = targetNodeAndPath?.first?.id
                        ) }
                    }
                }
            }
        }
        viewModelScope.launch {
            categoryRepo.loading.collect { loading ->
                _state.update { it.copy(loadingCategories = loading) }
            }
        }
        viewModelScope.launch {
            categoryRepo.error.collect { err ->
                _state.update { it.copy(categoryError = err) }
            }
        }
    }

    fun retryLoadCategories() {
        viewModelScope.launch {
            categoryRepo.getCategoryTree(forceRefresh = true)
        }
    }

    fun onCategorySelectionShown() = _state.update { it.copy(requireCategorySelection = false) }

    private fun findExactNodeAndPath(nodes: List<CategoryNode>, targetId: String, pathPrefix: String = ""): Pair<CategoryNode, String>? {
        for (node in nodes) {
            val currentPath = if (pathPrefix.isEmpty()) node.name else "$pathPrefix > ${node.name}"
            if (node.id == targetId) return node to currentPath
            if (node.children.isNotEmpty()) {
                val found = findExactNodeAndPath(node.children, targetId, currentPath)
                if (found != null) return found
            }
        }
        return null
    }

    fun loadQuota() {
        viewModelScope.launch {
            repo.quota().onSuccess { q -> _state.update { it.copy(quota = q) } }
        }
    }

    fun onSelectLeafCategory(leafNode: CategoryNode, fullPath: String) {
        _state.update {
            it.copy(
                selectedCategory = leafNode,
                selectedCategoryPath = fullPath,
                categoryId = leafNode.id,
                dynamicFields = leafNode.attributeSchema,
            )
        }
        if (leafNode.attributeSchema.isEmpty()) {
            viewModelScope.launch {
                categoryRepo.getCategorySchema(leafNode.id).onSuccess { res ->
                    if (res.attributeSchema.isNotEmpty()) {
                        _state.update { s -> s.copy(dynamicFields = res.attributeSchema) }
                    }
                }
            }
        }
    }

    fun onDynamicFieldValueChange(key: String, value: String) {
        _state.update { s ->
            s.copy(dynamicFormValues = s.dynamicFormValues + (key to value))
        }
    }

    fun loadPost(id: String) {
        if (id == "{postId}" || id.isBlank()) return
        viewModelScope.launch {
            repo.post(id).onSuccess { post ->
                val attrs = post.attributes.mapValues { (_, element) ->
                    if (element is kotlinx.serialization.json.JsonPrimitive) element.content else element.toString()
                }
                _state.update {
                    it.copy(
                        editingPostId = post.id,
                        publisherType = post.publisherType,
                        merchantId = post.merchantId,
                        listingType = post.listingType,
                        categoryId = post.category,
                        title = post.title,
                        description = post.description,
                        price = post.price?.toString() ?: "",
                        images = post.images,
                        location = post.locationName ?: "连山壮族瑶族自治县",
                        dynamicFormValues = attrs,
                    )
                }
                if (_state.value.categoryTree.isNotEmpty()) {
                    val found = categoryRepo.findLeafCategoryAndPath(_state.value.categoryTree, post.category)
                        ?: categoryRepo.findFirstLeafAndPath(_state.value.categoryTree)
                    if (found != null) {
                        onSelectLeafCategory(found.first, found.second)
                    }
                }
            }
        }
    }

    fun onTitle(v: String) = _state.update { it.copy(title = v) }
    fun onDescription(v: String) = _state.update { it.copy(description = v) }
    fun onPrice(v: String) = _state.update { it.copy(price = v.filter { c -> c.isDigit() || c == '.' }) }
    fun onImagesSelected(uris: List<String>) {
        val current = _state.value.images
        _state.update { it.copy(images = current + uris) }
    }
    fun removeImage(uri: String) = _state.update { it.copy(images = it.images - uri) }
    fun onLocation(l: String) = _state.update { it.copy(location = l) }
    fun onPublisherType(type: String, merchantId: String? = null) = _state.update { 
        it.copy(publisherType = type, merchantId = merchantId) 
    }
    fun onListingType(type: String) = _state.update { it.copy(listingType = type) }

    fun generateAiDescription() {
        val s = _state.value
        val hint = if (s.title.isNotBlank()) s.title else s.selectedCategoryPath
        val draft = s.description
        _state.update { it.copy(aiOptimizing = true) }
        viewModelScope.launch {
            repo.aiGenerateDescription(
                title = hint,
                categoryId = s.categoryId,
                draft = draft,
                schema = s.dynamicFields
            ).onSuccess { res: AiGenerateDescResponse ->
                val newTitle = res.title
                val newDesc = res.description
                val extractedAttrs = mutableMapOf<String, String>()
                
                s.dynamicFields.forEach { field ->
                    val element = res.attributes[field.key]
                    val extractedVal = if (element is kotlinx.serialization.json.JsonPrimitive) element.content else element?.toString() ?: ""
                    if (extractedVal.isNotBlank()) {
                        extractedAttrs[field.key] = extractedVal
                    }
                }

                _state.update {
                    it.copy(
                        aiOptimizing = false,
                        title = newTitle.ifBlank { s.title },
                        description = newDesc.ifBlank { draft },
                        dynamicFormValues = s.dynamicFormValues + extractedAttrs,
                        message = "AI 已自动优化文案并精细回填属性"
                    )
                }
            }.onFailure {
                _state.update { it.copy(aiOptimizing = false, message = "AI 优化失败") }
            }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null, success = false) }
    fun setMessage(msg: String) = _state.update { it.copy(message = msg) }

    fun submit() {
        val s = _state.value
        if (s.selectedCategory == null || !s.selectedCategory.isLeaf) {
            _state.update { it.copy(message = "请选择具体的最底层叶子类目后发布") }
            return
        }
        if (s.title.isBlank() || s.description.isBlank()) {
            _state.update { it.copy(message = "请填写标题和描述") }
            return
        }
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            val uploadedUrls = try {
                coroutineScope {
                    s.images.map { uri ->
                        async {
                            if (uri.startsWith("http")) {
                                uri
                            } else {
                                val bytes = ImageCompressor.compress(context, uri)
                                val reqFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                                val part = okhttp3.MultipartBody.Part.createFormData("image", "upload.jpg", reqFile)
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

            val isGoodsCategory = isPersonalIdleCategory(s.categoryId) ||
                s.categoryId == "veggies" ||
                s.categoryId == "veggies_fruit" ||
                s.listingType == "GOODS"
            val listingType = if (isGoodsCategory) "GOODS" else "SERVICE"

            val req = CreatePostRequest(
                category = s.categoryId,
                title = s.title.ifBlank { "同城优质发布" },
                description = s.description,
                price = s.price.toDoubleOrNull(),
                images = uploadedUrls,
                publisherType = s.publisherType,
                merchantId = s.merchantId,
                listingType = listingType,
                attributes = kotlinx.serialization.json.JsonObject(s.dynamicFormValues.mapValues { kotlinx.serialization.json.JsonPrimitive(it.value) }),
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
