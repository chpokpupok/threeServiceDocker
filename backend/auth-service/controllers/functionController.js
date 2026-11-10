const pool = require("../db");
const queries = require("../queries");

//КАМИЛЬ
//ФУНКЦИЯ ПРОСМОТРА КУРСОВЫХ РАБОТ
const get_courseworks_for_teacher = (req, res) => {
    const supervisor_id = req.body.supervisor_id || req.params.supervisor_id;

    pool.query(
        queries.get_courseworks_for_teacher,
        [supervisor_id],
        (error, results) => {
            if (error) {
                console.error("Ошибка при получении курсовых:", error);
                return res.status(400).json({ error: error.message });
            }
            res.status(200).json({
                success: true,
                count: results.rows.length,
                data: results.rows
            });
        }
    );
};

//ФУНКЦИЯ ГЕНЕРАЦИИ ЛОГИНОВ И ПАРОЛЕЙ
const get_student_credentials = (req, res) => {
    pool.query(queries.generate_student_credentials, (error, results) => {
        if (error) {
            console.error("Ошибка при генерации логинов:", error);
            return res.status(400).json({ error: error.message });
        }
        res.status(200).json({
            success: true,
            count: results.rows.length,
            data: results.rows
        });
    });
};

// ФУНКЦИЯ СОЗДАНИЯ ОТЧЕТА (ФИО, ТЕМА, ДАТА)
const get_coursework_report = (req, res) => {
    pool.query(queries.create_coursework_report, (error, results) => {
        if (error) {
            console.error("Ошибка при создании отчета:", error);
            return res.status(400).json({ error: error.message });
        }
        res.status(200).json({
            success: true,
            count: results.rows.length,
            data: results.rows
        });
    });
};

// ФУНКЦИЯ ПОЛУЧЕНИЯ КУРСОВЫХ РАБОТ СТУДЕНТА
const get_courseworks_for_student = (req, res) => {
    const student_id = req.body.student_id || req.params.student_id;

    if (!student_id) {
        return res.status(400).json({ 
            error: "student_id обязателен" 
        });
    }

    pool.query(
        queries.get_courseworks_for_student, // Нужно создать этот запрос в queries.js
        [student_id],
        (error, results) => {
            if (error) {
                console.error("Ошибка при получении курсовых студента:", error);
                return res.status(400).json({ error: error.message });
            }
            res.status(200).json({
                success: true,
                count: results.rows.length,
                data: results.rows
            });
        }
    );
};

module.exports = {
    //камиль
    get_courseworks_for_teacher,
    get_student_credentials,
    get_coursework_report,
    get_courseworks_for_student
};