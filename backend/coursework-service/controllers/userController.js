require("dotenv").config();

const express = require("express");

const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const pool = require("../db");
const queries = require('../queries.js');
const { validationResult, check } = require("express-validator");
const { body } = require('express-validator');

const secretKey = process.env.secretKey; // получение значения secretKey из файла .env

//extended: true означает, что req.body может содержать любые значения, extended: false - только строки.
//urlencoded({ extended: true })); //обрабатывает данные формы и добавляет их в объект req.body
const urlencodedParser = express.urlencoded({ extended: true }); //создадим отдельный экземпляр urlencodedParser для обработки данных из формы внутри функции getLogin



//проверка login преподавателя
const validate_supervisor_login_data = [

body("login")
  .notEmpty().withMessage("Логин не должен быть пустым")
  .isLength({ min: 4, max: 20 }).withMessage("Логин должен содержать от 4 до 20 символов")
  .matches(/^[a-zA-Z0-9_.]+$/).withMessage("Логин может содержать только латинские буквы, цифры, точку и подчеркивание"),

  body("password")
  .isLength({ min: 6 })
  .withMessage("Поле Пароль не должно быть пустым и должно содержать не менее 6 символов")
];


// Аутентификация преподавателя
//не забудьте использовать async/await при выполнении асинхронных операций, таких как запрос к базе данных
const get_supervisor_login = async (req, res) => {
  const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(422).json({ errors: errors.array() });
      }
  // применим парсер для обработки данных из формы
  urlencodedParser(req, res, async () => {
    //const { username, password } = req.body;
    let username = req.body.login;
    let password = req.body.password;

    try {
      const result = await pool.query(
        "SELECT * FROM scientific_supervisor WHERE login = $1",
        [username]
      );
      const user = result.rows[0];

      if (!user) {
        console.warn(`Неудачная попытка входа для пользователя: ${username}`);
        return res
          .status(401)
          .json({ message: "Пользователь не найден или неверный пароль" });
      }

      //const hashedPassword = await bcrypt.hash(user.password, 10);
      const passwordMatch = await bcrypt.compare(password, user.password);

      if (!passwordMatch) {
        console.warn(`Неверный пароль для пользователя: ${username}`);
        return res
          .status(401)
          .json({ message: "Пользователь не найден или неверный пароль" });
      }

      // Если успешная аутентификация
      // Генерация JWT токена и добавление его срока действия через  параметр expiresIn равному 2 часа
      const token = jwt.sign({ userId: user.id }, secretKey, {
        expiresIn: "2h",
      });

      // Сохраняем токен в cookie на 2 часа, maxAge задается в миллисекундах
      res.cookie("token", token, {
        httpOnly: true,
        maxAge: 2 * 60 * 60 * 1000,
        sameSite: "None",
        secure: true,
      });

      // Сохраняем в cookie также имя пользователя на 2 часа
      res.cookie("username", username, {
        maxAge: 2 * 60 * 60 * 1000,
        sameSite: "None",
        secure: true,
      });

      return res
          .status(200)
          .json({ message: "Успешная аутентификация!" });

    } catch (error) {
      console.error("Ошибка при аутентификации", error);
      res.status(500).send("Ошибка при аутентификации");
    }
  });
};

//проверка студента
const validate_student_login_data = [

  body("login")
  .notEmpty().withMessage("Логин не должен быть пустым")
  .isLength({ min: 5, max: 16 }).withMessage("Логин должен содержать от 5 до 16 символов")
  .matches(/^[a-zA-Zа-яА-ЯёЁ0-9_.]+$/).withMessage("Логин может содержать только латинские, русские буквы, цифры, точку и подчеркивание"),

  body("password")
  .isLength({ min: 14, max: 14 })
  .withMessage("Поле Пароль не должно быть пустым и должно содержать 14 символов")
];

