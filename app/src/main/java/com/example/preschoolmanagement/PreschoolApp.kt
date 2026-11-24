package com.example.preschoolmanagement

import android.app.Application
import com.example.preschoolmanagement.data.AppDatabase
import com.example.preschoolmanagement.data.PreschoolRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PreschoolApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: PreschoolRepository by lazy { PreschoolRepository(database) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Начальное заполнение БД (сид-данные, ≥ 10 записей на таблицу)
        applicationScope.launch { repository.seedIfEmpty() }
    }
}
