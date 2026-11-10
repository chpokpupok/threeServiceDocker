//импорт пула БД
const pool = require("../db");
const queries = require("../queries");
const { validationResult, check } = require("express-validator");
const { body } = require('express-validator');
const bcrypt = require("bcrypt");

//проверка обновления препода
const validate_up_supervisor_data = [

  body("login")
  .notEmpty().withMessage("Логин не должен быть пустым")
  .isLength({ min: 4, max: 20 }).withMessage("Логин должен содержать от 4 до 20 символов")
  .matches(/^[a-zA-Z0-9_.]+$/).withMessage("Логин может содержать только латинские буквы, цифры, точку и подчеркивание"),

  body("password")
  .optional()
  .if((value) => value !== null && value !== undefined)
  .isLength({ min: 6 })
  .withMessage("Поле Пароль должно содержать не менее 6 символов"),

  body("last_name")
  .matches(/^[а-яёА-ЯЁs]*$/).withMessage("Поле Фамилия должно содержать только буквы русского алфавита"),

  body("first_name")
  .matches(/^[а-яёА-ЯЁs]*$/).withMessage("Поле Имя должно содержать только буквы русского алфавита"),

  body("middle_name")
  .matches(/^[а-яёА-ЯЁs]*$/).withMessage("Поле Отчество должно содержать только буквы русского алфавита"),

 body("position")
  .matches(/^[а-яёА-ЯЁ .]*$/).withMessage("Поле Должность должно содержать только буквы русского алфавита, пробелы и точки"),

  body("phone_number")
  .optional()
  .if((value) => value !== null && value !== undefined)
  .isLength({ min: 12, max: 12 }).withMessage("Номер телефона должен содержать 12 символов")
  .custom((value) => {
    // Дополнительная проверка на валидность
    if (!value.startsWith('+7')) {
      throw new Error('Номер должен начинаться с +7');
    }
    return true;
  }),
  
  body("email")
  .optional()
  .if((value) => value !== null && value !== undefined)
  .isEmail()
  .withMessage("Поле email должно содержать валидный email")

];

//обновление препода
const update_supervisor = async (req, res) => {

    const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(422).json({ errors: errors.array() });
      }

    const {login, password, last_name, first_name, middle_name, position, phone_number, email} = req.body; // извлекаем данные из тела объекта
    const hashed_password = (password == null)?null:await bcrypt.hash(password, 10);
    pool.query(
        queries.update_supervisor,
        [login, hashed_password, last_name, first_name, middle_name, position, phone_number, email],
        (error, results) => {
        if (error) return res.status(500).json({ error: "Ошибка сервера", details: error.message }); //если есть ошибка, то вывести сообщение об ошибке
        res.status(201).send("Superviser updated");
        }
    );
};

//проверка удаления препода
const validate_del_supervisor_data = [

  body("login")
  .notEmpty().withMessage("Логин не должен быть пустым")
  .isLength({ min: 4, max: 20 }).withMessage("Логин должен содержать от 4 до 20 символов")
  .matches(/^[a-zA-Z0-9_.]+$/).withMessage("Логин может содержать только латинские буквы, цифры, точку и подчеркивание"),

];

//удаление препода
const delete_supervisor = async (req, res) => {

    const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(422).json({ errors: errors.array() });
      }

    const {login} = req.body; // извлекаем данные из тела объекта

    pool.query(
        queries.delete_supervisor,
        [login],
        (error, results) => {
        if (error) return res.status(500).json({ error: "Ошибка сервера", details: error.message }); //если есть ошибка, то вывести сообщение об ошибке
        res.status(201).send("Superviser deleted");
        }
    );
};

//добавление дисциплины
const insert_discipline = async (req, res) => {

    const {name, department, semester} = req.body; // извлекаем данные из тела объекта

    pool.query(
        queries.insert_discipline,
        [name, department, semester],
        (error, results) => {
        if (error) return res.status(500).json({ error: "Ошибка сервера", details: error.message }); //если есть ошибка, то вывести сообщение об ошибке
        res.status(201).send("Discipline insered");
        }
    );
};

