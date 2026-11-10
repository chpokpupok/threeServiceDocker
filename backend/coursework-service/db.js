require("dotenv").config(); // библиотека dotenv загружает переменные среды из файла .env

// создание нового подключения к базе данных PostgreSQL с использованием модуля Pool из библиотеки pg.
const { Pool } = require("pg"); 

const pool = new Pool({
  user: process.env.DB_USER,
  host: process.env.DB_HOST,
  database: process.env.DB_DATABASE,
  password: process.env.DB_PASSWORD,
  port: process.env.DB_PORT,
}); // Конфигурационные параметры для подключения к базе данных берутся из переменных среды файла .env с помощью библиотеки dotenv.

module.exports = pool; //  созданное подключение к базе данных экспортируется из модуля как объект pool, который может быть использован в других частях кода для выполнения запросов к базе данных.
