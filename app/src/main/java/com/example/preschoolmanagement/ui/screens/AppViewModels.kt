package com.example.preschoolmanagement.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.preschoolmanagement.PreschoolApp
import com.example.preschoolmanagement.data.PreschoolRepository
import com.example.preschoolmanagement.data.dao.AttendanceDao
import com.example.preschoolmanagement.data.dao.ContractDao
import com.example.preschoolmanagement.data.dao.GroupDao
import com.example.preschoolmanagement.data.dao.PaymentDao
import com.example.preschoolmanagement.data.dao.ScheduleDao
import com.example.preschoolmanagement.data.model.AttendanceEntity
import com.example.preschoolmanagement.data.model.GroupEntity
import com.example.preschoolmanagement.data.model.ParentEntity
import com.example.preschoolmanagement.data.model.StudentEntity
import com.example.preschoolmanagement.data.model.SubjectEntity
import com.example.preschoolmanagement.data.model.TeacherEntity
import com.example.preschoolmanagement.data.SeedData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private fun dbOf(application: Application) =
    (application as PreschoolApp).database

private fun repoOf(application: Application) =
    (application as PreschoolApp).repository

// ---------- Группы ----------

class GroupsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = dbOf(application)
    private val repo = repoOf(application)

    val groups: StateFlow<List<GroupDao.GroupWithCount>> =
        db.groupDao().getAllWithStudentCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGroup(name: String, ageCategory: String?, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val id = db.groupDao().insert(GroupEntity(groupName = name, ageCategory = ageCategory))
            onResult(id > 0)
        }
    }

    fun editGroup(group: GroupEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repo.updateGroup(group)
            onResult(true)
        }
    }

    fun deleteGroup(groupId: Long, onResult: (PreschoolRepositoryResult) -> Unit) {
        viewModelScope.launch {
            onResult(repo.deleteGroup(groupId).toResult())
        }
    }
}

// ---------- Педагоги ----------

class TeachersViewModel(application: Application) : AndroidViewModel(application) {
    private val db = dbOf(application)
    private val repo = repoOf(application)

    val teachers: StateFlow<List<TeacherEntity>> =
        db.teacherDao().getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTeacher(teacher: TeacherEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(db.teacherDao().insert(teacher) > 0)
        }
    }

    fun editTeacher(teacher: TeacherEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repo.updateTeacher(teacher)
            onResult(true)
        }
    }

    fun deleteTeacher(teacherId: Long, onResult: (PreschoolRepositoryResult) -> Unit) {
        viewModelScope.launch {
            onResult(repo.deleteTeacher(teacherId).toResult())
        }
    }
}

// ---------- Предметы ----------

class SubjectsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = dbOf(application)
    private val repo = repoOf(application)

    val subjects: StateFlow<List<SubjectEntity>> =
        db.subjectDao().getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSubject(name: String, description: String?, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(db.subjectDao().insert(SubjectEntity(subjectName = name, description = description)) > 0)
        }
    }

    fun editSubject(subject: SubjectEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repo.updateSubject(subject)
            onResult(true)
        }
    }

    fun deleteSubject(subjectId: Long, onResult: (PreschoolRepositoryResult) -> Unit) {
        viewModelScope.launch {
            onResult(repo.deleteSubject(subjectId).toResult())
        }
    }
}

// ---------- Расписание ----------

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val db = dbOf(application)
    private val repo = repoOf(application)

    val schedule: StateFlow<List<ScheduleDao.ScheduleItem>> =
        db.scheduleDao().getFullSchedule()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteItem(scheduleId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repo.deleteScheduleItem(scheduleId)
            onResult(true)
        }
    }
}

// ---------- Договоры ----------

class ContractsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = dbOf(application)
    private val repo = repoOf(application)

    val contracts: StateFlow<List<ContractDao.ContractInfo>> =
        db.contractDao().getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val students: StateFlow<List<StudentEntity>> =
        db.studentDao().getAllRaw()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val parents: StateFlow<List<ParentEntity>> =
        db.parentDao().getAllParents()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createContract(
        studentId: Long,
        parentId: Long,
        monthlyAmount: Double,
        months: Int,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val id = repo.createContractWithPayments(
                studentId, parentId, monthlyAmount,
                System.currentTimeMillis(), months
            )
            onResult(id > 0)
        }
    }

    fun deleteContract(contractId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repo.deleteContract(contractId)
            onResult(true)
        }
    }
}

// ---------- Платежи ----------

class PaymentsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = dbOf(application)
    private val repo = repoOf(application)

    val payments: StateFlow<List<PaymentDao.PaymentInfo>> =
        db.paymentDao().getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Автоматическое определение просрочек при открытии экрана
        viewModelScope.launch { repo.markOverduePayments() }
    }

    val contracts: StateFlow<List<ContractDao.ContractInfo>> =
        db.contractDao().getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Выставление счёта — ручной платёж «Ожидает» по договору. */
    fun addPayment(contractId: Long, amount: Double, date: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val id = repo.addPayment(contractId, amount, date)
            onResult(id > 0)
        }
    }

    fun markAsPaid(paymentId: Long) {
        viewModelScope.launch { repo.markPaymentAsPaid(paymentId) }
    }

    fun markOverdueNow() {
        viewModelScope.launch { repo.markOverduePayments() }
    }

    fun deletePayment(paymentId: Long) {
        viewModelScope.launch { repo.deletePayment(paymentId) }
    }
}

// ---------- Посещаемость ----------

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = dbOf(application)
    private val repo = repoOf(application)

    val attendance: StateFlow<List<AttendanceDao.AttendanceWithStudent>> =
        db.attendanceDao().getAllWithStudent()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val students: StateFlow<List<StudentEntity>> =
        db.studentDao().getAllRaw()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAttendance(
        studentId: Long, date: Long, isPresent: Boolean, reason: String?,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val id = repo.addAttendance(studentId, date, isPresent, reason)
            onResult(id > 0)
        }
    }

    fun deleteAttendance(attendanceId: Long) {
        viewModelScope.launch { repo.deleteAttendance(attendanceId) }
    }
}

private fun PreschoolRepository.DeleteResult.toResult(): PreschoolRepositoryResult = when (this) {
    PreschoolRepository.DeleteResult.DELETED -> PreschoolRepositoryResult.SUCCESS
    PreschoolRepository.DeleteResult.BLOCKED -> PreschoolRepositoryResult.BLOCKED
    PreschoolRepository.DeleteResult.ERROR -> PreschoolRepositoryResult.ERROR
}
