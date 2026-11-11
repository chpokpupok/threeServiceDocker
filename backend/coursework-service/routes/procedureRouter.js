const { Router } = require("express");
const controller = require("../controllers/procedureController");

const router = Router();

/**
 * @swagger
 * tags:
 *   name: Procedures
 *   description: Изменение данных (Хранимые процедуры)
 */

// ================= САФИРА =================

/**
 * @swagger
 * /api/procedure/update_supervisor/:
 *   put:
 *     summary: Обновить данные преподавателя
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [login, last_name, first_name, position]
 *             properties:
 *               login: { type: string, example: "supervisor1" }
 *               password: { type: string, example: "newpassword123" }
 *               last_name: { type: string, example: "Иванов" }
 *               first_name: { type: string, example: "Иван" }
 *               middle_name: { type: string, example: "Иванович" }
 *               position: { type: string, example: "Доцент" }
 *               phone_number: { type: string, example: "+79991234567" }
 *               email: { type: string, example: "ivanov@mail.ru" }
 *     responses:
 *       201: { description: "Superviser updated" }
 *       422: { description: "Ошибка валидации" }
 */
router.put("/update_supervisor/", controller.validate_up_supervisor_data, controller.update_supervisor);

/**
 * @swagger
 * /api/procedure/delete_supervisor/:
 *   delete:
 *     summary: Удалить преподавателя по логину
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [login]
 *             properties:
 *               login: { type: string, example: "supervisor1" }
 *     responses:
 *       201: { description: "Superviser deleted" }
 */
router.delete("/delete_supervisor/", controller.validate_del_supervisor_data, controller.delete_supervisor);

/**
 * @swagger
 * /api/procedure/insert_discipline/:
 *   post:
 *     summary: Добавить дисциплину
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [name, department, semester]
 *             properties:
 *               name: { type: string, example: "Базы данных" }
 *               department: { type: string, example: "Кафедра ИТ" }
 *               semester: { type: integer, example: 1 }
 *     responses:
 *       201: { description: "Discipline insered" }
 */
router.post("/insert_discipline/", controller.insert_discipline);

/**
 * @swagger
 * /api/procedure/update_discipline/:
 *   put:
 *     summary: Обновить дисциплину
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [old_name, old_department, new_name, new_department, semester]
 *             properties:
 *               old_name: { type: string, example: "Базы данных" }
 *               old_department: { type: string, example: "Кафедра ИТ" }
 *               new_name: { type: string, example: "Базы данных (SQL)" }
 *               new_department: { type: string, example: "Кафедра ИТ" }
 *               semester: { type: integer, example: 2 }
 *     responses:
 *       201: { description: "Discipline updated" }
 */
router.put("/update_discipline/", controller.update_discipline);

/**
 * @swagger
 * /api/procedure/delete_discipline/:
 *   delete:
 *     summary: Удалить дисциплину
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [name, department]
 *             properties:
 *               name: { type: string, example: "Базы данных" }
 *               department: { type: string, example: "Кафедра ИТ" }
 *     responses:
 *       201: { description: "Discipline deleted" }
 */
router.delete("/delete_discipline/", controller.delete_discipline);

/**
 * @swagger
 * /api/procedure/insert_group/:
 *   post:
 *     summary: Добавить группу
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [name, year_of_admission, department, education_type, study_duration]
 *             properties:
 *               name: { type: string, example: "ИВТ-21" }
 *               year_of_admission: { type: integer, example: 2021 }
 *               department: { type: string, example: "Кафедра ИТ" }
 *               education_type: { type: string, example: "Бюджет" }
 *               study_duration: { type: integer, example: 4 }
 *     responses:
 *       201: { description: "Group insered" }
 */
router.post("/insert_group/", controller.insert_group);

/**
 * @swagger
 * /api/procedure/update_group/:
 *   put:
 *     summary: Обновить группу
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [name, year_of_admission, department, education_type, study_duration]
 *             properties:
 *               name: { type: string, example: "ИВТ-21" }
 *               year_of_admission: { type: integer, example: 2021 }
 *               department: { type: string, example: "Кафедра ИТ" }
 *               education_type: { type: string, example: "Контракт" }
 *               study_duration: { type: integer, example: 4 }
 *     responses:
 *       201: { description: "Group updated" }
 */
router.put("/update_group/", controller.update_group);

/**
 * @swagger
 * /api/procedure/delete_group/:
 *   delete:
 *     summary: Удалить группу
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [name]
 *             properties:
 *               name: { type: string, example: "ИВТ-21" }
 *     responses:
 *       201: { description: "Group deleted" }
 */