//обновление дисциплины
const update_discipline = async (req, res) => {

    const {old_name, old_department, new_name, new_department, semester} = req.body; // извлекаем данные из тела объекта

    pool.query(
        queries.update_discipline,
        [old_name, old_department, new_name, new_department, semester],
        (error, results) => {
        if (error) return res.status(500).json({ error: "Ошибка сервера", details: error.message }); //если есть ошибка, то вывести сообщение об ошибке
        res.status(201).send("Discipline updated");
        }
    );
};

//удаление дисциплины
const delete_discipline = async (req, res) => {

    const {name, department} = req.body; // извлекаем данные из тела объекта

    pool.query(
        queries.delete_discipline,
        [name, department],
        (error, results) => {
        if (error) return res.status(500).json({ error: "Ошибка сервера", details: error.message }); //если есть ошибка, то вывести сообщение об ошибке
        res.status(201).send("Discipline deleted");
        }
    );
};

//добавление группы
const insert_group = async (req, res) => {

    const {name, year_of_admission, department, education_type, study_duration} = req.body; // извлекаем данные из тела объекта

    pool.query(
        queries.insert_group,
        [name, year_of_admission, department, education_type, study_duration],
        (error, results) => {
        if (error) return res.status(500).json({ error: "Ошибка сервера", details: error.message }); //если есть ошибка, то вывести сообщение об ошибке
        res.status(201).send("Group insered");
        }
    );
};

//обновление группы
const update_group = async (req, res) => {

    const {name, year_of_admission, department, education_type, study_duration} = req.body; // извлекаем данные из тела объекта

    pool.query(
        queries.update_group,
        [name, year_of_admission, department, education_type, study_duration],
        (error, results) => {
        if (error) return res.status(500).json({ error: "Ошибка сервера", details: error.message }); //если есть ошибка, то вывести сообщение об ошибке
        res.status(201).send("Group updated");
        }
    );
};

//удаление группы
const delete_group = async (req, res) => {

    const {name} = req.body; // извлекаем данные из тела объекта

    pool.query(
        queries.delete_group,
        [name],
        (error, results) => {
        if (error) return res.status(500).json({ error: "Ошибка сервера", details: error.message }); //если есть ошибка, то вывести сообщение об ошибке
        res.status(201).send("Group deleted");
        }
    );
};

//добавление кафедры
const insert_department = async (req, res) => {

    const {name, faculty, head, phone} = req.body; // извлекаем данные из тела объекта

    pool.query(
        queries.insert_department,
        [name, faculty, head, phone],
        (error, results) => {
        if (error) return res.status(500).json({ error: "Ошибка сервера", details: error.message }); //если есть ошибка, то вывести сообщение об ошибке
        res.status(201).send("Department insered");
        }
    );
};

//КАМИЛЬ
//ПРОЦЕДУРА ИЗМЕНЕНИЯ КУРСОВОЙ РАБОТЫ

//проверка
const validate_up_coursework_data = [

  body("submission_date")
  .optional()
  .if((value) => value !== null && value !== undefined)
  .matches(/^\d{2}[./-]\d{2}[./-]\d{4}$/).withMessage("Неверный формат даты"),

  body("defense_date")
  .optional()
  .if((value) => value !== null && value !== undefined)
  .matches(/^\d{2}[./-]\d{2}[./-]\d{4}$/).withMessage("Неверный формат даты"),

  body("topic_status")
  .matches(/^[а-яёА-ЯЁ -]*$/).withMessage("Поле Статус темы должно содержать только буквы русского алфавита, пробелы"),

  body("work_link")
  .optional()
  .if((value) => value !== null && value !== undefined)
  .isURL({
    protocols: ['http', 'https'], // Разрешаем только HTTP и HTTPS
    require_protocol: true, // Требуем указание протокола
    require_valid_protocol: true, // Протокол должен быть из разрешенных
    allow_protocol_relative_urls: false // Запрещаем относительные URL без протокола
  })
  .withMessage("Неверный адрес"),

  body("antiplagiat_level")
  .optional()
  .if((value) => value !== null && value !== undefined)
  .isInt({ min: 0, max: 100 })
  .withMessage('Уровень антиплагиата должен быть целым числом от 0 до 100'),

  body("theory_grade")
  .optional()
  .if((value) => value !== null && value !== undefined)
  .isFloat({ min: 0, max: 5 })
  .withMessage('theory_grade должен быть числом от 0 до 5'),

  body("practice_grade")
  .optional()
  .if((value) => value !== null && value !== undefined)
  .isFloat({ min: 0, max: 5 })
  .withMessage('practice_grade должен быть числом от 0 до 5'),

  body("design_grade")
  .optional()
  .if((value) => value !== null && value !== undefined)
  .isFloat({ min: 0, max: 5 })
  .withMessage('design_grade должен быть числом от 0 до 5'),

  body("defense_grade")
  .optional()
  .if((value) => value !== null && value !== undefined)
  .isFloat({ min: 0, max: 5 })
  .withMessage('defense_grade должен быть числом от 0 до 5'),

  body("work_status")
  .matches(/^[а-яёА-ЯЁ -]*$/).withMessage("Поле Статус работы должно содержать только буквы русского алфавита, пробелы")
];

