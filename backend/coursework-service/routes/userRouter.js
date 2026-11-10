const { Router } = require("express");
const controller = require("../controllers/userController"); //импортируйте контроллер

// создаем объект маршрутизатор
const router = Router();

// добавляем маршруты
router.post("/login_supervisor", controller.validate_supervisor_login_data, controller.get_supervisor_login);
router.post("/login_student", controller.validate_student_login_data, controller.get_student_login);
router.post("/register", controller.validate_supervisor_data, controller.register_supervisor);

//создание домашнего маршрута, который будет выводить имя пользователя.
// http://localhost:3001/home
router.get("/", controller.get_redirect_home);

// экспортируем маршрутизатор на server
module.exports = router;
