package com.example.preschoolmanagement.data

import androidx.room.withTransaction
import com.example.preschoolmanagement.data.model.AttendanceEntity
import com.example.preschoolmanagement.data.model.ContractEntity
import com.example.preschoolmanagement.data.model.ContractStatus
import com.example.preschoolmanagement.data.model.GroupEntity
import com.example.preschoolmanagement.data.model.SubjectEntity
import com.example.preschoolmanagement.data.model.TeacherEntity
import com.example.preschoolmanagement.data.model.PaymentEntity
import com.example.preschoolmanagement.data.model.PaymentStatus
import com.example.preschoolmanagement.data.model.ParentEntity
import com.example.preschoolmanagement.data.model.ParentStudentEntity
import com.example.preschoolmanagement.data.model.StudentEntity
import java.util.Calendar
import java.util.Locale

/**
 * Репозиторий — аналог сервисных классов Desktop-версии (StudentService,
 * ParentService, ContractService, PaymentService).
 * Бизнес-операции выполняются в транзакциях для обеспечения целостности данных.
 */
class PreschoolRepository(private val db: AppDatabase) {

    enum class DeleteResult { DELETED, BLOCKED, ERROR }

    // ---------- Начальное заполнение ----------

    suspend fun seedIfEmpty() {
        if (db.groupDao().count() > 0) return

        val groupIds = SeedData.groups.map { db.groupDao().insert(it) }

        // Распределяем 12 учеников по 10 группам
        val studentIds = SeedData.students.mapIndexed { i, s ->
            db.studentDao().insert(s.copy(groupId = groupIds[i % groupIds.size]))
        }

        val teacherIds = SeedData.teachers.map { db.teacherDao().insert(it) }
        val subjectIds = SeedData.subjects.map { db.subjectDao().insert(it) }

        SeedData.schedule.forEachIndexed { i, sch ->
            db.scheduleDao().insert(
                sch.copy(
                    groupId = groupIds[i % groupIds.size],
                    subjectId = subjectIds[i % subjectIds.size],
                    teacherId = teacherIds[i % teacherIds.size]
                )
            )
        }

        val parentIds = SeedData.parents.map { db.parentDao().insert(it) }

        SeedData.parentStudentRows.forEach { row ->
            db.parentStudentDao().insert(
                ParentStudentEntity(
                    studentId = studentIds[row.studentNo - 1],
                    parentId = parentIds[row.parentNo - 1],
                    relationshipType = row.relationshipType,
                    contactPriority = row.contactPriority
                )
            )
        }

        val contractIds = SeedData.contractRows.map { row ->
            db.contractDao().insert(
                ContractEntity(
                    studentId = studentIds[row.studentNo - 1],
                    parentId = parentIds[row.parentNo - 1],
                    contractDate = SeedData.date(
                        row.dateStr.substring(0, 4).toInt(),
                        row.dateStr.substring(5, 7).toInt(),
                        row.dateStr.substring(8, 10).toInt()
                    ),
                    paymentAmount = row.amount,
                    status = row.status
                )
            )
        }

        SeedData.paymentRows.forEach { row ->
            db.paymentDao().insert(
                PaymentEntity(
                    contractId = contractIds[row.contractNo - 1],
                    paymentAmount = row.amount,
                    paymentDate = SeedData.date(
                        row.dateStr.substring(0, 4).toInt(),
                        row.dateStr.substring(5, 7).toInt(),
                        row.dateStr.substring(8, 10).toInt()
                    ),
                    paymentPeriod = row.period,
                    paymentStatus = row.status
                )
            )
        }

        SeedData.attendanceRows.forEach { row ->
            db.attendanceDao().insert(
                AttendanceEntity(
                    studentId = studentIds[row.studentNo - 1],
                    date = SeedData.date(
                        row.dateStr.substring(0, 4).toInt(),
                        row.dateStr.substring(5, 7).toInt(),
                        row.dateStr.substring(8, 10).toInt()
                    ),
                    isPresent = row.present,
                    absenceReason = row.reason
                )
            )
        }
    }

    // ---------- Справочники (группы, педагоги, предметы) ----------

    /** Удаление группы: блокируется, если в ней есть ученики. */
    suspend fun deleteGroup(groupId: Long): DeleteResult {
        if (db.groupDao().studentCount(groupId) > 0) return DeleteResult.BLOCKED
        return try {
            db.withTransaction { db.groupDao().deleteById(groupId) }
            DeleteResult.DELETED
        } catch (e: Exception) {
            DeleteResult.ERROR
        }
    }

    /** Удаление педагога: блокируется, если он занят в расписании. */
    suspend fun deleteTeacher(teacherId: Long): DeleteResult {
        if (db.teacherDao().scheduleCount(teacherId) > 0) return DeleteResult.BLOCKED
        return try {
            db.withTransaction { db.teacherDao().deleteById(teacherId) }
            DeleteResult.DELETED
        } catch (e: Exception) {
            DeleteResult.ERROR
        }
    }

    /** Удаление предмета: блокируется, если он есть в расписании. */
    suspend fun deleteSubject(subjectId: Long): DeleteResult {
        if (db.subjectDao().scheduleCount(subjectId) > 0) return DeleteResult.BLOCKED
        return try {
            db.withTransaction { db.subjectDao().deleteById(subjectId) }
            DeleteResult.DELETED
        } catch (e: Exception) {
            DeleteResult.ERROR
        }
    }

