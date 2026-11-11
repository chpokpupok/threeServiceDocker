const { Router } = require("express");
const controller = require("../controllers/queryController");

const router = Router();

/**
 * @swagger
 * tags:
 *   name: Query
 *   description: Простые запросы к таблице факультетов (CRUD)
 */

/**
 * @swagger
 * /api/query/student-by-login/{login}:
 *   get:
 *     summary: Получить данные студента по логину
 *     tags: [Query]
 *     parameters:
 *       - in: path
 *         name: login
 *         required: true
 *         schema:
 *           type: string
 *           example: "ivanov_ii"
 *     responses:
 *       200:
 *         description: Данные студента
 *       404:
 *         description: Студент не найден
 */
router.get("/student-by-login/:login", controller.get_student_by_login);

/**
 * @swagger
 * /api/query:
 *   get:
 *     summary: Получить список всех факультетов
 *     tags: [Query]
 *     responses:
 *       200:
 *         description: Список факультетов
 *   post:
 *     summary: Добавить новый факультет
 *     tags: [Query]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - name
 *             properties:
 *               name:
 *                 type: string
 *                 example: "Факультет информатики"
 *               dean:
 *                 type: string
 *                 example: "Иванов Иван Иванович"
 *               phone_number:
 *                 type: string
 *                 example: "+79991234567"
 *     responses:
 *       201:
 *         description: Факультет добавлен
 *       500:
 *         description: Ошибка сервера
 */
router.get("/", controller.get_faculty);
router.post("/", controller.add_faculty);

/**
 * @swagger
 * /api/query/{id}:
 *   get:
 *     summary: Получить факультет по ID
 *     tags: [Query]
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: Данные факультета
 *       404:
 *         description: Факультет не найден
 *   put:
 *     summary: Обновить данные факультета
 *     tags: [Query]
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               name:
 *                 type: string
 *               dean:
 *                 type: string
 *               phone_number:
 *                 type: string
 *     responses:
 *       200:
 *         description: Факультет обновлен
 *   delete:
 *     summary: Удалить факультет
 *     tags: [Query]
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: Факультет удален
 */
router.get("/:id", controller.get_faculty_by_id);
router.put("/:id", controller.update_faculty);
router.delete("/:id", controller.delete_faculty);

module.exports = router;