const { Router } = require("express");
const controller = require("../controllers/userController");

const router = Router();

/**
 * @swagger
 * tags:
 *   name: Auth
 *   description: Аутентификация и регистрация
 */

/**
 * @swagger
 * /home/login_student:
 *   post:
 *     summary: Вход студента
 *     tags: [Auth]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [login, password]
 *             properties:
 *               login: { type: string, example: "student_login" }
 *               password: { type: string, example: "14characters!!" }
 *     responses:
 *       200:
 *         description: Успешный вход (устанавливает cookie)
 *       401:
 *         description: Неверный логин или пароль
 *       422:
 *         description: Ошибка валидации (пароль не 14 символов и т.д.)
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
 *     summary: Вход преподавателя
 *     tags: [Auth]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [login, password]
 *             properties:
 *               login: { type: string, example: "supervisor1" }
 *               password: { type: string, example: "secretpass" }
 *     responses:
 *       200:
 *         description: Успешный вход (устанавливает cookie)
 *       401:
 *         description: Неверный логин или пароль
 *       422:
 *         description: Ошибка валидации
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
 *     tags: [Auth]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [login, password, last_name, first_name, position]
 *             properties:
 *               login: { type: string, example: "new_supervisor" }
 *               password: { type: string, example: "password123" }
 *               last_name: { type: string, example: "Сидоров" }
 *               first_name: { type: string, example: "Сидор" }
 *               middle_name: { type: string, example: "Сидорович" }
 *               position: { type: string, example: "Профессор" }
 *               phone_number: { type: string, example: "+79991234567" }
 *               email: { type: string, example: "sidorov@mail.ru" }
 *     responses:
 *       201:
 *         description: Успешная регистрация
 *       422:
 *         description: Ошибка валидации (неверный формат данных)
 *       500:
 *         description: Ошибка сервера (например, логин уже занят)
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
 *     tags: [Auth]
 *     responses:
 *       200:
 *         description: Возвращает приветствие с именем пользователя или просьбу войти
 */
router.get("/", controller.get_redirect_home);

module.exports = router;