    suspend fun addAttendance(studentId: Long, date: Long, isPresent: Boolean, reason: String?): Long =
        db.attendanceDao().insert(
            AttendanceEntity(studentId = studentId, date = date, isPresent = isPresent, absenceReason = reason)
        )

    suspend fun deleteAttendance(attendanceId: Long) = db.attendanceDao().deleteById(attendanceId)

    suspend fun deleteContract(contractId: Long) = db.contractDao().deleteById(contractId)

    suspend fun deletePayment(paymentId: Long) = db.paymentDao().deleteById(paymentId)

    suspend fun deleteScheduleItem(scheduleId: Long) = db.scheduleDao().deleteById(scheduleId)

    // ---------- Модификация записей (update) ----------

    suspend fun updateStudent(student: StudentEntity) = db.studentDao().update(student)
    suspend fun updateParent(parent: ParentEntity) = db.parentDao().update(parent)
    suspend fun updateTeacher(teacher: TeacherEntity) = db.teacherDao().update(teacher)
    suspend fun updateSubject(subject: SubjectEntity) = db.subjectDao().update(subject)
    suspend fun updateGroup(group: GroupEntity) = db.groupDao().update(group)

    /**
     * Выставление счёта — ручное создание платежа «Ожидает» по договору.
     */
    suspend fun addPayment(contractId: Long, amount: Double, date: Long): Long =
        db.paymentDao().insert(
            PaymentEntity(
                contractId = contractId,
                paymentAmount = amount,
                paymentDate = date,
                paymentPeriod = monthYear(date),
                paymentStatus = PaymentStatus.PENDING
            )
        )

    suspend fun markPaymentAsPaid(paymentId: Long) =
        db.paymentDao().updateStatus(paymentId, PaymentStatus.PAID)

    suspend fun markOverduePayments() = db.paymentDao().markOverdue(System.currentTimeMillis())

    // ---------- Ученики ----------

    /**
     * Добавление ученика с привязкой к родителям (аналог процесса из
     * Desktop-версии). Транзакция: при ошибке — полный откат.
     */
    suspend fun addStudentWithParents(
        lastName: String,
        firstName: String,
        middleName: String?,
        dateOfBirth: Long,
        groupId: Long,
        medicalInfo: String?,
        parentIds: List<Long>,
        relationshipType: String = "Родитель"
    ): Long = db.withTransaction {
        val studentId = db.studentDao().insert(
            StudentEntity(
                lastName = lastName, firstName = firstName, middleName = middleName,
                dateOfBirth = dateOfBirth, groupId = groupId, medicalInfo = medicalInfo,
                admissionDate = System.currentTimeMillis()
            )
        )
        parentIds.forEach { parentId ->
            db.parentStudentDao().insert(
                ParentStudentEntity(
                    studentId = studentId, parentId = parentId,
                    relationshipType = relationshipType, contactPriority = 1
                )
            )
        }
        studentId
    }

    /**
     * Удаление ученика. При наличии договоров удаление блокируется
     * (аналог проверки целостности в Desktop-версии).
     */
    suspend fun deleteStudent(studentId: Long): DeleteResult {
        if (db.contractDao().countForStudent(studentId) > 0) {
            return DeleteResult.BLOCKED
        }
        return try {
            db.withTransaction { db.studentDao().deleteById(studentId) }
            DeleteResult.DELETED
        } catch (e: Exception) {
            DeleteResult.ERROR
        }
    }

    // ---------- Родители ----------

    /** Удаление родителя только при отсутствии связей с учениками (блокировка). */
    suspend fun deleteParent(parentId: Long): DeleteResult {
        if (db.parentDao().childrenCount(parentId) > 0) {
            return DeleteResult.BLOCKED
        }
        return try {
            db.withTransaction { db.parentDao().deleteById(parentId) }
            DeleteResult.DELETED
        } catch (e: Exception) {
            DeleteResult.ERROR
        }
    }

    // ---------- Договоры и платежи ----------

    /**
     * Создание договора + генерация платежного графика.
     * Полностью транзакционная операция (аналог ContractService.CreateContract +
     * PaymentService.GeneratePayments из Desktop-версии).
     */
    suspend fun createContractWithPayments(
        studentId: Long,
        parentId: Long,
        monthlyAmount: Double,
        startDate: Long,
        months: Int
    ): Long = db.withTransaction {
        val contractId = db.contractDao().insert(
            ContractEntity(
                studentId = studentId, parentId = parentId,
                contractDate = startDate,
                paymentAmount = monthlyAmount * months,
                status = ContractStatus.ACTIVE
            )
        )
        repeat(months) { i ->
            val date = addMonths(startDate, i)
            db.paymentDao().insert(
                PaymentEntity(
                    contractId = contractId,
                    paymentAmount = monthlyAmount,
                    paymentDate = date,
                    paymentPeriod = monthYear(date),
                    paymentStatus = PaymentStatus.PENDING
                )
            )
        }
        contractId
    }

    companion object {
        fun addMonths(epochMillis: Long, months: Int): Long {
            val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
            cal.add(Calendar.MONTH, months)
            return cal.timeInMillis
        }

        fun monthYear(epochMillis: Long): String {
            val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
            val months = arrayOf(
                "январь", "февраль", "март", "апрель", "май", "июнь",
                "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"
            )
            return "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
        }

        fun formatDate(epochMillis: Long): String {
            val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
            return String.format(
                Locale("ru"), "%02d.%02d.%04d",
                cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)
            )
        }
    }
}
