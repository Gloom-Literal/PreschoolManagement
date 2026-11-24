package com.example.preschoolmanagement.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM `Group` ORDER BY groupId")
    fun getAll(): Flow<List<GroupEntity>>

    data class GroupWithCount(
        @Embedded val group: GroupEntity,
        val studentCount: Int
    )

    @Query(
        """
        SELECT g.*, COUNT(s.studentId) AS studentCount
        FROM `Group` g
        LEFT JOIN Student s ON g.groupId = s.groupId
        GROUP BY g.groupId
        ORDER BY g.groupId
        """
    )
    fun getAllWithStudentCount(): Flow<List<GroupWithCount>>

    @Query("SELECT COUNT(*) FROM `Group`")
    suspend fun count(): Int

    @Insert
    suspend fun insert(group: GroupEntity): Long

    @Update
    suspend fun update(group: GroupEntity)

    @Delete
    suspend fun delete(group: GroupEntity)

    @Query("SELECT COUNT(*) FROM Student WHERE groupId = :groupId")
    suspend fun studentCount(groupId: Long): Int

    @Query("DELETE FROM `Group` WHERE groupId = :groupId")
    suspend fun deleteById(groupId: Long)
}

@Dao
interface StudentDao {
    data class StudentWithGroup(
        @Embedded val student: StudentEntity,
        val groupName: String
    )

    @Query(
        """
        SELECT s.*, g.groupName AS groupName FROM Student s
        JOIN `Group` g ON s.groupId = g.groupId
        ORDER BY s.lastName, s.firstName
        """
    )
    fun getAllWithGroup(): Flow<List<StudentWithGroup>>

    @Query(
        """
        SELECT s.*, g.groupName AS groupName FROM Student s
        JOIN `Group` g ON s.groupId = g.groupId
        WHERE s.lastName LIKE '%' || :query || '%'
           OR s.firstName LIKE '%' || :query || '%'
           OR s.middleName LIKE '%' || :query || '%'
        ORDER BY s.lastName, s.firstName
        """
    )
    fun search(query: String): Flow<List<StudentWithGroup>>

    @Query("SELECT * FROM Student ORDER BY lastName")
    fun getAllRaw(): Flow<List<StudentEntity>>

    @Query("SELECT COUNT(*) FROM Student")
    suspend fun count(): Int

    @Insert
    suspend fun insert(student: StudentEntity): Long

    @Update
    suspend fun update(student: StudentEntity)

    @Query("DELETE FROM Student WHERE studentId = :studentId")
    suspend fun deleteById(studentId: Long)
}

@Dao
interface TeacherDao {
    @Query("SELECT * FROM Teacher ORDER BY lastName")
    fun getAll(): Flow<List<TeacherEntity>>

    @Query("SELECT COUNT(*) FROM Teacher")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM Schedule WHERE teacherId = :teacherId")
    suspend fun scheduleCount(teacherId: Long): Int

    @Insert
    suspend fun insert(teacher: TeacherEntity): Long

    @Update
    suspend fun update(teacher: TeacherEntity)

    @Delete
    suspend fun delete(teacher: TeacherEntity)

    @Query("DELETE FROM Teacher WHERE teacherId = :teacherId")
    suspend fun deleteById(teacherId: Long)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM Subject ORDER BY subjectName")
    fun getAll(): Flow<List<SubjectEntity>>

    @Query("SELECT COUNT(*) FROM Subject")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM Schedule WHERE subjectId = :subjectId")
    suspend fun scheduleCount(subjectId: Long): Int

    @Insert
    suspend fun insert(subject: SubjectEntity): Long

    @Update
    suspend fun update(subject: SubjectEntity)

    @Delete
    suspend fun delete(subject: SubjectEntity)

    @Query("DELETE FROM Subject WHERE subjectId = :subjectId")
    suspend fun deleteById(subjectId: Long)
}

