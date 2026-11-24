@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.preschoolmanagement.ui.screens

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
import com.example.preschoolmanagement.data.PreschoolRepository
import com.example.preschoolmanagement.data.SeedData
import com.example.preschoolmanagement.data.model.GroupEntity
import com.example.preschoolmanagement.data.model.SubjectEntity
import com.example.preschoolmanagement.data.model.TeacherEntity

/** Общая обёртка экрана списка с topbar и кнопкой «назад». */
@Composable
private fun ListScaffold(
    title: String,
    onBack: () -> Unit,
    fab: (@Composable () -> Unit)? = null,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Назад") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = { fab?.invoke() }
    ) { padding ->
        content(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        )
    }
}

@Composable
private fun ErrorDialog(message: String?, onDismiss: () -> Unit) {
    message?.let {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
            title = { Text("Информация") },
            text = { Text(it) }
        )
    }
}

@Composable
private fun DeleteDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onConfirm(); onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Удалить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        title = { Text(title) },
        text = { Text(text) }
    )
}

// ================= Группы =================

@Composable
fun GroupsScreen(onBack: () -> Unit, viewModel: GroupsViewModel = viewModel()) {
    val groups by viewModel.groups.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<GroupEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    ListScaffold("Группы", onBack, fab = {
        FloatingActionButton(containerColor = MaterialTheme.colorScheme.primary, onClick = { showAdd = true }) {
            Text("+", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimary)
        }
    }) { modifier ->
        Column(modifier) {
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(groups, key = { it.group.groupId }) { item ->
                    ListCard(
                        title = item.group.groupName,
                        subtitle = "Возраст: ${item.group.ageCategory ?: "—"} · Учеников: ${item.studentCount}"
                    ) {
                        TextButton(onClick = { editTarget = item.group }) {
                            Text("✏", color = MaterialTheme.colorScheme.secondary)
                        }
                        TextButton(onClick = { deleteTarget = item.group.groupId to item.group.groupName }) {
                            Text("🗑", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var age by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            confirmButton = {
                Button(onClick = {
                    if (name.isBlank()) return@Button
                    showAdd = false
                    viewModel.addGroup(name.trim(), age.trim().ifBlank { null }) {
                        message = if (it) "Группа добавлена" else "Ошибка добавления"
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Отмена") } },
            title = { Text("Новая группа") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Название *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(age, { age = it }, label = { Text("Возрастная категория") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            }
        )
    }

    editTarget?.let { group ->
        var name by remember(group) { mutableStateOf(group.groupName) }
        var age by remember(group) { mutableStateOf(group.ageCategory ?: "") }
        AlertDialog(
            onDismissRequest = { editTarget = null },
            title = { Text("Редактирование группы") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Название *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(age, { age = it }, label = { Text("Возрастная категория") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isBlank()) return@Button
                    editTarget = null
                    viewModel.editGroup(group.copy(groupName = name.trim(), ageCategory = age.trim().ifBlank { null })) {
                        message = if (it) "Изменения сохранены" else "Ошибка сохранения"
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { editTarget = null }) { Text("Отмена") } }
        )
    }

    deleteTarget?.let { (id, name) ->
        DeleteDialog(
            "Удаление группы",
            "Удалить группу «$name»? Группы с учениками удалить нельзя.",
            { viewModel.deleteGroup(id) { result -> message = result.toMessage("Группа удалена", "Группа содержит учеников — удаление заблокировано") } },
            { deleteTarget = null }
        )
    }
    ErrorDialog(message) { message = null }
}

// ================= Педагоги =================

@Composable
fun TeachersScreen(onBack: () -> Unit, viewModel: TeachersViewModel = viewModel()) {
    val teachers by viewModel.teachers.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<TeacherEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    ListScaffold("Педагоги", onBack, fab = {
        FloatingActionButton(containerColor = MaterialTheme.colorScheme.primary, onClick = { showAdd = true }) {
            Text("+", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimary)
        }
    }) { modifier ->
        Column(modifier) {
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(teachers, key = { it.teacherId }) { t ->
                    ListCard(
                        title = "${t.lastName} ${t.firstName} ${t.middleName ?: ""}",
                        subtitle = "${t.specialization} · ${t.salary.toInt()} руб. · ${t.phone}"
                    ) {
                        TextButton(onClick = { editTarget = t }) {
                            Text("✏", color = MaterialTheme.colorScheme.secondary)
                        }
                        TextButton(onClick = { deleteTarget = t.teacherId to t.lastName }) {
                            Text("🗑", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    editTarget?.let { teacher ->
        var specialization by remember(teacher) { mutableStateOf(teacher.specialization) }
        var salary by remember(teacher) { mutableStateOf(teacher.salary.toString()) }
        var phone by remember(teacher) { mutableStateOf(teacher.phone) }
        var address by remember(teacher) { mutableStateOf(teacher.address ?: "") }
        AlertDialog(
            onDismissRequest = { editTarget = null },
            title = { Text("Редактирование педагога: ${teacher.lastName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(specialization, { specialization = it }, label = { Text("Специализация *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(salary, { salary = it }, label = { Text("Оклад *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(phone, { phone = it }, label = { Text("Телефон *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(address, { address = it }, label = { Text("Адрес") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    val s = salary.toDoubleOrNull()
                    if (specialization.isBlank() || s == null || phone.isBlank()) return@Button
                    editTarget = null
                    viewModel.editTeacher(
                        teacher.copy(
                            specialization = specialization.trim(),
                            salary = s,
                            phone = phone.trim(),
                            address = address.trim().ifBlank { null }
                        )
                    ) { ok -> message = if (ok) "Изменения сохранены" else "Ошибка сохранения" }
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { editTarget = null }) { Text("Отмена") } }
        )
    }

    if (showAdd) {
        var lastName by remember { mutableStateOf("") }
        var firstName by remember { mutableStateOf("") }
        var middleName by remember { mutableStateOf("") }
        var specialization by remember { mutableStateOf("") }
        var hireDate by remember { mutableStateOf("") }
        var salary by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            confirmButton = {
                Button(onClick = {
                    val parts = hireDate.split(".")
                    val hire = if (parts.size == 3) runCatching {
                        SeedData.date(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
                    }.getOrNull() else null
                    when {
                        lastName.isBlank() || firstName.isBlank() -> error = "Введите ФИ и фамилию"
                        specialization.isBlank() -> error = "Введите специализацию"
                        hire == null -> error = "Дата найма в формате ДД.ММ.ГГГГ"
                        salary.toDoubleOrNull() == null -> error = "Введите оклад числом"
                        phone.isBlank() -> error = "Введите телефон"
                        else -> {
                            showAdd = false
                            viewModel.addTeacher(
                                TeacherEntity(
                                    lastName = lastName.trim(), firstName = firstName.trim(),
                                    middleName = middleName.trim().ifBlank { null },
                                    specialization = specialization.trim(), hireDate = hire,
                                    salary = salary.toDouble(), phone = phone.trim()
                                )
                            ) { ok -> message = if (ok) "Педагог добавлен" else "Ошибка добавления" }
                        }
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Отмена") } },
            title = { Text("Новый педагог") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(lastName, { lastName = it }, label = { Text("Фамилия *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(firstName, { firstName = it }, label = { Text("Имя *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(middleName, { middleName = it }, label = { Text("Отчество") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(specialization, { specialization = it }, label = { Text("Специализация *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(hireDate, { hireDate = it }, label = { Text("Дата найма (ДД.ММ.ГГГГ) *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(salary, { salary = it }, label = { Text("Оклад *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(phone, { phone = it }, label = { Text("Телефон *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        )
    }

    deleteTarget?.let { (id, name) ->
        DeleteDialog(
            "Удаление педагога",
            "Удалить педагога $name? Педагоги, занятые в расписании, не удаляются.",
            { viewModel.deleteTeacher(id) { result -> message = result.toMessage("Педагог удалён", "Педагог занят в расписании — удаление заблокировано") } },
            { deleteTarget = null }
        )
    }
    ErrorDialog(message) { message = null }
}

// ================= Предметы =================

@Composable
fun SubjectsScreen(onBack: () -> Unit, viewModel: SubjectsViewModel = viewModel()) {
    val subjects by viewModel.subjects.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<SubjectEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    ListScaffold("Предметы", onBack, fab = {
        FloatingActionButton(containerColor = MaterialTheme.colorScheme.primary, onClick = { showAdd = true }) {
            Text("+", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimary)
        }
    }) { modifier ->
        Column(modifier) {
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subjects, key = { it.subjectId }) { s ->
                    ListCard(
                        title = s.subjectName,
                        subtitle = s.description ?: ""
                    ) {
                        TextButton(onClick = { editTarget = s }) {
                            Text("✏", color = MaterialTheme.colorScheme.secondary)
                        }
                        TextButton(onClick = { deleteTarget = s.subjectId to s.subjectName }) {
                            Text("🗑", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    editTarget?.let { subject ->
        var name by remember(subject) { mutableStateOf(subject.subjectName) }
        var description by remember(subject) { mutableStateOf(subject.description ?: "") }
        AlertDialog(
            onDismissRequest = { editTarget = null },
            title = { Text("Редактирование предмета") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Название *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(description, { description = it }, label = { Text("Описание") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isBlank()) return@Button
                    editTarget = null
                    viewModel.editSubject(subject.copy(subjectName = name.trim(), description = description.trim().ifBlank { null })) {
                        message = if (it) "Изменения сохранены" else "Ошибка сохранения"
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { editTarget = null }) { Text("Отмена") } }
        )
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            confirmButton = {
                Button(onClick = {
                    if (name.isBlank()) return@Button
                    showAdd = false
                    viewModel.addSubject(name.trim(), description.trim().ifBlank { null }) {
                        message = if (it) "Предмет добавлен" else "Ошибка добавления"
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Отмена") } },
            title = { Text("Новый предмет") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Название *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(description, { description = it }, label = { Text("Описание") }, modifier = Modifier.fillMaxWidth())
                }
            }
        )
    }

    deleteTarget?.let { (id, name) ->
        DeleteDialog(
            "Удаление предмета",
            "Удалить предмет «$name»? Предметы из расписания удалить нельзя.",
            { viewModel.deleteSubject(id) { result -> message = result.toMessage("Предмет удалён", "Предмет используется в расписании — удаление заблокировано") } },
            { deleteTarget = null }
        )
    }
    ErrorDialog(message) { message = null }
}

// ================= Расписание =================

@Composable
fun ScheduleScreen(onBack: () -> Unit, viewModel: ScheduleViewModel = viewModel()) {
    val schedule by viewModel.schedule.collectAsState()

    ListScaffold("Расписание", onBack) { modifier ->
        Column(modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Занятий: ${schedule.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(schedule, key = { it.scheduleId }) { item ->
                    ListCard(
                        title = "${item.dayOfWeek}, ${item.startTime}–${item.endTime}",
                        subtitle = "${item.groupName} · ${item.subjectName} · ${item.teacherName}"
                    ) {
                        TextButton(onClick = { viewModel.deleteItem(item.scheduleId) {} }) {
                            Text("🗑", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

/** Преобразование результата удаления в сообщение для UI. */
fun PreschoolRepositoryResult.toMessage(success: String, blocked: String): String = when (this) {
    PreschoolRepositoryResult.SUCCESS -> success
    PreschoolRepositoryResult.BLOCKED -> blocked
    PreschoolRepositoryResult.ERROR -> "Ошибка операции"
}

@Composable
private fun FloatingActionButton(containerColor: androidx.compose.ui.graphics.Color, onClick: () -> Unit, content: @Composable () -> Unit) {
    androidx.compose.material3.FloatingActionButton(
        onClick = onClick,
        containerColor = containerColor,
        content = content
    )
}
