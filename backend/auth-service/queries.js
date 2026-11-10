// queries.js

// SELECT
const get_faculty = "SELECT * FROM faculty";
const get_faculty_by_id = "SELECT * FROM faculty WHERE faculty_id = $1";

// INSERT
const add_faculty =
  "INSERT INTO faculty (name, dean, phone_number) VALUES ($1, $2, $3) RETURNING *";


//ПРОЦЕДУРЫ САФИРА
const register_supervisor = "call register_supervisor($1, $2, $3, $4, $5, $6, $7, $8)"; 
const update_supervisor = "call update_supervisor($1, $2, $3, $4, $5, $6, $7, $8)";
const delete_supervisor = "call delete_supervisor($1)";
const insert_discipline = "call insert_discipline($1, $2, $3)";
const update_discipline = "call update_discipline($1, $2, $3, $4, $5)";
const delete_discipline = "call delete_discipline($1, $2)";
const insert_group = "call insert_group($1, $2, $3, $4, $5)";
const update_group = "call update_group($1, $2, $3, $4, $5)";
const delete_group = "call delete_group($1)";
const insert_department = "call insert_department($1, $2, $3, $4)";

//ПРОЦЕДУРЫ КАМИЛЬ
const update_coursework_teacher = "CALL update_coursework_teacher($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)";
const add_supervisor_department = "CALL add_supervisor_department($1, $2)";
const remove_supervisor_department = "CALL remove_supervisor_department($1)";

//ПОЛЬЗОВАТЕЛЬСКИЕ ФУНКЦИИ КАМИЛЬ
const get_courseworks_for_teacher = "SELECT * FROM get_courseworks_for_teacher($1)";
const generate_student_credentials = "SELECT * FROM generate_student_credentials()";
const create_coursework_report = "SELECT * FROM create_coursework_report()";

//ОБЫЧНЫЕ ЗАПРОСЫ ДЛЯ ТАБЛИЦЫ COURSEWORK КАМИЛЬ
const get_courseworks = "SELECT * FROM coursework";
const get_coursework_by_id = "SELECT * FROM coursework WHERE coursework_id = $1";
const add_coursework = "INSERT INTO coursework (student_id, supervisor_id, discipline_id, topic) VALUES ($1, $2, $3, $4)";
const update_coursework = "UPDATE coursework SET topic = $1 WHERE coursework_id = $2";
const remove_coursework = "DELETE FROM coursework WHERE coursework_id = $1";

const get_courseworks_for_student = "SELECT * FROM coursework WHERE student_id = $1";

//артем процедуры

const delete_department = 'CALL delete_department($1)';
const update_department = 'CALL update_department($1, $2, $3, $4, $5)';
const insert_faculty = 'CALL insert_faculty($1, $2, $3)';
const delete_faculty = 'CALL delete_faculty($1)';
const update_faculty = 'CALL update_faculty($1, $2, $3, $4)';
const add_students_to_group = 'CALL add_students_to_group($1, $2::TEXT[])';
const update_student = 'CALL update_student($1, $2, $3, $4)';
const insert_coursework = 'CALL insert_coursework($1, $2, $3, $4, $5)';
const delete_coursework = 'CALL delete_coursework($1)';
const update_coursework_student = 'CALL update_coursework_student($1, $2, $3)';



module.exports = {
  get_faculty,
  get_faculty_by_id,
  add_faculty,
  update_faculty,
  delete_faculty,

  //сафира
  register_supervisor,
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

  get_courseworks_for_teacher,
  generate_student_credentials,
  create_coursework_report,

  get_courseworks,
  get_coursework_by_id,
  add_coursework,
  update_coursework,
  remove_coursework,

  get_courseworks_for_student,

  //артем

  delete_department,
  update_department,
  insert_faculty,
  add_students_to_group,
  update_student,
  insert_coursework,
  delete_coursework,
  update_coursework_student
};