router.delete("/delete_group/", controller.delete_group);

/**
 * @swagger
 * /api/procedure/insert_department/:
 *   post:
 *     summary: Добавить кафедру
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [name, faculty, head, phone]
 *             properties:
 *               name: { type: string, example: "Кафедра ИТ" }
 *               faculty: { type: string, example: "Факультет информатики" }
 *               head: { type: string, example: "Петров Петр Петрович" }
 *               phone: { type: string, example: "+79991234567" }
 *     responses:
 *       201: { description: "Department insered" }
 */
router.post("/insert_department/", controller.insert_department);

// ================= КАМИЛЬ =================

/**
 * @swagger
 * /api/procedure/update_coursework:
 *   put:
 *     summary: Обновить курсовую (преподаватель)
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [coursework_id, topic_status, work_status]
 *             properties:
 *               coursework_id: { type: integer, example: 1 }
 *               topic: { type: string, example: "Новая тема" }
 *               submission_date: { type: string, example: "2024-05-20" }
 *               defense_date: { type: string, example: "2024-06-10" }
 *               topic_status: { type: string, example: "Утверждена" }
 *               work_link: { type: string, example: "http://example.com/work.pdf" }
 *               antiplagiat_level: { type: integer, example: 85 }
 *               theory_grade: { type: number, example: 4.5 }
 *               practice_grade: { type: number, example: 5.0 }
 *               design_grade: { type: number, example: 4.0 }
 *               defense_grade: { type: number, example: 5.0 }
 *               commission: { type: string, example: "Комиссия №1" }
 *               work_status: { type: string, example: "Защищена" }
 *               comment: { type: string, example: "Отличная работа" }
 *     responses:
 *       200: { description: "Курсовая работа обновлена успешно" }
 */
router.put("/update_coursework", controller.validate_up_coursework_data, controller.update_coursework_teacher);

/**
 * @swagger
 * /api/procedure/add_supervisor_department:
 *   post:
 *     summary: Привязать преподавателя к кафедре
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [supervisor_login, department_name]
 *             properties:
 *               supervisor_login: { type: string, example: "supervisor1" }
 *               department_name: { type: string, example: "Кафедра ИТ" }
 *     responses:
 *       201: { description: "Преподаватель добавлен к кафедре успешно" }
 */
router.post("/add_supervisor_department", controller.add_supervisor_department);

/**
 * @swagger
 * /api/procedure/remove_supervisor_department/{link_id}:
 *   delete:
 *     summary: Отвязать преподавателя от кафедры
 *     tags: [Procedures]
 *     parameters:
 *       - in: path
 *         name: link_id
 *         required: true
 *         schema: { type: integer, example: 1 }
 *     responses:
 *       200: { description: "Преподаватель удален из кафедры успешно" }
 */
router.delete("/remove_supervisor_department/:link_id", controller.remove_supervisor_department);

// ================= АРТЕМ =================

/**
 * @swagger
 * /api/procedure/delete_department:
 *   delete:
 *     summary: Удалить кафедру
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [name]
 *             properties:
 *               name: { type: string, example: "Кафедра ИТ" }
 *     responses:
 *       200: { description: "Department deleted" }
 */
router.delete('/delete_department', controller.delete_department);

/**
 * @swagger
 * /api/procedure/update_department:
 *   put:
 *     summary: Обновить кафедру
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [old_name, new_name]
 *             properties:
 *               old_name: { type: string, example: "Кафедра ИТ" }
 *               new_name: { type: string, example: "Кафедра ИВТ" }
 *               faculty: { type: string, example: "Факультет информатики" }
 *               head: { type: string, example: "Сидоров С.С." }
 *               phone: { type: string, example: "+79991234567" }
 *     responses:
 *       200: { description: "Department updated" }
 */
router.put('/update_department', controller.update_department);

/**
 * @swagger
 * /api/procedure/insert_faculty:
 *   post:
 *     summary: Добавить факультет
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [name, dean, phone]
 *             properties:
 *               name: { type: string, example: "Факультет информатики" }
 *               dean: { type: string, example: "Иванов И.И." }
 *               phone: { type: string, example: "+79991234567" }
 *     responses:
 *       201: { description: "Faculty inserted" }
 */
router.post('/insert_faculty', controller.insert_faculty);

/**
 * @swagger
 * /api/procedure/delete_faculty:
 *   delete:
 *     summary: Удалить факультет
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [name]
 *             properties:
 *               name: { type: string, example: "Факультет информатики" }
 *     responses:
 *       200: { description: "Faculty deleted" }
 */
router.delete('/delete_faculty', controller.delete_faculty);