@Dao
interface ScheduleDao {
    data class ScheduleItem(
        val scheduleId: Long,
        val groupName: String,
        val subjectName: String,
        val teacherName: String,
        val dayOfWeek: String,
        val startTime: String,
        val endTime: String
    )

    @Query(
        """
        SELECT sch.scheduleId, g.groupName, sub.subjectName,
               t.lastName || ' ' || t.firstName || ' ' || t.middleName AS teacherName,
               sch.dayOfWeek, sch.startTime, sch.endTime
        FROM Schedule sch
        JOIN `Group` g ON sch.groupId = g.groupId
        JOIN Subject sub ON sch.subjectId = sub.subjectId
        JOIN Teacher t ON sch.teacherId = t.teacherId
        ORDER BY CASE sch.dayOfWeek
            WHEN 'Понедельник' THEN 1 WHEN 'Вторник' THEN 2 WHEN 'Среда' THEN 3
            WHEN 'Четверг' THEN 4 WHEN 'Пятница' THEN 5 WHEN 'Суббота' THEN 6
            WHEN 'Воскресенье' THEN 7 END, sch.startTime
        """
    )
    fun getFullSchedule(): Flow<List<ScheduleItem>>

    @Query("SELECT COUNT(*) FROM Schedule")
    suspend fun count(): Int

    @Insert
    suspend fun insert(schedule: ScheduleEntity): Long

    @Update
    suspend fun update(schedule: ScheduleEntity)

    @Delete
    suspend fun delete(schedule: ScheduleEntity)

    @Query("DELETE FROM Schedule WHERE scheduleId = :scheduleId")
    suspend fun deleteById(scheduleId: Long)
}

@Dao
interface ParentDao {
    data class ParentWithChildren(
        @Embedded val parent: ParentEntity,
        val children: String?
    )

    @Query(
        """
        SELECT p.*, GROUP_CONCAT(ch.lastName || ' ' || ch.firstName, ', ') AS children
        FROM Parent p
        LEFT JOIN ParentStudent ps ON p.parentId = ps.parentId
        LEFT JOIN Student ch ON ps.studentId = ch.studentId
        GROUP BY p.parentId
        ORDER BY p.lastName
        """
    )
    fun getAllWithChildren(): Flow<List<ParentWithChildren>>

    @Query(
        """
        SELECT p.*, GROUP_CONCAT(ch.lastName || ' ' || ch.firstName, ', ') AS children
        FROM Parent p
        LEFT JOIN ParentStudent ps ON p.parentId = ps.parentId
        LEFT JOIN Student ch ON ps.studentId = ch.studentId
        WHERE p.lastName LIKE '%' || :query || '%'
           OR p.firstName LIKE '%' || :query || '%'
        GROUP BY p.parentId
        ORDER BY p.lastName
        """
    )
    fun search(query: String): Flow<List<ParentWithChildren>>

    @Query("SELECT * FROM Parent ORDER BY lastName")
    fun getAllParents(): Flow<List<ParentEntity>>

    @Query("SELECT COUNT(*) FROM Parent")
    suspend fun count(): Int

    @Insert
    suspend fun insert(parent: ParentEntity): Long

    @Update
    suspend fun update(parent: ParentEntity)

    @Query("DELETE FROM Parent WHERE parentId = :parentId")
    suspend fun deleteById(parentId: Long)

    @Query("SELECT COUNT(*) FROM ParentStudent WHERE parentId = :parentId")
    suspend fun childrenCount(parentId: Long): Int
}

@Dao
interface ParentStudentDao {
    @Query("SELECT * FROM ParentStudent")
    fun getAll(): Flow<List<ParentStudentEntity>>

    @Query("SELECT COUNT(*) FROM ParentStudent")
    suspend fun count(): Int

    @Insert
    suspend fun insert(relation: ParentStudentEntity): Long

    @Delete
    suspend fun delete(relation: ParentStudentEntity)
}

