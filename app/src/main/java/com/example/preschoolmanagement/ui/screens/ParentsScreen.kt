package com.example.preschoolmanagement.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.preschoolmanagement.data.model.ParentEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentsScreen(
    onBack: () -> Unit,
    viewModel: ParentsViewModel = viewModel()
) {
    val parents by viewModel.parents.collectAsState()
    val query by viewModel.query.collectAsState()

    var editTarget by remember { mutableStateOf<ParentEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<ParentTarget?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Родители", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Назад") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Поиск по ФИО") },
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Найдено: ${parents.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(parents, key = { it.parent.parentId }) { item ->
                    val p = item.parent
                    ListCard(
                        title = "${p.lastName} ${p.firstName} ${p.middleName ?: ""}",
                        subtitle = buildString {
                            append("Тел: ${p.phone}")
                            item.children?.let { append("\nДети: $it") }
                        }
                    ) {
                        IconButton(onClick = { editTarget = p }) {
                            Text("✏", color = MaterialTheme.colorScheme.secondary)
                        }
                        IconButton(onClick = { deleteTarget = ParentTarget(p.parentId, "${p.lastName} ${p.firstName}") }) {
                            Text("🗑", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    editTarget?.let { parent ->
        var editLastName by remember(parent) { mutableStateOf(parent.lastName) }
        var editFirstName by remember(parent) { mutableStateOf(parent.firstName) }
        var editMiddleName by remember(parent) { mutableStateOf(parent.middleName ?: "") }
        var editPhone by remember(parent) { mutableStateOf(parent.phone) }
        var editAddress by remember(parent) { mutableStateOf(parent.address ?: "") }

        AlertDialog(
            onDismissRequest = { editTarget = null },
            title = { Text("Редактирование родителя") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(editLastName, { editLastName = it }, label = { Text("Фамилия *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(editFirstName, { editFirstName = it }, label = { Text("Имя *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(editMiddleName, { editMiddleName = it }, label = { Text("Отчество") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(editPhone, { editPhone = it }, label = { Text("Телефон *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(editAddress, { editAddress = it }, label = { Text("Адрес") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (editLastName.isBlank() || editFirstName.isBlank() || editPhone.isBlank()) return@Button
                    editTarget = null
                    viewModel.editParent(
                        parent.copy(
                            lastName = editLastName.trim(),
                            firstName = editFirstName.trim(),
                            middleName = editMiddleName.trim().ifBlank { null },
                            phone = editPhone.trim(),
                            address = editAddress.trim().ifBlank { null }
                        )
                    ) { ok -> message = if (ok) "Изменения сохранены" else "Ошибка сохранения" }
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { editTarget = null }) { Text("Отмена") } }
        )
    }

    message?.let {
        AlertDialog(
            onDismissRequest = { message = null },
            confirmButton = { TextButton(onClick = { message = null }) { Text("OK") } },
            title = { Text("Информация") },
            text = { Text(it) }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            confirmButton = {
                Button(
                    onClick = {
                        deleteTarget = null
                        viewModel.deleteParent(target.id) { result ->
                            message = when (result) {
                                PreschoolRepositoryResult.SUCCESS -> "Родитель удалён"
                                PreschoolRepositoryResult.BLOCKED ->
                                    "Удаление заблокировано: у родителя есть привязанные дети."
                                PreschoolRepositoryResult.ERROR -> "Ошибка удаления"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Отмена") } },
            title = { Text("Удаление родителя") },
            text = { Text("Удалить родителя ${target.name}?") }
        )
    }
}

private data class ParentTarget(val id: Long, val name: String)
