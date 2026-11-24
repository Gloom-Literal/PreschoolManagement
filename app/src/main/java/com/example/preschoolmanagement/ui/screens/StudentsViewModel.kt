package com.example.preschoolmanagement.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.preschoolmanagement.PreschoolApp
import com.example.preschoolmanagement.data.PreschoolRepository
import com.example.preschoolmanagement.data.dao.GroupDao
import com.example.preschoolmanagement.data.dao.StudentDao
import com.example.preschoolmanagement.data.model.GroupEntity
import com.example.preschoolmanagement.data.model.StudentEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudentsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as PreschoolApp).repository
    private val db = (application as PreschoolApp).database

    private val searchQuery = MutableStateFlow("")
    val query: StateFlow<String> = searchQuery.asStateFlow()

    val groups: StateFlow<List<GroupEntity>> = db.groupDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val students: StateFlow<List<StudentDao.StudentWithGroup>> =
        combine(searchQuery, db.studentDao().getAllWithGroup()) { q, list ->
            if (q.isBlank()) list
            else list.filter {
                it.student.lastName.contains(q, ignoreCase = true) ||
                    it.student.firstName.contains(q, ignoreCase = true) ||
                    (it.student.middleName?.contains(q, ignoreCase = true) ?: false)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(value: String) {
        searchQuery.value = value
    }

    fun addStudent(
        lastName: String,
        firstName: String,
        middleName: String?,
        dateOfBirth: Long,
        groupId: Long,
        medicalInfo: String?,
        parentIds: List<Long>,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val id = repo.addStudentWithParents(
                lastName, firstName, middleName, dateOfBirth, groupId, medicalInfo, parentIds
            )
            onResult(id > 0)
        }
    }

    /** Модификация записи ученика. */
    fun editStudent(student: StudentEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repo.updateStudent(student)
            onResult(true)
        }
    }

    fun deleteStudent(studentId: Long, onResult: (PreschoolRepositoryResult) -> Unit) {
        viewModelScope.launch {
            when (repo.deleteStudent(studentId)) {
                PreschoolRepository.DeleteResult.DELETED ->
                    onResult(PreschoolRepositoryResult.SUCCESS)
                PreschoolRepository.DeleteResult.BLOCKED ->
                    onResult(PreschoolRepositoryResult.BLOCKED)
                PreschoolRepository.DeleteResult.ERROR ->
                    onResult(PreschoolRepositoryResult.ERROR)
            }
        }
    }
}

/** Перечисление результатов операций для UI. */
enum class PreschoolRepositoryResult { SUCCESS, BLOCKED, ERROR }
