package com.example.preschoolmanagement.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.preschoolmanagement.PreschoolApp
import com.example.preschoolmanagement.data.PreschoolRepository
import com.example.preschoolmanagement.data.dao.ParentDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ParentsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as PreschoolApp).repository
    private val db = (application as PreschoolApp).database

    private val searchQuery = MutableStateFlow("")
    val query: StateFlow<String> = searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val parents: StateFlow<List<ParentDao.ParentWithChildren>> = searchQuery
        .flatMapLatest { q ->
            if (q.isBlank()) db.parentDao().getAllWithChildren()
            else db.parentDao().search(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(value: String) {
        searchQuery.value = value
    }

    /** Модификация записи родителя. */
    fun editParent(parent: com.example.preschoolmanagement.data.model.ParentEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repo.updateParent(parent)
            onResult(true)
        }
    }

    fun deleteParent(parentId: Long, onResult: (PreschoolRepositoryResult) -> Unit) {
        viewModelScope.launch {
            when (repo.deleteParent(parentId)) {
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
