@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.preschoolmanagement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.preschoolmanagement.data.model.PaymentStatus
import com.example.preschoolmanagement.ui.theme.StatusOverdue
import com.example.preschoolmanagement.ui.theme.StatusPaid
import com.example.preschoolmanagement.ui.theme.StatusPending

/** Цветной бейдж статуса платежа (палитра из DESIGN.md). */
@Composable
private fun StatusBadge(status: String) {
    val (bg, fg) = when (status) {
        PaymentStatus.PAID -> com.example.preschoolmanagement.ui.theme.PrimaryContainer to com.example.preschoolmanagement.ui.theme.OnPrimaryContainer
        PaymentStatus.OVERDUE -> com.example.preschoolmanagement.ui.theme.Error.copy(alpha = 0.15f) to StatusOverdue
        else -> com.example.preschoolmanagement.ui.theme.SecondaryContainer to StatusPending
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg
    ) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = fg,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Простой выбор из списка (две колонки кликабельных строк). */
@Composable
private fun PickerList(
    items: List<Pair<Long, String>>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    Row(Modifier.height(160.dp)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.take((items.size + 1) / 2).forEach { (id, label) ->
                PickerRow(label, selectedId == id) { onSelect(id) }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.drop((items.size + 1) / 2).forEach { (id, label) ->
                PickerRow(label, selectedId == id) { onSelect(id) }
            }
        }
    }
}

@Composable
private fun PickerRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = if (selected) "● $label" else label,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}

// ================= Договоры =================