@Dao
interface ContractDao {
    data class ContractInfo(
        val contractId: Long,
        val studentName: String,
        val parentName: String,
        val contractDate: Long,
        val paymentAmount: Double,
        val status: String
    )

    @Query(
        """
        SELECT c.contractId,
               s.lastName || ' ' || s.firstName AS studentName,
               p.lastName || ' ' || p.firstName AS parentName,
               c.contractDate, c.paymentAmount, c.status
        FROM Contract c
        JOIN Student s ON c.studentId = s.studentId
        JOIN Parent p ON c.parentId = p.parentId
        ORDER BY c.contractDate DESC
        """
    )
    fun getAll(): Flow<List<ContractInfo>>

    @Query("SELECT COUNT(*) FROM Contract")
    suspend fun count(): Int

    @Insert
    suspend fun insert(contract: ContractEntity): Long

    @Update
    suspend fun update(contract: ContractEntity)

    @Query("DELETE FROM Contract WHERE contractId = :contractId")
    suspend fun deleteById(contractId: Long)

    @Query("SELECT COUNT(*) FROM Contract WHERE studentId = :studentId")
    suspend fun countForStudent(studentId: Long): Int
}

@Dao
interface PaymentDao {
    data class PaymentInfo(
        val paymentId: Long,
        val contractId: Long,
        val studentName: String,
        val parentName: String,
        val paymentAmount: Double,
        val paymentDate: Long,
        val paymentPeriod: String,
        val paymentStatus: String
    )

    @Query(
        """
        SELECT pay.paymentId, pay.contractId,
               s.lastName || ' ' || s.firstName AS studentName,
               p.lastName || ' ' || p.firstName AS parentName,
               pay.paymentAmount, pay.paymentDate, pay.paymentPeriod, pay.paymentStatus
        FROM Payment pay
        JOIN Contract c ON pay.contractId = c.contractId
        JOIN Student s ON c.studentId = s.studentId
        JOIN Parent p ON c.parentId = p.parentId
        ORDER BY pay.paymentDate
        """
    )
    fun getAll(): Flow<List<PaymentInfo>>

    @Query("SELECT COUNT(*) FROM Payment")
    suspend fun count(): Int

    @Insert
    suspend fun insert(payment: PaymentEntity): Long

    @Update
    suspend fun update(payment: PaymentEntity)

    @Query("UPDATE Payment SET paymentStatus = :status WHERE paymentId = :paymentId")
    suspend fun updateStatus(paymentId: Long, status: String)

    @Query(
        """
        UPDATE Payment SET paymentStatus = 'Просрочен'
        WHERE paymentStatus = 'Ожидает' AND paymentDate < :now
        """
    )
    suspend fun markOverdue(now: Long)

    @Query("DELETE FROM Payment WHERE paymentId = :paymentId")
    suspend fun deleteById(paymentId: Long)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM Attendance ORDER BY date DESC")
    fun getAll(): Flow<List<AttendanceEntity>>

    data class AttendanceWithStudent(
        @Embedded val attendance: AttendanceEntity,
        val studentName: String
    )

    @Query(
        """
        SELECT a.*, s.lastName || ' ' || s.firstName AS studentName
        FROM Attendance a
        JOIN Student s ON a.studentId = s.studentId
        ORDER BY a.date DESC
        """
    )
    fun getAllWithStudent(): Flow<List<AttendanceWithStudent>>

    @Query("SELECT COUNT(*) FROM Attendance")
    suspend fun count(): Int

    @Insert
    suspend fun insert(attendance: AttendanceEntity): Long

    @Update
    suspend fun update(attendance: AttendanceEntity)

    @Delete
    suspend fun delete(attendance: AttendanceEntity)

    @Query("DELETE FROM Attendance WHERE attendanceId = :attendanceId")
    suspend fun deleteById(attendanceId: Long)
}