/**
 * @swagger
 * /api/procedure/update_faculty:
 *   put:
 *     summary: Обновить факультет
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [old_name, new_name]
 *             properties:
 *               old_name: { type: string, example: "Факультет информатики" }
 *               new_name: { type: string, example: "Факультет ИТ" }
 *               dean: { type: string, example: "Петров П.П." }
 *               phone: { type: string, example: "+79991234567" }
 *     responses:
 *       200: { description: "Faculty updated" }
 */
router.put('/update_faculty', controller.update_faculty);

/**
 * @swagger
 * /api/procedure/add_students_to_group:
 *   post:
 *     summary: Добавить студентов в группу
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [group_name, students_list]
 *             properties:
 *               group_name: { type: string, example: "ИВТ-21" }
 *               students_list:
 *                 type: array
 *                 items: { type: string }
 *                 example: ["{\"lastname\":\"Иванов\",\"firstname\":\"Иван\",\"middlename\":\"Иванович\",\"recordbook\":\"123456\"}"]
 *     responses:
 *       201: { description: "Students added to group" }
 */
router.post('/add_students_to_group', controller.add_students_to_group);

/**
 * @swagger
 * /api/procedure/update_student:
 *   put:
 *     summary: Обновить данные студента
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [student_login]
 *             properties:
 *               student_login: { type: string, example: "student1" }
 *               phone: { type: string, example: "+79991234567" }
 *               email: { type: string, example: "student@mail.ru" }
 *               leader: { type: string, example: "Иванов И.И." }
 *     responses:
 *       200: { description: "Student updated" }
 */
router.put('/update_student', controller.validate_up_student_data, controller.update_student);

/**
 * @swagger
 * /api/procedure/insert_coursework:
 *   post:
 *     summary: Создать курсовую работу
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [student_login, supervisor_login, discipline_name, topic, year]
 *             properties:
 *               student_login: { type: string, example: "student1" }
 *               supervisor_login: { type: string, example: "supervisor1" }
 *               discipline_name: { type: string, example: "Базы данных" }
 *               topic: { type: string, example: "Разработка БД" }
 *               year: { type: string, example: "2024-09-01" }
 *     responses:
 *       201: { description: "Coursework created" }
 */
router.post('/insert_coursework', controller.validate_ins_coursework_data, controller.insert_course_work);

/**
 * @swagger
 * /api/procedure/delete_coursework:
 *   delete:
 *     summary: Удалить курсовую работу
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [coursework_id]
 *             properties:
 *               coursework_id: { type: integer, example: 1 }
 *     responses:
 *       200: { description: "Coursework deleted" }
 */
router.delete('/delete_coursework', controller.delete_course_work);

/**
 * @swagger
 * /api/procedure/update_coursework_student:
 *   put:
 *     summary: Обновить курсовую (студент)
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [coursework_id, topic, work_link]
 *             properties:
 *               coursework_id: { type: integer, example: 1 }
 *               topic: { type: string, example: "Новая тема от студента" }
 *               work_link: { type: string, example: "http://example.com/work.pdf" }
 *     responses:
 *       200: { description: "Coursework updated by student" }
 */
router.put('/update_coursework_student', controller.validate_link_data, controller.update_course_work_student);

/**
 * @swagger
 * /api/procedure/import_students_from_text:
 *   post:
 *     summary: Импорт студентов из текста
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [group_name, students_text]
 *             properties:
 *               group_name: { type: string, example: "ИВТ-21" }
 *               students_text: { type: string, example: "Иванов Иван Иванович 123456\nПетров Петр Петрович 654321" }
 *     responses:
 *       201: { description: "Успешно добавлено студентов" }
 *       400: { description: "Ошибка в тексте" }
 */
router.post('/import_students_from_text', controller.import_students_from_text);

/**
 * @swagger
 * /api/procedure/insert_courseworks_for_students_from_file:
 *   post:
 *     summary: Массовое создание курсовых из файла
 *     tags: [Procedures]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [students_text, supervisor_login, discipline_name, topic, year]
 *             properties:
 *               students_text: { type: string, example: "Иванов Иван Иванович 123456" }
 *               supervisor_login: { type: string, example: "supervisor1" }
 *               discipline_name: { type: string, example: "Базы данных" }
 *               topic: { type: string, example: "Разработка БД" }
 *               year: { type: string, example: "2024-09-01" }
 *     responses:
 *       201: { description: "Успешно создано курсовых" }
 *       500: { description: "Ошибка" }
 */
router.post('/insert_courseworks_for_students_from_file', controller.insert_courseworks_for_students_from_file);

module.exports = router;