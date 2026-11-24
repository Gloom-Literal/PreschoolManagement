package com.example.preschoolmanagement.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.preschoolmanagement.data.PreschoolRepository
import com.example.preschoolmanagement.data.SeedData
import com.example.preschoolmanagement.data.model.StudentEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    onBack: () -> Unit,
    viewModel: StudentsViewModel = viewModel()
) {
    val students by viewModel.students.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val query by viewModel.query.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<StudentEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<StudentDaoTarget?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Ученики", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Назад") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    "+",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        ) {
            // Поиск
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Поиск по ФИО") },
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Найдено: ${students.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(students, key = { it.student.studentId }) { item ->
                    ListCard(
                        title = "${item.student.lastName} ${item.student.firstName} ${item.student.middleName ?: ""}",
                        subtitle = "Группа: ${item.groupName} · Дата рождения: " +
                            PreschoolRepository.formatDate(item.student.dateOfBirth) +
                            (item.student.medicalInfo?.let { " · $it" } ?: "")
                    ) {
                        IconButton(onClick = { editTarget = item.student }) {
                            Text("✏", color = MaterialTheme.colorScheme.secondary)
                        }
                        IconButton(onClick = { deleteTarget = StudentDaoTarget(item.student.studentId, item.student.lastName) }) {
                            Text("🗑", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    message?.let {
        AlertDialog(
            onDismissRequest = { message = null },
            confirmButton = {
                TextButton(onClick = { message = null }) { Text("OK") }
            },
            title = { Text("Информация") },
            text = { Text(it) }
        )
    }

    editTarget?.let { student ->
        EditStudentDialog(
            student = student,
            groups = groups.map { it.groupId to it.groupName },
            onDismiss = { editTarget = null },
            onConfirm = { updated ->
                editTarget = null
                viewModel.editStudent(updated) { ok ->
                    message = if (ok) "Изменения сохранены" else "Ошибка сохранения"
                }
            }
        )
    }

    if (showAddDialog) {
        AddStudentDialog(
            groups = groups.map { it.groupId to it.groupName },
            onDismiss = { showAddDialog = false },
            onConfirm = { form ->
                showAddDialog = false
                viewModel.addStudent(
                    form.lastName, form.firstName, form.middleName.ifBlank { null },
                    SeedData.date(form.year, form.month, form.day),
                    form.groupId, form.medicalInfo.ifBlank { null },
                    form.selectedParentIds
                ) { ok ->
                    message = if (ok) "Ученик добавлен" else "Ошибка добавления ученика"
                }
            }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            confirmButton = {
                Button(
                    onClick = {
                        deleteTarget = null
                        viewModel.deleteStudent(target.id) { result ->
                            message = when (result) {
                                PreschoolRepositoryResult.SUCCESS -> "Ученик удалён"
                                PreschoolRepositoryResult.BLOCKED ->
                                    "Удаление заблокировано: у ученика есть договоры. Сначала удалите связанные договоры."
                                PreschoolRepositoryResult.ERROR -> "Ошибка удаления"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Отмена") }
            },
            title = { Text("Удаление ученика") },
            text = { Text("Удалить ученика ${target.name}? Связи с родителями будут удалены автоматически.") }
        )
    }
}

private data class StudentDaoTarget(val id: Long, val name: String)

/** Форма добавления ученика (аналог AddStudent из Desktop-версии). */
@Composable
private fun AddStudentDialog(
    groups: List<Pair<Long, String>>,
    onDismiss: () -> Unit,
    onConfirm: (StudentForm) -> Unit
) {
    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var medicalInfo by remember { mutableStateOf("") }
    var groupId by remember { mutableStateOf<Long?>(null) }
    var selectedParents by remember { mutableStateOf(setOf<Long>()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый ученик") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(lastName, { lastName = it }, label = { Text("Фамилия *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(firstName, { firstName = it }, label = { Text("Имя *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(middleName, { middleName = it }, label = { Text("Отчество") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(birthDate, { birthDate = it }, label = { Text("Дата рождения (ДД.ММ.ГГГГ) *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(medicalInfo, { medicalInfo = it }, label = { Text("Медицинские особенности") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Text("Группа *", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    Column {
                        groups.take((groups.size + 1) / 2).forEach { (id, name) ->
                            GroupChoice(name, groupId == id) { groupId = id }
                        }
                    }
                    Column {
                        groups.drop((groups.size + 1) / 2).forEach { (id, name) ->
                            GroupChoice(name, groupId == id) { groupId = id }
                        }
                    }
                }

                Text("Ошибка: $error", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.height(if (error == null) 0.dp else 20.dp))
            }
        },
        confirmButton = {
            Button(onClick = {
                // Валидация обязательных полей
                val parts = birthDate.split(".")
                val parsed = if (parts.size == 3) {
                    parts[0].toIntOrNull()?.let { d ->
                        parts[1].toIntOrNull()?.let { m ->
                            parts[2].toIntOrNull()?.let { y -> Triple(y, m, d) }
                        }
                    }
                } else null
                when {
                    lastName.isBlank() -> error = "Введите фамилию"
                    firstName.isBlank() -> error = "Введите имя"
                    parsed == null -> error = "Дата в формате ДД.ММ.ГГГГ"
                    parsed.third !in 1..31 || parsed.second !in 1..12 -> error = "Некорректная дата"
                    groupId == null -> error = "Выберите группу"
                    else -> {
                        error = null
                        onConfirm(
                            StudentForm(
                                lastName.trim(), firstName.trim(), middleName.trim(),
                                parsed.second, parsed.third, parsed.first,
                                groupId!!, medicalInfo.trim(), selectedParents.toList()
                            )
                        )
                    }
                }
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

data class StudentForm(
    val lastName: String,
    val firstName: String,
    val middleName: String,
    val month: Int,
    val day: Int,
    val year: Int,
    val groupId: Long,
    val medicalInfo: String,
    val selectedParentIds: List<Long>
)

@Composable
private fun EditStudentDialog(
    student: StudentEntity,
    groups: List<Pair<Long, String>>,
    onDismiss: () -> Unit,
    onConfirm: (StudentEntity) -> Unit
) {
    var lastName by remember { mutableStateOf(student.lastName) }
    var firstName by remember { mutableStateOf(student.firstName) }
    var middleName by remember { mutableStateOf(student.middleName ?: "") }
    var birthDate by remember { mutableStateOf(PreschoolRepository.formatDate(student.dateOfBirth)) }
    var medicalInfo by remember { mutableStateOf(student.medicalInfo ?: "") }
    var groupId by remember { mutableStateOf(student.groupId) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактирование ученика") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(lastName, { lastName = it }, label = { Text("Фамилия *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(firstName, { firstName = it }, label = { Text("Имя *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(middleName, { middleName = it }, label = { Text("Отчество") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(birthDate, { birthDate = it }, label = { Text("Дата рождения (ДД.ММ.ГГГГ) *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(medicalInfo, { medicalInfo = it }, label = { Text("Медицинские особенности") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("Группа *", style = MaterialTheme.typography.labelLarge)
                groups.forEach { (id, name) ->
                    Text(
                        text = if (groupId == id) "● $name" else name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (groupId == id) FontWeight.Bold else FontWeight.Normal,
                        color = if (groupId == id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { groupId = id }
                    )
                }
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                val parts = birthDate.split(".")
                val date = if (parts.size == 3) runCatching {
                    SeedData.date(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
                }.getOrNull() else null
                when {
                    lastName.isBlank() || firstName.isBlank() -> error = "Введите фамилию и имя"
                    date == null -> error = "Дата в формате ДД.ММ.ГГГГ"
                    else -> {
                        error = null
                        onConfirm(
                            student.copy(
                                lastName = lastName.trim(),
                                firstName = firstName.trim(),
                                middleName = middleName.trim().ifBlank { null },
                                dateOfBirth = date,
                                groupId = groupId,
                                medicalInfo = medicalInfo.trim().ifBlank { null }
                            )
                        )
                    }
                }
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
private fun GroupChoice(name: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = if (selected) "● $name" else name,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .clickable(onClick = onClick)
    )
}
