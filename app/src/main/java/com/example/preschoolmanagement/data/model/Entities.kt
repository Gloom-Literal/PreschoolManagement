package com.example.preschoolmanagement.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * Схема БД «Дошкольное учебное учреждение».
 * Источник схемы: .agent/ENTITIES.md (перенос с Desktop-версии).
 * Даты хранятся как Long (epoch millis).
 */

@Entity(tableName = "Group")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val groupId: Long = 0,
    val groupName: String,
    val ageCategory: String? = null
)

@Entity(
    tableName = "Student",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("groupId")]
)
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val studentId: Long = 0,
    val lastName: String,
    val firstName: String,
    val middleName: String? = null,
    val dateOfBirth: Long,
    val groupId: Long,
    val medicalInfo: String? = null,
    val admissionDate: Long? = null,
    val graduationDate: Long? = null
)

@Entity(tableName = "Teacher")
data class TeacherEntity(
    @PrimaryKey(autoGenerate = true) val teacherId: Long = 0,
    val lastName: String,
    val firstName: String,
    val middleName: String? = null,
    val specialization: String,
    val hireDate: Long,
    val salary: Double,
    val passportSeries: String? = null,
    val passportNumber: String? = null,
    val address: String? = null,
    val phone: String
)

@Entity(tableName = "Subject")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val subjectId: Long = 0,
    val subjectName: String,
    val description: String? = null
)

@Entity(
    tableName = "Schedule",
    foreignKeys = [
        ForeignKey(entity = GroupEntity::class, parentColumns = ["groupId"], childColumns = ["groupId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = SubjectEntity::class, parentColumns = ["subjectId"], childColumns = ["subjectId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = TeacherEntity::class, parentColumns = ["teacherId"], childColumns = ["teacherId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("groupId"), Index("subjectId"), Index("teacherId")]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val scheduleId: Long = 0,
    val groupId: Long,
    val subjectId: Long,
    val teacherId: Long,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String
)

@Entity(tableName = "Parent")
data class ParentEntity(
    @PrimaryKey(autoGenerate = true) val parentId: Long = 0,
    val lastName: String,
    val firstName: String,
    val middleName: String? = null,
    val passportSeries: String? = null,
    val passportNumber: String? = null,
    val phone: String,
    val address: String? = null
)

@Entity(
    tableName = "ParentStudent",
    foreignKeys = [
        ForeignKey(entity = StudentEntity::class, parentColumns = ["studentId"], childColumns = ["studentId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ParentEntity::class, parentColumns = ["parentId"], childColumns = ["parentId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("studentId"), Index("parentId")]
)
data class ParentStudentEntity(
    @PrimaryKey(autoGenerate = true) val parentStudentId: Long = 0,
    val studentId: Long,
    val parentId: Long,
    val relationshipType: String,
    val contactPriority: Int = 1
)

@Entity(
    tableName = "Contract",
    foreignKeys = [
        ForeignKey(entity = StudentEntity::class, parentColumns = ["studentId"], childColumns = ["studentId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = ParentEntity::class, parentColumns = ["parentId"], childColumns = ["parentId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("studentId"), Index("parentId")]
)
data class ContractEntity(
    @PrimaryKey(autoGenerate = true) val contractId: Long = 0,
    val studentId: Long,
    val parentId: Long,
    val contractDate: Long,
    val paymentAmount: Double,
    val status: String = ContractStatus.ACTIVE
)

object ContractStatus {
    const val ACTIVE = "Активен"
    const val FINISHED = "Завершён"
}

@Entity(
    tableName = "Payment",
    foreignKeys = [
        ForeignKey(
            entity = ContractEntity::class,
            parentColumns = ["contractId"],
            childColumns = ["contractId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contractId")]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val paymentId: Long = 0,
    val contractId: Long,
    val paymentAmount: Double,
    val paymentDate: Long,
    val paymentPeriod: String,
    val paymentStatus: String = PaymentStatus.PENDING
)

object PaymentStatus {
    const val PENDING = "Ожидает"
    const val PAID = "Оплачен"
    const val OVERDUE = "Просрочен"
}

@Entity(
    tableName = "Attendance",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId")]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val attendanceId: Long = 0,
    val studentId: Long,
    val date: Long,
    val isPresent: Boolean,
    val absenceReason: String? = null
)