const update_coursework_teacher = async (req, res) => {

    const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(422).json({ errors: errors.array() });
      }

    const {
        coursework_id, topic, submission_date, defense_date, topic_status,
        work_link, antiplagiat_level, theory_grade, practice_grade,
        design_grade, defense_grade, commission, work_status, comment
    } = req.body;

    pool.query(
        queries.update_coursework_teacher,
        [
            coursework_id, topic, submission_date, defense_date, topic_status,
            work_link, antiplagiat_level, theory_grade, practice_grade,
            design_grade, defense_grade, commission, work_status, comment
        ],
        (error, results) => {
            if (error) {
                console.error("Ошибка при обновлении курсовой:", error);
                return res.status(400).json({ error: error.message });
            }
            res.status(200).json({ 
                success: true, 
                message: "Курсовая работа обновлена успешно" 
            });
        }
    );
};


//ПРОЦЕДУРА ДОБАВЛЕНИЯ В КАФЕДРУ ПРЕПОДАВАТЕЛЯ
const add_supervisor_department = async (req, res) => {
    const { supervisor_login, department_name } = req.body;

    pool.query(
        queries.add_supervisor_department,
        [supervisor_login, department_name],
        (error, results) => {
            if (error) {
                console.error("Ошибка при добавлении в кафедру:", error);
                return res.status(400).json({ error: error.message });
            }
            res.status(201).json({
                success: true,
                message: "Преподаватель добавлен к кафедре успешно"
            });
        }
    );
};

// ПРОЦЕДУРА УДАЛЕНИЯ ИЗ КАФЕДРЫ ПРЕПОДАВАТЕЛЯ
const remove_supervisor_department = async (req, res) => {
    const link_id = req.params.link_id || req.body.link_id;

    pool.query(
        queries.remove_supervisor_department,
        [link_id],
        (error, results) => {
            if (error) {
                console.error("Ошибка при удалении из кафедры:", error);
                return res.status(400).json({ error: error.message });
            }
            res.status(200).json({
                success: true,
                message: "Преподаватель удален из кафедры успешно"
            });
        }
    );
};

//артем