@Composable
fun ContractsScreen(onBack: () -> Unit, viewModel: ContractsViewModel = viewModel()) {
    val contracts by viewModel.contracts.collectAsState()
    val students by viewModel.students.collectAsState()
    val parents by viewModel.parents.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    androidx.compose.material3.Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Договоры", fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Назад") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = { showAdd = true }
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(contracts, key = { it.contractId }) { c ->
                    ListCard(
                        title = "Договор №${c.contractId} — ${c.studentName}",
                        subtitle = "Плательщик: ${c.parentName} · ${PreschoolRepository.formatDate(c.contractDate)} · ${c.paymentAmount.toInt()} руб. · ${c.status}"
                    ) {
                        TextButton(onClick = { deleteTarget = c.contractId to "№${c.contractId}" }) {
                            Text("🗑", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        var studentId by remember { mutableStateOf<Long?>(null) }
        var parentId by remember { mutableStateOf<Long?>(null) }
        var monthly by remember { mutableStateOf("5000") }
        var months by remember { mutableStateOf("9") }
        var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Новый договор") },
            confirmButton = {
                Button(onClick = {
                    val m = monthly.toDoubleOrNull()
                    val n = months.toIntOrNull()
                    when {
                        studentId == null -> error = "Выберите ученика"
                        parentId == null -> error = "Выберите родителя"
                        m == null || m <= 0 -> error = "Введите сумму числом"
                        n == null || n !in 1..24 -> error = "Срок 1–24 месяца"
                        else -> {
                            showAdd = false
                            viewModel.createContract(studentId!!, parentId!!, m, n) { ok ->
                                message = if (ok) "Договор создан, платежный график сформирован" else "Ошибка создания договора"
                            }
                        }
                    }
                }) { Text("Создать") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Отмена") } },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ученик:", style = MaterialTheme.typography.labelLarge)
                    PickerList(students.map { it.studentId to "${it.lastName} ${it.firstName}" }, studentId) { studentId = it }
                    Text("Родитель:", style = MaterialTheme.typography.labelLarge)
                    PickerList(parents.map { it.parentId to "${it.lastName} ${it.firstName}" }, parentId) { parentId = it }
                    OutlinedTextField(monthly, { monthly = it }, label = { Text("Сумма в месяц, руб. *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(months, { months = it }, label = { Text("Срок, месяцев *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    val total = (monthly.toDoubleOrNull() ?: 0.0) * (months.toIntOrNull() ?: 0)
                    Text(
                        "Общая сумма: ${total.toInt()} руб.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        )
    }

    deleteTarget?.let { (id, name) ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            confirmButton = {
                Button(
                    onClick = { deleteTarget = null; viewModel.deleteContract(id) { message = "Договор удалён вместе с платежами" } },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Отмена") } },
            title = { Text("Удаление договора") },
            text = { Text("Удалить договор $name? Все связанные платежи будут удалены.") }
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
}

// ================= Платежи =================

@Composable
fun PaymentsScreen(onBack: () -> Unit, viewModel: PaymentsViewModel = viewModel()) {
    val payments by viewModel.payments.collectAsState()
    val contracts by viewModel.contracts.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    androidx.compose.material3.Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Платежи", fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Назад") } },
                actions = {
                    TextButton(onClick = { viewModel.markOverdueNow() }) { Text("Проверить просрочки") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = { showAdd = true }
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(payments, key = { it.paymentId }) { p ->
                    ListCard(
                        title = "${p.studentName} — ${p.paymentAmount.toInt()} руб.",
                        subtitle = "Договор №${p.contractId} · ${p.paymentPeriod} · срок: ${PreschoolRepository.formatDate(p.paymentDate)}"
                    ) {
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            StatusBadge(p.paymentStatus)
                            if (p.paymentStatus != PaymentStatus.PAID) {
                                TextButton(onClick = { viewModel.markAsPaid(p.paymentId) }) {
                                    Text("Оплатить", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        var contractId by remember { mutableStateOf<Long?>(null) }
        var amount by remember { mutableStateOf("5000") }
        var dateText by remember { mutableStateOf(PreschoolRepository.formatDate(System.currentTimeMillis())) }
        var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Выставить счёт на оплату") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Договор:", style = MaterialTheme.typography.labelLarge)
                    PickerList(
                        contracts.map { it.contractId to "№${it.contractId} ${it.studentName}" },
                        contractId
                    ) { contractId = it }
                    OutlinedTextField(amount, { amount = it }, label = { Text("Сумма, руб. *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(dateText, { dateText = it }, label = { Text("Дата счёта (ДД.ММ.ГГГГ) *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val a = amount.toDoubleOrNull()
                    val parts = dateText.split(".")
                    val date = if (parts.size == 3) runCatching {
                        SeedData.date(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
                    }.getOrNull() else null
                    when {
                        contractId == null -> error = "Выберите договор"
                        a == null || a <= 0 -> error = "Введите сумму числом"
                        date == null -> error = "Дата в формате ДД.ММ.ГГГГ"
                        else -> {
                            showAdd = false
                            viewModel.addPayment(contractId!!, a, date) { ok ->
                                message = if (ok) "Счёт выставлен" else "Ошибка выставления счёта"
                            }
                        }
                    }
                }) { Text("Выставить") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Отмена") } }
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
}

// ================= Посещаемость =================

@Composable
fun AttendanceScreen(onBack: () -> Unit, viewModel: AttendanceViewModel = viewModel()) {
    val attendance by viewModel.attendance.collectAsState()
    val students by viewModel.students.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    androidx.compose.material3.Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Посещаемость", fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Назад") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = { showAdd = true }
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(attendance, key = { it.attendance.attendanceId }) { item ->
                    val a = item.attendance
                    ListCard(
                        title = item.studentName,
                        subtitle = buildString {
                            append(PreschoolRepository.formatDate(a.date))
                            append(if (a.isPresent) " · Присутствовал" else " · Отсутствовал")
                            a.absenceReason?.let { append(" ($it)") }
                        }
                    ) {
                        TextButton(onClick = { viewModel.deleteAttendance(a.attendanceId) }) {
                            Text("🗑", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        var studentId by remember { mutableStateOf<Long?>(null) }
        var dateText by remember { mutableStateOf("") }
        var isPresent by remember { mutableStateOf(true) }
        var reason by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Отметка посещаемости") },
            confirmButton = {
                Button(onClick = {
                    val parts = dateText.split(".")
                    val date = if (parts.size == 3) runCatching {
                        SeedData.date(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
                    }.getOrNull() else null
                    when {
                        studentId == null -> error = "Выберите ученика"
                        date == null -> error = "Дата в формате ДД.ММ.ГГГГ"
                        else -> {
                            showAdd = false
                            viewModel.addAttendance(studentId!!, date, isPresent, reason.trim().ifBlank { null }) {
                                message = if (it) "Отметка добавлена" else "Ошибка добавления"
                            }
                        }
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Отмена") } },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ученик:", style = MaterialTheme.typography.labelLarge)
                    PickerList(students.map { it.studentId to "${it.lastName} ${it.firstName}" }, studentId) { studentId = it }
                    OutlinedTextField(dateText, { dateText = it }, label = { Text("Дата (ДД.ММ.ГГГГ) *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isPresent, onCheckedChange = { isPresent = it })
                        Text(if (isPresent) "Присутствовал" else "Отсутствовал")
                    }
                    if (!isPresent) {
                        OutlinedTextField(reason, { reason = it }, label = { Text("Причина отсутствия") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
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
}
