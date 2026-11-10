const { Router } = require("express");
const controller = require("../controllers/queryController");

// создаем объект маршрутизатор
const router = Router();

// добавляем маршруты для запросов
router.get("/", controller.get_faculty); // вызов метода getCooperator (находиться в файле controller.js) для объекта controller
router.get("/:id", controller.get_faculty_by_id);
router.post("/", controller.add_faculty);
router.put("/:id", controller.update_faculty);
router.delete("/:id", controller.delete_faculty);

// экспортируем маршрутизатор на server
module.exports = router;