const delete_department = async (req, res) => {
  const { name } = req.body;
  try {
    await pool.query(queries.delete_department, [name]);
    res.status(200).json({ success: true, message: 'Department deleted' });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
};

const update_department = async (req, res) => {
  const { old_name, new_name, faculty, head, phone } = req.body;
  try {
    await pool.query(queries.update_department, [old_name, new_name, faculty, head, phone]);
    res.status(200).json({ success: true, message: 'Department updated' });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
};

const insert_faculty = async (req, res) => {
  const { name, dean, phone } = req.body;
  try {
    await pool.query(queries.insert_faculty, [name, dean, phone]);
    res.status(201).json({ success: true, message: 'Faculty inserted' });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
};

const delete_faculty = async (req, res) => {
  const { name } = req.body;
  try {
    await pool.query(queries.delete_faculty, [name]);
    res.status(200).json({ success: true, message: 'Faculty deleted' });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
};

const update_faculty = async (req, res) => {
  const { old_name, new_name, dean, phone } = req.body;
  try {
    await pool.query(queries.update_faculty, [old_name, new_name, dean, phone]);
    res.status(200).json({ success: true, message: 'Faculty updated' });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
};

const add_students_to_group = async (req, res) => {
  const { group_name, students_list } = req.body;
  try {
    await pool.query(queries.add_students_to_group, [group_name, students_list]);
    res.status(201).json({ success: true, message: 'Students added to group' });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
};

//проверка
const validate_up_student_data = [

  body("student_login")
  .notEmpty().withMessage("Логин не должен быть пустым")
  .isLength({ min: 5, max: 16 }).withMessage("Логин должен содержать от 5 до 16 символов")
  .matches(/^[a-zA-Zа-яА-ЯёЁ0-9_.]+$/).withMessage("Логин может содержать только латинские буквы, цифры, точку и подчеркивание"),

  body("phone")
  .optional()
  .if((value) => value !== null && value !== undefined)
  .isLength({ min: 12, max: 12 }).withMessage("Номер телефона должен содержать 12 символов")
  .custom((value) => {
    // Дополнительная проверка на валидность
    if (!value.startsWith('+7')) {
      throw new Error('Номер должен начинаться с +7');
    }
    return true;
  }),
  
  body("email")
  .optional()
  .if((value) => value !== null && value !== undefined)
  .isEmail()
  .withMessage("Поле email должно содержать валидный email")

];

const update_student = async (req, res) => {
  const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(422).json({ errors: errors.array() });
      }
  
  const { student_login, phone, email, leader } = req.body;
  try {
    await pool.query(queries.update_student, [student_login, phone, email, leader]);
    res.status(200).json({ success: true, message: 'Student updated' });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
};

//проверка
const validate_ins_coursework_data = [

  body("student_login")
  .notEmpty().withMessage("Логин не должен быть пустым")
  .isLength({ min: 5, max: 16 }).withMessage("Логин должен содержать от 5 до 16 символов")
  .matches(/^[a-zA-Zа-яА-ЯёЁ0-9_.]+$/).withMessage("Логин может содержать только латинские буквы, цифры, точку и подчеркивание"),

  body("supervisor_login")
  .notEmpty().withMessage("Логин не должен быть пустым")
  .isLength({ min: 4, max: 20 }).withMessage("Логин должен содержать от 4 до 20 символов")
  .matches(/^[a-zA-Z0-9_.]+$/).withMessage("Логин может содержать только латинские, русские буквы, цифры, точку и подчеркивание"),

  body("year")
  .matches(/^(\d{2}[./-]\d{2}[./-]\d{4}|\d{4}-\d{2}-\d{2})$/).withMessage("Неверный формат даты")
];


const insert_course_work = async (req, res) => {
  const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(422).json({ errors: errors.array() });
      }

  const { student_login, supervisor_login, discipline_name, topic, year } = req.body;
  try {
    await pool.query(queries.insert_coursework, [student_login, supervisor_login, discipline_name, topic, year]);
    res.status(201).json({ success: true, message: 'Coursework created' });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
};

const delete_course_work = async (req, res) => {
  const { coursework_id } = req.body;
  try {
    await pool.query(queries.delete_coursework, [coursework_id]);
    res.status(200).json({ success: true, message: 'Coursework deleted' });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
};


const validate_link_data = [
body("work_link")
  .isURL({
    protocols: ['http', 'https'], // Разрешаем только HTTP и HTTPS
    require_protocol: true, // Требуем указание протокола
    require_valid_protocol: true, // Протокол должен быть из разрешенных
    allow_protocol_relative_urls: false // Запрещаем относительные URL без протокола
  })
  .withMessage("Неверный адрес"),
];

const update_course_work_student = async (req, res) => {
  const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(422).json({ errors: errors.array() });
      }

  const { coursework_id, topic, work_link } = req.body;
  try {
    await pool.query(queries.update_coursework_student, [coursework_id, topic, work_link]);
    res.status(200).json({ success: true, message: 'Coursework updated by student' });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
};

module.exports = {
  validate_up_supervisor_data,
  validate_del_supervisor_data,
  validate_up_coursework_data,
  validate_up_student_data,
  validate_ins_coursework_data,
  validate_link_data,


  update_supervisor,
  delete_supervisor,
  insert_discipline,
  update_discipline,
  delete_discipline,
  insert_group,
  update_group,
  delete_group,
  insert_department,

  //камиль
  update_coursework_teacher,
  add_supervisor_department,
  remove_supervisor_department,

  //артем
  update_course_work_student,
  delete_course_work,
  insert_course_work,
  update_student,
  add_students_to_group,
  update_faculty,
  delete_faculty,
  insert_faculty,
  update_department,
  delete_department

};
