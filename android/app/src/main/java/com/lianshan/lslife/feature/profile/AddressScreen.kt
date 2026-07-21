package com.lianshan.lslife.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.Address
import com.lianshan.lslife.core.network.AddressBody
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.PrimaryButton
import com.lianshan.lslife.ui.components.SoftCard
import com.lianshan.lslife.ui.components.StatusChip
import com.lianshan.lslife.ui.components.StatusTone
import com.lianshan.lslife.ui.theme.Dimens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private sealed interface AddressPage {
    data object List : AddressPage
    data class Edit(val address: Address?) : AddressPage
    data class Delete(val address: Address) : AddressPage
}

data class AddressUiState(
    val loading: Boolean = true,
    val addresses: List<Address> = emptyList(),
    val message: String? = null,
)

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val repo: LsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AddressUiState())
    val state: StateFlow<AddressUiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            repo.addresses()
                .onSuccess { list -> _state.update { it.copy(loading = false, addresses = list) } }
                .onFailure { _state.update { it.copy(loading = false, message = "加载地址失败") } }
        }
    }

    fun save(address: Address?, name: String, phone: String, detail: String, isDefault: Boolean) {
        viewModelScope.launch {
            val body = AddressBody(
                name = name.trim(),
                phone = phone.trim(),
                address = detail.trim(),
                isDefault = isDefault,
            )
            val result = if (address == null) {
                repo.addAddress(body)
            } else {
                repo.updateAddress(address.id, body)
            }
            result
                .onSuccess {
                    _state.update { it.copy(message = "地址已保存") }
                    load()
                }
                .onFailure { e ->
                    _state.update { it.copy(message = e.message ?: "保存失败") }
                }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repo.deleteAddress(id)
                .onSuccess {
                    _state.update { it.copy(message = "地址已删除") }
                    load()
                }
                .onFailure { e ->
                    _state.update { it.copy(message = e.message ?: "删除失败") }
                }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    onBack: () -> Unit,
    viewModel: AddressViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var page by remember { mutableStateOf<AddressPage>(AddressPage.List) }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    when (val current = page) {
        AddressPage.List -> AddressListPage(
            addresses = state.addresses,
            loading = state.loading,
            snackbar = snackbar,
            onBack = onBack,
            onAdd = { page = AddressPage.Edit(null) },
            onEdit = { page = AddressPage.Edit(it) },
            onDelete = { page = AddressPage.Delete(it) },
        )
        is AddressPage.Edit -> AddressEditPage(
            initial = current.address,
            onBack = { page = AddressPage.List },
            onSave = { name, phone, detail, isDefault ->
                viewModel.save(current.address, name, phone, detail, isDefault)
                page = AddressPage.List
            },
        )
        is AddressPage.Delete -> AddressDeletePage(
            address = current.address,
            onBack = { page = AddressPage.List },
            onConfirm = {
                viewModel.delete(current.address.id)
                page = AddressPage.List
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressListPage(
    addresses: List<Address>,
    loading: Boolean,
    snackbar: SnackbarHostState,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Address) -> Unit,
    onDelete: (Address) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("收货地址") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = "新增地址")
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (loading && addresses.isEmpty()) {
            LoadingBox(Modifier.padding(padding).fillMaxSize())
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
            contentPadding = PaddingValues(vertical = Dimens.md),
        ) {
            items(addresses, key = { it.id }) { address ->
                SoftCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    address.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (address.isDefault) StatusChip("默认", StatusTone.Success)
                            }
                            Text(
                                address.phone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(Dimens.xs))
                            Text(address.address, style = MaterialTheme.typography.bodyMedium)
                        }
                        Row {
                            IconButton(onClick = { onEdit(address) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { onDelete(address) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressEditPage(
    initial: Address?,
    onBack: () -> Unit,
    onSave: (name: String, phone: String, detail: String, isDefault: Boolean) -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var phone by remember { mutableStateOf(initial?.phone.orEmpty()) }
    var detail by remember { mutableStateOf(initial?.address.orEmpty()) }
    var isDefault by remember { mutableStateOf(initial?.isDefault ?: false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initial == null) "新增收货地址" else "编辑收货地址") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("收货人") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("手机号码") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = detail,
                onValueChange = { detail = it },
                label = { Text("详细地址") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("设为默认地址", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = isDefault, onCheckedChange = { isDefault = it })
            }
            Spacer(Modifier.height(Dimens.md))
            PrimaryButton(
                text = "保存地址",
                onClick = { onSave(name, phone, detail, isDefault) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && phone.isNotBlank() && detail.isNotBlank(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressDeletePage(
    address: Address,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("删除地址") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            Text(
                "确定删除以下收货地址？此操作不可恢复。",
                style = MaterialTheme.typography.bodyLarge,
            )
            SoftCard {
                Column(
                    modifier = Modifier.padding(Dimens.md),
                    verticalArrangement = Arrangement.spacedBy(Dimens.sm),
                ) {
                    Text("${address.name}  ${address.phone}", fontWeight = FontWeight.Bold)
                    Text(address.address)
                }
            }
            Spacer(Modifier.height(Dimens.md))
            PrimaryButton(text = "确定删除", onClick = onConfirm, modifier = Modifier.fillMaxWidth())
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeight),
            ) {
                Text("取消")
            }
        }
    }
}
