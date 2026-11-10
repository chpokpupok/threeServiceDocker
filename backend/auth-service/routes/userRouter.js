const { Router } = require("express");
const controller = require("../controllers/userController");

// создаем объект маршрутизатор
const router = Router();

/**
 * @swagger
 * /home/login_student:
 *   post:
 *     summary: Вход студента
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               login:
 *                 type: string
 *               password:
 *                 type: string
 *     responses:
 *       200:
 *         description: Успешный вход
 *       401:
 *         description: Неверный логин или пароль
 */
router.post(
  "/login_student",
  controller.validate_student_login_data,
  controller.get_student_login
);

/**
 * @swagger
 * /home/login_supervisor:
 *   post:
 *     summary: Вход преподавателя (научного руководителя)
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               login:
 *                 type: string
 *               password:
 *                 type: string
 *     responses:
 *       200:
 *         description: Успешный вход
 *       401:
 *         description: Неверный логин или пароль
 */
router.post(
  "/login_supervisor",
  controller.validate_supervisor_login_data,
  controller.get_supervisor_login
);

/**
 * @swagger
 * /home/register:
 *   post:
 *     summary: Регистрация преподавателя
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               login:
 *                 type: string
 *               password:
 *                 type: string
 *               last_name:
 *                 type: string
 *               first_name:
 *                 type: string
 *               middle_name:
 *                 type: string
 *               position:
 *                 type: string
 *               phone_number:
 *                 type: string
 *               email:
 *                 type: string
 *     responses:
 *       201:
 *         description: Успешная регистрация
 *       422:
 *         description: Ошибка валидации
 */
router.post(
  "/register",
  controller.validate_supervisor_data,
  controller.register_supervisor
);

/**
 * @swagger
 * /home:
 *   get:
 *     summary: Проверка статуса входа (домашняя страница)
 *     responses:
 *       200:
 *         description: Возвращает приветствие
 */
router.get("/", controller.get_redirect_home);

// экспортируем маршрутизатор на server
module.exports = router;