package com.example.preschoolmanagement.data

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
import java.util.Calendar

object SeedData {

    /** Long (epoch millis) из даты — для сид-данных */
    fun date(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            set(year, month - 1, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    // --- Группы (10 записей) ---
    val groups = listOf(
        "Ясли-1", "Ясли-2", "Младшая-1", "Младшая-2",
        "Средняя-1", "Средняя-2", "Старшая-1", "Старшая-2",
        "Подготовительная-1", "Подготовительная-2"
    ).map { name ->
        GroupEntity(
            groupName = name,
            ageCategory = when {
                name.startsWith("Ясли") -> "1–3 года"
                name.startsWith("Младшая") -> "3–4 года"
                name.startsWith("Средняя") -> "4–5 лет"
                name.startsWith("Старшая") -> "5–6 лет"
                else -> "6–7 лет"
            }
        )
    }

    // --- Ученики (12 записей), groupId проставляется при сидировании ---
    data class StudentRow(val lastName: String, val firstName: String, val birth: String)

    val students = listOf(
        StudentRow("Иванов", "Пётр", "2022-03-15"),
        StudentRow("Смирнова", "Анна", "2021-07-22"),
        StudentRow("Кузнецов", "Дмитрий", "2021-01-30"),
        StudentRow("Попова", "Мария", "2022-11-05"),
        StudentRow("Васильев", "Алексей", "2020-05-12"),
        StudentRow("Петрова", "Елена", "2021-09-18"),
        StudentRow("Соколов", "Никита", "2020-12-01"),
        StudentRow("Михайлова", "Ольга", "2022-02-14"),
        StudentRow("Новиков", "Артём", "2021-04-25"),
        StudentRow("Фёдорова", "Дарья", "2020-08-08"),
        StudentRow("Морозов", "Максим", "2022-06-30"),
        StudentRow("Волкова", "Ксения", "2021-10-17")
    ).map { row ->
        val p = row.birth.split("-").map { it.toInt() }
        StudentEntity(
            lastName = row.lastName,
            firstName = row.firstName,
            dateOfBirth = date(p[0], p[1], p[2]),
            groupId = 0, // заполняется при сидировании
            admissionDate = date(2024, 9, 1)
        )
    }

    // --- Педагоги (10 записей) ---
    data class TeacherRow(
        val fio: List<String>, val specialization: String,
        val hire: String, val salary: Double, val phone: String
    )

    val teachers = listOf(
        TeacherRow(listOf("Козлова", "Светлана", "Ивановна"), "Воспитатель", "2015-08-10", 35000.0, "+7-900-111-22-33"),
        TeacherRow(listOf("Орлов", "Сергей", "Петрович"), "Музыкальный руководитель", "2017-01-15", 32000.0, "+7-900-222-33-44"),
        TeacherRow(listOf("Захарова", "Наталья", "Андреевна"), "Воспитатель", "2016-09-01", 35000.0, "+7-900-333-44-55"),
        TeacherRow(listOf("Белов", "Игорь", "Владимирович"), "Инструктор по физкультуре", "2018-03-20", 33000.0, "+7-900-444-55-66"),
        TeacherRow(listOf("Тихонова", "Ирина", "Сергеевна"), "Воспитатель", "2014-06-12", 36000.0, "+7-900-555-66-77"),
        TeacherRow(listOf("Ефимов", "Павел", "Николаевич"), "Педагог-психолог", "2019-10-05", 34000.0, "+7-900-666-77-88"),
        TeacherRow(listOf("Громова", "Юлия", "Дмитриевна"), "Воспитатель", "2020-02-17", 35000.0, "+7-900-777-88-99"),
        TeacherRow(listOf("Дмитриев", "Андрей", "Олегович"), "Учитель-логопед", "2021-08-23", 37000.0, "+7-900-888-99-00"),
        TeacherRow(listOf("Панова", "Марина", "Викторовна"), "Воспитатель", "2022-01-09", 35000.0, "+7-900-999-00-11"),
        TeacherRow(listOf("Крылов", "Виктор", "Павлович"), "Музыкальный руководитель", "2023-04-03", 32000.0, "+7-900-101-01-02")
    ).map { row ->
        val p = row.hire.split("-").map { it.toInt() }
        TeacherEntity(
            lastName = row.fio[0], firstName = row.fio[1], middleName = row.fio[2],
            specialization = row.specialization,
            hireDate = date(p[0], p[1], p[2]),
            salary = row.salary,
            passportSeries = "4510",
            passportNumber = (100000 + row.phone.hashCode() % 899999).toString(),
            address = "г. Москва, ул. Ленина, д. ${10 + row.salary.toInt() % 40}",
            phone = row.phone
        )
    }

    // --- Предметы (10 записей) ---
    data class SubjectRow(val name: String, val description: String)

    val subjects = listOf(
        SubjectRow("Развитие речи", "Занятия по развитию речи и знакомству с литературой"),
        SubjectRow("Математика", "Элементарные математические представления"),
        SubjectRow("Рисование", "Художественно-творческое развитие"),
        SubjectRow("Лепка", "Развитие мелкой моторики"),
        SubjectRow("Аппликация", "Творческое развитие"),
        SubjectRow("Музыка", "Музыкальные занятия"),
        SubjectRow("Физкультура", "Физическое развитие"),
        SubjectRow("Окружающий мир", "Знакомство с природой и обществом"),
        SubjectRow("Конструирование", "Развитие логики и моторики"),
        SubjectRow("Подготовка к школе", "Подготовительный курс для старших групп")
    ).map { row -> SubjectEntity(subjectName = row.name, description = row.description) }

    // --- Расписание (12 записей) — id проставляются при сидировании ---
    data class ScheduleRow(val dayOfWeek: String, val startTime: String, val endTime: String)

    val schedule = listOf(
        ScheduleRow("Понедельник", "09:00", "09:30"),
        ScheduleRow("Понедельник", "10:00", "10:30"),
        ScheduleRow("Вторник", "09:00", "09:30"),
        ScheduleRow("Вторник", "10:00", "10:45"),
        ScheduleRow("Среда", "09:00", "09:30"),
        ScheduleRow("Среда", "11:00", "11:30"),
        ScheduleRow("Четверг", "09:00", "09:30"),
        ScheduleRow("Четверг", "10:00", "10:30"),
        ScheduleRow("Пятница", "09:00", "09:30"),
        ScheduleRow("Пятница", "10:00", "10:45"),
        ScheduleRow("Пятница", "11:00", "11:30"),
        ScheduleRow("Суббота", "10:00", "10:30")
    ).map { row ->
        ScheduleEntity(
            groupId = 0, subjectId = 0, teacherId = 0,
            dayOfWeek = row.dayOfWeek, startTime = row.startTime, endTime = row.endTime
        )
    }

    // --- Родители (10 записей) ---
    data class ParentRow(
        val fio: List<String>, val passport: String, val phone: String, val address: String
    )

    val parents = listOf(
        ParentRow(listOf("Иванов", "Сергей", "Викторович"), "4510 123456", "+7-901-101-01-01", "г. Москва, ул. Ленина, д. 5"),
        ParentRow(listOf("Иванова", "Екатерина", "Андреевна"), "4510 234567", "+7-901-102-02-02", "г. Москва, ул. Ленина, д. 5"),
        ParentRow(listOf("Смирнов", "Олег", "Иванович"), "4511 345678", "+7-901-103-03-03", "г. Москва, пр. Мира, д. 12"),
        ParentRow(listOf("Кузнецова", "Татьяна", "Львовна"), "4511 456789", "+7-901-104-04-04", "г. Москва, ул. Гагарина, д. 8"),
        ParentRow(listOf("Попов", "Роман", "Юрьевич"), "4512 567890", "+7-901-105-05-05", "г. Москва, ул. Садовая, д. 21"),
        ParentRow(listOf("Васильева", "Нина", "Павловна"), "4512 678901", "+7-901-106-06-06", "г. Москва, ул. Весенняя, д. 3"),
        ParentRow(listOf("Петров", "Денис", "Максимович"), "4513 789012", "+7-901-107-07-07", "г. Москва, пр. Победы, д. 17"),
        ParentRow(listOf("Соколова", "Алина", "Романовна"), "4513 890123", "+7-901-108-08-08", "г. Москва, ул. Луговая, д. 9"),
        ParentRow(listOf("Новикова", "Оксана", "Игоревна"), "4514 901234", "+7-901-109-09-09", "г. Москва, ул. Полевая, д. 14"),
        ParentRow(listOf("Фёдоров", "Глеб", "Аркадьевич"), "4514 012345", "+7-901-110-10-10", "г. Москва, ул. Центральная, д. 2")
    ).map { row ->
        val pp = row.passport.split(" ")
        ParentEntity(
            lastName = row.fio[0], firstName = row.fio[1], middleName = row.fio[2],
            passportSeries = pp[0], passportNumber = pp[1],
            phone = row.phone, address = row.address
        )
    }

    // --- Связи родитель–ученик (14 записей), id проставляются при сидировании ---
    data class ParentStudentRow(
        val parentNo: Int, val studentNo: Int,
        val relationshipType: String, val contactPriority: Int
    )

    val parentStudentRows = listOf(
        ParentStudentRow(1, 1, "Отец", 1), ParentStudentRow(2, 1, "Мать", 2),
        ParentStudentRow(3, 2, "Отец", 1),
        ParentStudentRow(4, 3, "Мать", 1),
        ParentStudentRow(5, 4, "Отец", 1),
        ParentStudentRow(6, 5, "Мать", 1), ParentStudentRow(1, 5, "Отец", 2),
        ParentStudentRow(7, 6, "Отец", 1),
        ParentStudentRow(8, 7, "Мать", 1),
        ParentStudentRow(9, 8, "Мать", 1),
        ParentStudentRow(10, 9, "Отец", 1),
        ParentStudentRow(2, 10, "Мать", 1),
        ParentStudentRow(3, 11, "Отец", 1), ParentStudentRow(5, 11, "Мать", 2)
    )

    // --- Договоры (10 записей), id проставляются при сидировании ---
    data class ContractRow(
        val studentNo: Int, val parentNo: Int,
        val dateStr: String, val amount: Double, val status: String
    )

    val contractRows = listOf(
        ContractRow(1, 1, "2024-09-01", 5000.0, "Активен"),
        ContractRow(2, 2, "2024-09-01", 5000.0, "Активен"),
        ContractRow(3, 3, "2024-09-05", 5500.0, "Активен"),
        ContractRow(4, 4, "2024-09-10", 4500.0, "Активен"),
        ContractRow(5, 5, "2024-09-12", 5000.0, "Активен"),
        ContractRow(6, 6, "2024-10-01", 5200.0, "Активен"),
        ContractRow(7, 7, "2024-10-03", 5000.0, "Активен"),
        ContractRow(8, 8, "2024-10-15", 4800.0, "Активен"),
        ContractRow(9, 9, "2025-01-10", 6000.0, "Активен"),
        ContractRow(10, 10, "2025-01-20", 5500.0, "Завершён")
    )

    // --- Платежи (12 записей), id проставляются при сидировании ---
    data class PaymentRow(
        val contractNo: Int, val amount: Double,
        val dateStr: String, val period: String, val status: String
    )

    val paymentRows = listOf(
        PaymentRow(1, 5000.0, "2024-09-01", "сентябрь 2024", "Оплачен"),
        PaymentRow(1, 5000.0, "2024-10-01", "октябрь 2024", "Оплачен"),
        PaymentRow(2, 5000.0, "2024-09-05", "сентябрь 2024", "Оплачен"),
        PaymentRow(3, 5500.0, "2024-09-10", "сентябрь 2024", "Просрочен"),
        PaymentRow(3, 5500.0, "2024-10-10", "октябрь 2024", "Ожидает"),
        PaymentRow(4, 4500.0, "2024-09-15", "сентябрь 2024", "Оплачен"),
        PaymentRow(5, 5000.0, "2024-09-15", "сентябрь 2024", "Ожидает"),
        PaymentRow(6, 5200.0, "2024-10-05", "октябрь 2024", "Ожидает"),
        PaymentRow(7, 5000.0, "2024-10-07", "октябрь 2024", "Просрочен"),
        PaymentRow(8, 4800.0, "2024-10-20", "октябрь 2024", "Ожидает"),
        PaymentRow(9, 6000.0, "2025-01-15", "январь 2025", "Оплачен"),
        PaymentRow(10, 5500.0, "2025-01-25", "январь 2025", "Оплачен")
    )

    // --- Посещаемость (12 записей), id проставляются при сидировании ---
    data class AttendanceRow(
        val studentNo: Int, val dateStr: String, val present: Boolean, val reason: String?
    )

    val attendanceRows = listOf(
        AttendanceRow(1, "2025-11-17", true, null),
        AttendanceRow(2, "2025-11-17", true, null),
        AttendanceRow(3, "2025-11-17", false, "Болезнь (ОРВИ)"),
        AttendanceRow(4, "2025-11-17", true, null),
        AttendanceRow(1, "2025-11-18", true, null),
        AttendanceRow(2, "2025-11-18", true, null),
        AttendanceRow(3, "2025-11-18", false, "Болезнь (ОРВИ)"),
        AttendanceRow(5, "2025-11-18", true, null),
        AttendanceRow(1, "2025-11-19", true, null),
        AttendanceRow(2, "2025-11-19", false, "Семейные обстоятельства"),
        AttendanceRow(4, "2025-11-19", true, null),
        AttendanceRow(6, "2025-11-19", true, null)
    )
}
