const { Router } = require("express");
const controller = require("../controllers/procedureController");

// создаем объект маршрутизатор
const router = Router();

// добавляем маршруты для хранимых процедур
//сафира
router.put("/update_supervisor/", controller.validate_up_supervisor_data, controller.update_supervisor);
router.delete("/delete_supervisor/", controller.validate_del_supervisor_data, controller.delete_supervisor);
router.post("/insert_discipline/", controller.insert_discipline);
router.put("/update_discipline/", controller.update_discipline);
router.delete("/delete_discipline/", controller.delete_discipline);
router.post("/insert_group/", controller.insert_group);
router.put("/update_group/", controller.update_group);
router.delete("/delete_group/", controller.delete_group);
router.post("/insert_department/", controller.insert_department);

//камиль
router.put("/update_coursework", controller.validate_up_coursework_data, controller.update_coursework_teacher);
router.post("/add_supervisor_department", controller.add_supervisor_department);
router.delete("/remove_supervisor_department/:link_id", controller.remove_supervisor_department);

//артем

router.delete('/delete_department', controller.delete_department);
router.put('/update_department', controller.update_department);
router.post('/insert_faculty', controller.insert_faculty);
router.delete('/delete_faculty', controller.delete_faculty);
router.put('/update_faculty', controller.update_faculty);
router.post('/add_students_to_group', controller.add_students_to_group);
router.put('/update_student', controller.validate_up_student_data, controller.update_student);
router.post('/insert_coursework', controller.validate_ins_coursework_data, controller.insert_course_work);
router.delete('/delete_coursework', controller.delete_course_work);
router.put('/update_coursework_student', controller.validate_link_data, controller.update_course_work_student);

// экспортируем маршрутизатор на server
module.exports = router;

