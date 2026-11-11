const { Router } = require("express");
const controller = require("../controllers/functionController");

const router = Router();

/**
 * @swagger
 * tags:
 *   name: Functions
 *   description: Вызовы хранимых функций (получение данных)
 */

/**
 * @swagger
 * /api/function/get-courseworks:
 *   post:
 *     summary: Получить курсовые преподавателя (через POST)
 *     tags: [Functions]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - supervisor_id
 *             properties:
 *               supervisor_id:
 *                 type: integer
 *                 example: 1
 *     responses:
 *       200:
 *         description: Успешный ответ со списком курсовых
 *       400:
 *         description: supervisor_id обязателен
 */
router.post("/get-courseworks", controller.get_courseworks_for_teacher);

/**
 * @swagger
 * /api/function/get-courseworks/{supervisor_id}:
 *   get:
 *     summary: Получить курсовые преподавателя по ID (через GET)
 *     tags: [Functions]
 *     parameters:
 *       - in: path
 *         name: supervisor_id
 *         required: true
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: Список курсовых
 *       400:
 *         description: supervisor_id обязателен
 */
router.get("/get-courseworks/:supervisor_id", controller.get_courseworks_for_teacher);

/**
 * @swagger
 * /api/function/student-credentials:
 *   get:
 *     summary: Сгенерировать логины и пароли студентов
 *     tags: [Functions]
 *     responses:
 *       200:
 *         description: Список сгенерированных логинов и паролей
 *       400:
 *         description: Ошибка при генерации
 */
router.get("/student-credentials", controller.get_student_credentials);

/**
 * @swagger
 * /api/function/coursework-report:
 *   get:
 *     summary: Создать отчет (ФИО, Тема, Дата)
 *     tags: [Functions]
 *     responses:
 *       200:
 *         description: Данные отчета
 *       400:
 *         description: Ошибка при создании отчета
 */
router.get("/coursework-report", controller.get_coursework_report);

/**
 * @swagger
 * /api/function/get_courseworks_for_student/{student_id}:
 *   get:
 *     summary: Получить курсовые студента по ID (через GET)
 *     tags: [Functions]
 *     parameters:
 *       - in: path
 *         name: student_id
 *         required: true
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: Список курсовых студента
 */
router.get("/get_courseworks_for_student/:student_id", controller.get_courseworks_for_student);

/**
 * @swagger
 * /api/function/get_courseworks_for_student:
 *   post:
 *     summary: Получить курсовые студента (через POST)
 *     tags: [Functions]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - student_id
 *             properties:
 *               student_id:
 *                 type: integer
 *                 example: 1
 *     responses:
 *       200:
 *         description: Список курсовых студента
 */
router.post("/get_courseworks_for_student", controller.get_courseworks_for_student);

module.exports = router;