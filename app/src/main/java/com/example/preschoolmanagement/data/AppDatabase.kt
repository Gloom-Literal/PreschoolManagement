package com.example.preschoolmanagement.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.preschoolmanagement.data.dao.AttendanceDao
import com.example.preschoolmanagement.data.dao.ContractDao
import com.example.preschoolmanagement.data.dao.GroupDao
import com.example.preschoolmanagement.data.dao.ParentDao
import com.example.preschoolmanagement.data.dao.ParentStudentDao
import com.example.preschoolmanagement.data.dao.PaymentDao
import com.example.preschoolmanagement.data.dao.ScheduleDao
import com.example.preschoolmanagement.data.dao.StudentDao
import com.example.preschoolmanagement.data.dao.SubjectDao
import com.example.preschoolmanagement.data.dao.TeacherDao
import com.example.preschoolmanagement.data.model.AttendanceEntity
import com.example.preschoolmanagement.data.model.ContractEntity
import com.example.preschoolmanagement.data.model.GroupEntity
import com.example.preschoolmanagement.data.model.ParentEntity
import com.example.preschoolmanagement.data.model.ParentStudentEntity
import com.example.preschoolmanagement.data.model.PaymentEntity
import com.example.preschoolmanagement.data.model.ScheduleEntity
import com.example.preschoolmanagement.data.model.StudentEntity
import com.example.preschoolmanagement.data.model.SubjectEntity
import com.example.preschoolmanagement.data.model.TeacherEntity

@Database(
    entities = [
        GroupEntity::class,
        StudentEntity::class,
        TeacherEntity::class,
        SubjectEntity::class,
        ScheduleEntity::class,
        ParentEntity::class,
        ParentStudentEntity::class,
        ContractEntity::class,
        PaymentEntity::class,
        AttendanceEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun studentDao(): StudentDao
    abstract fun teacherDao(): TeacherDao
    abstract fun subjectDao(): SubjectDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun parentDao(): ParentDao
    abstract fun parentStudentDao(): ParentStudentDao
    abstract fun contractDao(): ContractDao
    abstract fun paymentDao(): PaymentDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "preschool.db"
                ).build().also { INSTANCE = it }
            }
    }
}