// Аутентификация студента
//не забудьте использовать async/await при выполнении асинхронных операций, таких как запрос к базе данных
const get_student_login = async (req, res) => {
  const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(422).json({ errors: errors.array() });
      }
  // применим парсер для обработки данных из формы
  urlencodedParser(req, res, async () => {
    //const { username, password } = req.body;
    let username = req.body.login;
    let password = req.body.password;

    try {
      const result = await pool.query(
        "SELECT * FROM student WHERE login = $1",
        [username]
      );
      const user = result.rows[0];

      if (!user) {
        console.warn(`Неудачная попытка входа для пользователя: ${username}`);
        return res
          .status(401)
          .json({ message: "Пользователь не найден или неверный пароль" });
      }

      const passwordMatch = (password == user.password)?true:false;

      if (!passwordMatch) {
        console.warn(`Неверный пароль для пользователя: ${username}`);
        return res
          .status(401)
          .json({ message: "Пользователь не найден или неверный пароль" });
      }

      // Если успешная аутентификация
      // Генерация JWT токена и добавление его срока действия через  параметр expiresIn равному 2 часа
      const token = jwt.sign({ userId: user.id }, secretKey, {
        expiresIn: "2h",
      });

      // Сохраняем токен в cookie на 2 часа, maxAge задается в миллисекундах
      res.cookie("token", token, {
        httpOnly: true,
        maxAge: 2 * 60 * 60 * 1000,
        sameSite: "None",
        secure: true,
      });

      // Сохраняем в cookie также имя пользователя на 2 часа
      res.cookie("username", username, {
        maxAge: 2 * 60 * 60 * 1000,
        sameSite: "None",
        secure: true,
      });

      return res
          .status(200)
          .json({ message: "Успешная аутентификация!" });

    } catch (error) {
      console.error("Ошибка при аутентификации", error);
      res.status(500).send("Ошибка при аутентификации");
    }
  });
};


const get_redirect_home = async (req, res) => {
  // Проверяем наличие cookie с токеном и именем пользователя
  if (req.cookies && req.cookies.token && req.cookies.username) {
    const user = req.cookies.username; // Извлекаем имя пользователя из cookie
    res.send(`С возвращением, ${user}!`);
  } else {
    res.send("Пожалуйста, войдите в систему, чтобы просмотреть эту страницу!");
  }
};


//проверка регирации препода
const validate_supervisor_data = [

  body("login")
  .notEmpty().withMessage("Логин не должен быть пустым")
  .isLength({ min: 4, max: 20 }).withMessage("Логин должен содержать от 4 до 20 символов")
  .matches(/^[a-zA-Z0-9_.]+$/).withMessage("Логин может содержать только латинские буквы, цифры, точку и подчеркивание"),

  body("password")
  .isLength({ min: 6 })
  .withMessage("Поле Пароль не должно быть пустым и должно содержать не менее 6 символов"),

  body("last_name")
  .isAlpha("ru-RU", { ignore: " " })
  .withMessage("Поле Фамилия не должно быть пустым и должно содержать только буквы русского алфавита"),

  body("first_name")
  .isAlpha("ru-RU", { ignore: " " })
  .withMessage("Поле Имя не должно быть пустым и должно содержать только буквы русского алфавита"),

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



//регистрация препода
const register_supervisor = async (req, res) => {

    const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(422).json({ errors: errors.array() });
      }

    const {login, password, last_name, first_name, middle_name, position, phone_number, email} = req.body; // извлекаем данные из тела объекта
    const hashed_password = await bcrypt.hash(password, 10);
    pool.query(
        queries.register_supervisor,
        [login, hashed_password, last_name, first_name, middle_name, position, phone_number, email],
        (error, results) => {
        if (error) return res.status(500).json({ error: "Ошибка сервера", details: error.message }); //если есть ошибка, то вывести сообщение об ошибке
        res.status(201).send("Superviser insered");
        }
    );
};



// экспортируем модуль как объект, в котором будет несколько функций
module.exports = {
  get_supervisor_login,
  get_student_login,
  get_redirect_home,
  register_supervisor,
  validate_supervisor_data,
  validate_supervisor_login_data,
  validate_student_login_data
};