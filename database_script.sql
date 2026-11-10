
DROP TABLE IF EXISTS supervisor_department CASCADE;
DROP TABLE IF EXISTS coursework_plan CASCADE;
DROP TABLE IF EXISTS coursework CASCADE;
DROP TABLE IF EXISTS discipline CASCADE;
DROP TABLE IF EXISTS scientific_supervisor CASCADE;
DROP TABLE IF EXISTS student CASCADE;
DROP TABLE IF EXISTS "group" CASCADE;
DROP TABLE IF EXISTS department CASCADE;
DROP TABLE IF EXISTS faculty CASCADE;

-- Создание таблицы Факультет (Faculty)
CREATE TABLE faculty (
    faculty_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(100) NOT NULL,
    dean VARCHAR(150),
    phone_number CHAR(12) CHECK (phone_number ~ '^\+7\d{10}$') UNIQUE
);

-- Создание таблицы Кафедра (Department)
CREATE TABLE department (
    department_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(100) NOT NULL UNIQUE,
    faculty_id INTEGER NOT NULL,
    head VARCHAR(150),
    phone CHAR(12) CHECK (phone ~ '^\+7\d{10}$') UNIQUE,
    
    CONSTRAINT fk_department_faculty 
        FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id) ON DELETE RESTRICT
);

-- Создание таблицы Группа (Group)
CREATE TABLE "group" (
    group_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(15) NOT NULL UNIQUE,
    year_of_admission DATE NOT NULL CHECK (EXTRACT(YEAR FROM year_of_admission) >= EXTRACT(YEAR FROM CURRENT_DATE) - 5),
    department_id INTEGER NOT NULL,
    education_type VARCHAR(12) CHECK (education_type IN ('очная', 'заочная', 'очно-заочная')) DEFAULT 'очная',
    study_duration NUMERIC(1,0) CHECK (study_duration > 0) DEFAULT 4,
    course NUMERIC(1,0),
    
    CONSTRAINT fk_group_department 
        FOREIGN KEY (department_id) REFERENCES department(department_id) ON DELETE RESTRICT
);
--trigger
CREATE OR REPLACE FUNCTION update_group_course()
RETURNS TRIGGER AS $$
BEGIN
    NEW.course := EXTRACT(YEAR FROM AGE(CURRENT_DATE, NEW.year_of_admission)) + 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_group_course
    BEFORE INSERT OR UPDATE ON "group"
    FOR EACH ROW
    EXECUTE FUNCTION update_group_course();

-- Создание таблицы Студент (Student)
CREATE TABLE student (
    student_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    login VARCHAR(20),
    password VARCHAR(14),
    last_name VARCHAR(36) NOT NULL CHECK (last_name ~ '^[А-Яа-яЁё\-]+$'),
    first_name VARCHAR(15) NOT NULL CHECK (first_name ~ '^[А-Яа-яЁё\-]+$'),
    middle_name VARCHAR(20) CHECK (middle_name ~ '^[А-Яа-яЁё\-]*$'),
    group_id INTEGER NOT NULL,
    phone_number CHAR(12) CHECK (phone_number ~ '^\+7\d{10}$') UNIQUE,
    email VARCHAR(254) CHECK (email ~ '^[^@]+@[^@]+\.(com|net|ru)$'),
    record_book CHAR(6) NOT NULL CHECK (record_book ~ '^\d{6}$') UNIQUE,
    leader BOOLEAN DEFAULT FALSE,
    
    CONSTRAINT fk_student_group 
        FOREIGN KEY (group_id) REFERENCES "group"(group_id) ON DELETE RESTRICT
);

--trigger
-- Функция для перевода русских букв в латиницу
CREATE OR REPLACE FUNCTION transliterate(input_string TEXT)
RETURNS TEXT AS $$
DECLARE
    result TEXT;
BEGIN
    result := LOWER(input_string);
    result := replace(result, 'а', 'a');
    result := replace(result, 'б', 'b');
    result := replace(result, 'в', 'v');
    result := replace(result, 'г', 'g');
    result := replace(result, 'д', 'd');
    result := replace(result, 'е', 'e');
    result := replace(result, 'ё', 'e');
    result := replace(result, 'ж', 'zh');
    result := replace(result, 'з', 'z');
    result := replace(result, 'и', 'i');
    result := replace(result, 'й', 'y');
    result := replace(result, 'к', 'k');
    result := replace(result, 'л', 'l');
    result := replace(result, 'м', 'm');
    result := replace(result, 'н', 'n');
    result := replace(result, 'о', 'o');
    result := replace(result, 'п', 'p');
    result := replace(result, 'р', 'r');
    result := replace(result, 'с', 's');
    result := replace(result, 'т', 't');
    result := replace(result, 'у', 'u');
    result := replace(result, 'ф', 'f');
    result := replace(result, 'х', 'kh');
    result := replace(result, 'ц', 'ts');
    result := replace(result, 'ч', 'ch');
    result := replace(result, 'ш', 'sh');
    result := replace(result, 'щ', 'shch');
    result := replace(result, 'ъ', '');
    result := replace(result, 'ы', 'y');
    result := replace(result, 'ь', '');
    result := replace(result, 'э', 'e');
    result := replace(result, 'ю', 'yu');
    result := replace(result, 'я', 'ya');
    return result;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Обновленный триггер генерации логинов и паролей
CREATE OR REPLACE FUNCTION generate_passwords()
RETURNS TRIGGER AS $$
BEGIN
    -- Генерируем логин: транслитерируем фамилию и имя в латиницу + ID
	NEW.login := transliterate(SUBSTRING(NEW.last_name FROM 1 FOR 8) || SUBSTRING(NEW.first_name FROM 1 FOR 3)) || 
        LPAD(NEW.student_id::TEXT, 3, '0');
    -- Генерируем пароль (MD5 и так выдает только латиницу и цифры)
    NEW.password := SUBSTRING(MD5(RANDOM()::TEXT) FROM 1 FOR 14);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_generate_passwords
    BEFORE INSERT ON student
    FOR EACH ROW
    EXECUTE FUNCTION generate_passwords();


-- Создание таблицы Научный руководитель (Scientific supervisor)
CREATE TABLE scientific_supervisor (
    supervisor_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    login VARCHAR(20) UNIQUE NOT NULL,
    password TEXT NOT NULL,
    last_name VARCHAR(36) NOT NULL CHECK (last_name ~ '^[А-Яа-яЁё\-]+$'),
    first_name VARCHAR(15) NOT NULL CHECK (first_name ~ '^[А-Яа-яЁё\-]+$'),
    middle_name VARCHAR(20) CHECK (middle_name ~ '^[А-Яа-яЁё\-]*$'),
    position VARCHAR(25) CHECK (position IN ('ассистент', 'преподаватель', 'старший преподаватель', 'доцент', 'профессор', 'зав. кафедрой')) DEFAULT 'преподаватель',
    phone_number CHAR(12) CHECK (phone_number ~ '^\+7\d{10}$') UNIQUE,
    email VARCHAR(254) CHECK (email ~ '^[^@]+@[^@]+\.(com|net|ru)$')
);

-- Создание таблицы Дисциплина (Discipline)
CREATE TABLE discipline (
    discipline_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name TEXT NOT NULL CHECK (name ~ '^[А-Яа-яЁё\s\-\()\,]+$'),
    department_id INTEGER NOT NULL,
    semester NUMERIC(2,0) NOT NULL CHECK (semester BETWEEN 1 AND 10),
    
    CONSTRAINT fk_discipline_department 
        FOREIGN KEY (department_id) REFERENCES department(department_id) ON DELETE RESTRICT
);

-- Создание таблицы Курсовая работа (Coursework)
CREATE TABLE coursework (
    coursework_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    submission_date DATE,
    defense_date DATE,
    topic TEXT NOT NULL,
    topic_status VARCHAR(20) NOT NULL CHECK (topic_status IN ('На проверке', 'Принята', 'Не принята', '-')) DEFAULT '-',
    work_link VARCHAR(255) CHECK (work_link ~ '^https?://'),
    antiplagiat_level NUMERIC(5,2) CHECK (antiplagiat_level BETWEEN 0 AND 100),
    theory_grade NUMERIC(2,0) CHECK (theory_grade BETWEEN 0 AND 5),
    practice_grade NUMERIC(2,0) CHECK (practice_grade BETWEEN 0 AND 5),
    design_grade NUMERIC(2,0) CHECK (design_grade BETWEEN 0 AND 5),
    defense_grade NUMERIC(2,0) CHECK (defense_grade BETWEEN 0 AND 5),
    final_grade NUMERIC(3,2) GENERATED ALWAYS AS (
        (COALESCE(theory_grade, 0) + COALESCE(practice_grade, 0) + COALESCE(design_grade, 0) + COALESCE(defense_grade, 0)) / 
        NULLIF(
            (CASE WHEN theory_grade IS NOT NULL THEN 1 ELSE 0 END) +
            (CASE WHEN practice_grade IS NOT NULL THEN 1 ELSE 0 END) +
            (CASE WHEN design_grade IS NOT NULL THEN 1 ELSE 0 END) +
            (CASE WHEN defense_grade IS NOT NULL THEN 1 ELSE 0 END), 
        0)
    ) STORED,
    commission TEXT[],
    work_status VARCHAR(15) NOT NULL CHECK (work_status IN ('-', 'Черновик', 'На проверке', 'Принята', 'Не принята', 'Защищена', 'Долг')) DEFAULT '-',
    comment TEXT
    
);

-- Создание таблицы План курсовых (Coursework Plan)
CREATE TABLE coursework_plan (
    plan_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    student_id INTEGER NOT NULL,
    supervisor_id INTEGER NOT NULL,
    discipline_id INTEGER NOT NULL,
    coursework_id INTEGER NOT NULL,
  coursework_year DATE NOT NULL,
    
    CONSTRAINT fk_plan_student 
        FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE RESTRICT,
    CONSTRAINT fk_plan_supervisor 
        FOREIGN KEY (supervisor_id) REFERENCES scientific_supervisor(supervisor_id) ON DELETE RESTRICT,
    CONSTRAINT fk_plan_discipline 
        FOREIGN KEY (discipline_id) REFERENCES discipline(discipline_id) ON DELETE RESTRICT,
    CONSTRAINT fk_plan_coursework 
        FOREIGN KEY (coursework_id) REFERENCES coursework(coursework_id) ON DELETE RESTRICT
);

CREATE TABLE supervisor_department (
    link_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    supervisor_id INTEGER NOT NULL,
    department_id INTEGER NOT NULL,
    
    CONSTRAINT fk_link_supervisor 
        FOREIGN KEY (supervisor_id) REFERENCES scientific_supervisor(supervisor_id) ON DELETE RESTRICT,
    CONSTRAINT fk_link_department 
        FOREIGN KEY (department_id) REFERENCES department(department_id) ON DELETE RESTRICT,
    CONSTRAINT uk_supervisor_department 
        UNIQUE (supervisor_id, department_id)
);


-- 1) Преподаватель — регистрация, изменение, удаление

CREATE OR REPLACE PROCEDURE register_supervisor(
  p_login VARCHAR(20),
  p_password VARCHAR(14),
  p_last_name VARCHAR(36),
  p_first_name VARCHAR(15),
  p_middle_name VARCHAR(20) DEFAULT NULL,
  p_position VARCHAR(25) DEFAULT NULL,
  p_phone_number CHAR(12) DEFAULT NULL,
  p_email VARCHAR(254) DEFAULT NULL
)
AS $$
BEGIN
  -- Проверка уникальности логина
  IF EXISTS (SELECT 1 FROM scientific_supervisor WHERE login = p_login) THEN
    RAISE EXCEPTION 'Login "%" already exists', p_login;
  END IF;

  INSERT INTO scientific_supervisor (login, password, last_name, first_name, middle_name, position, phone_number, email)
  VALUES (p_login, p_password, p_last_name, p_first_name, p_middle_name, p_position, p_phone_number, p_email);
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE PROCEDURE update_supervisor(
  p_login VARCHAR(20),
  p_password VARCHAR(14) DEFAULT NULL,
  p_last_name VARCHAR(36) DEFAULT NULL,
  p_first_name VARCHAR(15) DEFAULT NULL,
  p_middle_name VARCHAR(20) DEFAULT NULL,
  p_position VARCHAR(25) DEFAULT NULL,
  p_phone_number CHAR(12) DEFAULT NULL,
  p_email VARCHAR(254) DEFAULT NULL
)
AS $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM scientific_supervisor WHERE login = p_login) THEN
    RAISE EXCEPTION 'Supervisor with login % not found', p_login;
  END IF;

  UPDATE scientific_supervisor SET
    password = COALESCE(p_password, password),
    last_name = COALESCE(p_last_name, last_name),
    first_name = COALESCE(p_first_name, first_name),
    middle_name = COALESCE(p_middle_name, middle_name),
    position = COALESCE(p_position, position),
    phone_number = COALESCE(p_phone_number, phone_number),
    email = COALESCE(p_email, email)
  WHERE login = p_login;
  RAISE NOTICE 'Update success!';
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE PROCEDURE delete_supervisor(p_login VARCHAR(20))
AS $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM scientific_supervisor WHERE login = p_login) THEN
    RAISE EXCEPTION 'Supervisor with login % not found', p_login;
  END IF;

  DELETE FROM scientific_supervisor WHERE login = p_login;
  RAISE NOTICE 'Delete success!';
END;
$$ LANGUAGE plpgsql;


-- 2) Дисциплина — вставка / изменение / удаление
-- DROP PROCEDURE insert_discipline(text,text,numeric);

CREATE OR REPLACE PROCEDURE insert_discipline(
  p_name TEXT,
  p_department TEXT,
  p_semester NUMERIC(2,0)
)
AS $$
DECLARE
p_dept_id INT;
BEGIN
  IF NOT EXISTS (SELECT 1 FROM department WHERE LOWER(name) = LOWER(p_department)) THEN
    RAISE EXCEPTION 'Department with name % not found', p_department;
  ELSE p_dept_id = (SELECT department_id FROM department WHERE LOWER(name) = LOWER(p_department));
  END IF;

  IF EXISTS (SELECT 1 FROM discipline di JOIN department d ON di.department_id = d.department_id WHERE p_dept_id = d.department_id AND LOWER(p_name) = LOWER(di.name)) THEN
    RAISE EXCEPTION 'Discipline with name % already exists', p_name;
  ELSE 
    INSERT INTO discipline (name, department_id, semester)
    VALUES (p_name, p_dept_id, p_semester); 
  END IF;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE PROCEDURE update_discipline(
  p_old_name text,
  p_old_department TEXT,
  p_new_name text DEFAULT NULL,
  p_new_department TEXT DEFAULT NULL,
  p_semester numeric DEFAULT NULL
)
AS $$
DECLARE
p_old_dept_id INT;
p_new_dept_id INT;
BEGIN
  IF NOT EXISTS (SELECT 1 FROM department WHERE LOWER(name) = LOWER(p_old_department)) THEN
    RAISE EXCEPTION 'Department with name % not found', p_old_department;
  ELSE p_old_dept_id = (SELECT department_id FROM department WHERE LOWER(name) = LOWER(p_old_department));
  END IF;
 
  IF NOT EXISTS (SELECT 1 FROM department WHERE LOWER(name) = LOWER(p_new_department)) AND p_new_department IS NOT NULL THEN
    RAISE EXCEPTION 'Department with name % not found', p_new_department;
  ELSE p_new_dept_id = (SELECT department_id FROM department WHERE LOWER(name) = LOWER(p_new_department));
  END IF;

  IF NOT EXISTS (SELECT 1 FROM discipline di JOIN department d ON di.department_id = d.department_id WHERE p_old_dept_id = d.department_id AND LOWER(p_old_name) = LOWER(di.name)) THEN
    RAISE EXCEPTION 'Discipline with name % not exists', p_old_name;
  ELSE 
  UPDATE discipline SET
    name = COALESCE(p_new_name, name),
    department_id = COALESCE(p_new_dept_id, department_id),
    semester = COALESCE(p_semester, semester)
  WHERE LOWER(name) = LOWER(p_old_name) AND department_id = p_old_dept_id;
  RAISE NOTICE 'Update success!';
  END IF;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE PROCEDURE delete_discipline(
  p_name TEXT,
  p_department TEXT
)
AS $$
DECLARE 
p_dept_id INT;
BEGIN
  IF NOT EXISTS (SELECT 1 FROM department WHERE LOWER(name) = LOWER(p_department)) THEN
    RAISE EXCEPTION 'Department with name % not found', p_department;
  ELSE p_dept_id = (SELECT department_id FROM department WHERE LOWER(name) = LOWER(p_department));
  END IF;

  IF NOT EXISTS (SELECT 1 FROM discipline di JOIN department d ON di.department_id = d.department_id WHERE p_dept_id = d.department_id AND LOWER(p_name) = LOWER(di.name)) THEN
    RAISE EXCEPTION 'Discipline with name % not exists', p_name;
  END IF;

  DELETE FROM discipline WHERE LOWER(name) = LOWER(p_name) AND department_id = p_dept_id;
  RAISE NOTICE 'Delete success!';
END;
$$ LANGUAGE plpgsql;


-- 3) Группа — вставка с проверкой уникальности названия, изменение, удаление

CREATE OR REPLACE PROCEDURE insert_group(
  p_name varchar(15),
  p_year_of_admission date,
  p_department TEXT,
  p_education_type varchar(11) DEFAULT NULL,
  p_study_duration numeric(1,0) DEFAULT NULL
)
AS $$
DECLARE 
p_dept_id INT;
BEGIN
  -- проверяем уникальность имени группы
  IF EXISTS (SELECT 1 FROM "group" WHERE LOWER(name) = LOWER(p_name)) THEN
    RAISE EXCEPTION 'Group with name "%" already exists', p_name;
  END IF;

  -- проверяем существование кафедры
  IF NOT EXISTS (SELECT 1 FROM department WHERE LOWER(name) = LOWER(p_department)) THEN
    RAISE EXCEPTION 'Department with name % not found', p_department;
  ELSE p_dept_id = (SELECT department_id FROM department WHERE LOWER(name) = LOWER(p_department));
  END IF;

  INSERT INTO "group" (name, year_of_admission, department_id, education_type, study_duration)
  VALUES (p_name, p_year_of_admission, p_dept_id, p_education_type, p_study_duration);
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE PROCEDURE update_group(
  p_name varchar(15),
  p_year_of_admission date DEFAULT NULL,
  p_department TEXT DEFAULT NULL,
  p_education_type varchar(11) DEFAULT NULL,
  p_study_duration numeric(1,0) DEFAULT NULL
)
AS $$
DECLARE 
p_dept_id INT;
BEGIN
  IF NOT EXISTS (SELECT 1 FROM "group" WHERE LOWER(name) = LOWER(p_name)) THEN
    RAISE EXCEPTION 'Group with name % not found', p_name;
  END IF;

  -- проверяем существование кафедры
  IF p_department IS NOT NULL AND NOT EXISTS (SELECT 1 FROM department WHERE LOWER(name) = LOWER(p_department)) THEN
    RAISE EXCEPTION 'Department with name % not found', p_department;
  ELSE p_dept_id = (SELECT department_id FROM department WHERE LOWER(name) = LOWER(p_department));
  END IF;

  UPDATE "group" SET
    year_of_admission = COALESCE(p_year_of_admission, year_of_admission),
    department_id = COALESCE(p_dept_id, department_id),
    education_type = COALESCE(p_education_type, education_type),
    study_duration = COALESCE(p_study_duration, study_duration)
  WHERE LOWER(name) = LOWER(p_name);
  RAISE NOTICE 'Update success!';
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE PROCEDURE delete_group(p_name VARCHAR(15))
AS $$
BEGIN
  DELETE FROM "group" WHERE LOWER(name) = LOWER(p_name);
  IF NOT FOUND THEN
    RAISE EXCEPTION 'Group with name % not found', p_name;
  END IF;
  RAISE NOTICE 'Delete success!';
END;
$$ LANGUAGE plpgsql;


-- 4) Кафедра — вставка с проверкой уникальности названия

CREATE OR REPLACE PROCEDURE insert_department(
  p_name text,
  p_faculty text,
  p_head varchar(150) DEFAULT NULL,
  p_phone char(12) DEFAULT NULL
)
AS $$
DECLARE 
p_faculty_id INT;
BEGIN
  IF EXISTS (SELECT 1 FROM department WHERE LOWER(name) = LOWER(p_name)) THEN
    RAISE EXCEPTION 'Department with name "%" already exists', p_name;
  END IF;

  IF p_faculty IS NOT NULL AND NOT EXISTS (SELECT 1 FROM faculty WHERE LOWER(name) = LOWER(p_faculty)) THEN
    RAISE EXCEPTION 'Faculty with id % not found', p_faculty_id;
  ELSE p_faculty_id = (SELECT faculty_id FROM faculty WHERE LOWER(name) = LOWER(p_faculty));
  END IF;

  INSERT INTO department (name, faculty_id, head, phone)
  VALUES (p_name, p_faculty_id, p_head, p_phone);
END;
$$ LANGUAGE plpgsql;

-- ПРОЦЕДУРА ИЗМЕНЕНИЯ КУРСОВОЙ РАБОТЫ (ПРЕПОДАВАТЕЛЬ)
CREATE OR REPLACE PROCEDURE update_coursework_teacher(
    p_coursework_id INTEGER,
    p_topic TEXT DEFAULT NULL,
    p_submission_date DATE DEFAULT NULL,
    p_defense_date DATE DEFAULT NULL,
    p_topic_status VARCHAR(20) DEFAULT NULL,
    p_work_link VARCHAR(255) DEFAULT NULL,
    p_antiplagiat_level NUMERIC(5,2) DEFAULT NULL,
    p_theory_grade NUMERIC(2,0) DEFAULT NULL,
    p_practice_grade NUMERIC(2,0) DEFAULT NULL,
    p_design_grade NUMERIC(2,0) DEFAULT NULL,
    p_defense_grade NUMERIC(2,0) DEFAULT NULL,
    p_commission TEXT[] DEFAULT NULL,
    p_work_status VARCHAR(15) DEFAULT NULL,
    p_comment TEXT DEFAULT NULL
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_current_coursework coursework%ROWTYPE;
BEGIN
    -- Получаем текущие данные о курсовой
    SELECT * INTO v_current_coursework 
    FROM coursework 
    WHERE coursework_id = p_coursework_id;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Курсовая работа с ID % не найдена', p_coursework_id;
    END IF;
    
    -- Проверка: нельзя ставить оценки, если работа не прикреплена в срок
    IF (p_theory_grade IS NOT NULL OR p_practice_grade IS NOT NULL OR 
        p_design_grade IS NOT NULL OR p_defense_grade IS NOT NULL) THEN
        IF COALESCE(p_submission_date, v_current_coursework.submission_date) IS NULL OR 
           COALESCE(p_submission_date, v_current_coursework.submission_date) > COALESCE(p_defense_date, v_current_coursework.defense_date) THEN
            RAISE EXCEPTION 'Нельзя выставлять оценки: работа не сдана в срок';
        END IF;
    END IF;
    
    -- Проверка: нельзя ставить срок сдачи после срока защиты
    IF p_submission_date IS NOT NULL AND p_defense_date IS NOT NULL AND p_submission_date > p_defense_date THEN
        RAISE EXCEPTION 'Срок сдачи не может быть после срока защиты';
    END IF;
    
    -- Проверка: нельзя ставить срок защиты в запрещенные периоды
    IF p_defense_date IS NOT NULL THEN
        IF (EXTRACT(MONTH FROM p_defense_date) = 12 AND EXTRACT(DAY FROM p_defense_date) > 10) OR
           (EXTRACT(MONTH FROM p_defense_date) = 1 AND EXTRACT(DAY FROM p_defense_date) < 14) OR
           (EXTRACT(MONTH FROM p_defense_date) = 6 AND EXTRACT(DAY FROM p_defense_date) > 10) OR
           (EXTRACT(MONTH FROM p_defense_date) IN (7,8) OR 
            (EXTRACT(MONTH FROM p_defense_date) = 9 AND EXTRACT(DAY FROM p_defense_date) < 1)) THEN
            RAISE EXCEPTION 'Запрещенный период для защиты: 10.12-14.01 и 10.06-01.09';
        END IF;
    END IF;
    
    -- Обновление данных
    UPDATE coursework SET
        topic = COALESCE(p_topic, topic),
        submission_date = COALESCE(p_submission_date, submission_date),
        defense_date = COALESCE(p_defense_date, defense_date),
        topic_status = COALESCE(p_topic_status, topic_status),
        work_link = COALESCE(p_work_link, work_link),
        antiplagiat_level = COALESCE(p_antiplagiat_level, antiplagiat_level),
        theory_grade = COALESCE(p_theory_grade, theory_grade),
        practice_grade = COALESCE(p_practice_grade, practice_grade),
        design_grade = COALESCE(p_design_grade, design_grade),
        defense_grade = COALESCE(p_defense_grade, defense_grade),
        commission = COALESCE(p_commission, commission),
        work_status = COALESCE(p_work_status, work_status),
        comment = COALESCE(p_comment, comment)
    WHERE coursework_id = p_coursework_id;
END;
$$;

-- ФУНКЦИЯ ПРОСМОТРА КУРСОВЫХ РАБОТ (ПРЕПОДАВАТЕЛЬ)
CREATE OR REPLACE FUNCTION get_courseworks_for_teacher(p_supervisor_id INTEGER)
RETURNS TABLE(
    coursework_id INTEGER,
    student_name TEXT,
    group_name VARCHAR(15),
    discipline_name TEXT,
    topic TEXT,
    submission_date DATE,
    defense_date DATE,
    topic_status VARCHAR(20),
    work_status VARCHAR(15),
    final_grade NUMERIC(3,2),
    comment TEXT
) 
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.coursework_id,
        s.last_name || ' ' || s.first_name || ' ' || COALESCE(s.middle_name, '') as student_name,
        g.name as group_name,
        d.name as discipline_name,
        c.topic,
        c.submission_date,
        c.defense_date,
        c.topic_status,
        c.work_status,
        COALESCE(c.final_grade, 0) as final_grade,
        c.comment
    FROM coursework c JOIN coursework_plan cp ON c.coursework_id = cp.coursework_id
    JOIN student s ON cp.student_id = s.student_id
    JOIN "group" g ON s.group_id = g.group_id
    JOIN discipline d ON cp.discipline_id = d.discipline_id
    WHERE cp.supervisor_id = p_supervisor_id
    ORDER BY c.defense_date DESC;
END;
$$;

-- ПРОЦЕДУРА ЗАПОЛНЕНИЯ "КАФЕДРА ПРЕПОДАВАТЕЛЯ" (по названиям)
CREATE OR REPLACE PROCEDURE add_supervisor_department(
    p_supervisor_login VARCHAR(20),
    p_department_name VARCHAR(100)
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_supervisor_id INTEGER;
    v_department_id INTEGER;
BEGIN
    -- Получаем ID по логину и названию
    SELECT supervisor_id INTO v_supervisor_id 
    FROM scientific_supervisor 
    WHERE login = p_supervisor_login;
    
    SELECT department_id INTO v_department_id 
    FROM department 
    WHERE name = p_department_name;
    
    IF v_supervisor_id IS NULL THEN
        RAISE EXCEPTION 'Преподаватель с логином % не найден', p_supervisor_login;
    END IF;
    
    IF v_department_id IS NULL THEN
        RAISE EXCEPTION 'Кафедра % не найдена', p_department_name;
    END IF;
    
    -- Проверяем, нет ли уже такой записи
    IF EXISTS (SELECT 1 FROM supervisor_department 
               WHERE supervisor_id = v_supervisor_id AND department_id = v_department_id) THEN
        RAISE EXCEPTION 'Преподаватель уже привязан к этой кафедре';
    END IF;
    
    -- Добавляем запись
    INSERT INTO supervisor_department (supervisor_id, department_id)
    VALUES (v_supervisor_id, v_department_id);
END;
$$;

-- ПРОЦЕДУРА УДАЛЕНИЯ ИЗ "КАФЕДРА ПРЕПОДАВАТЕЛЯ"
CREATE OR REPLACE PROCEDURE remove_supervisor_department(p_link_id INTEGER)
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM supervisor_department WHERE link_id = p_link_id;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Запись с ID % не найдена', p_link_id;
    END IF;
END;
$$;

-- ТРИГГЕРНАЯ ФУНКЦИЯ ВОЗВРАЩЕНИЯ СТАТУСА РАБОТЫ
CREATE OR REPLACE FUNCTION update_work_status()
RETURNS TRIGGER AS $$
BEGIN
    -- Логика определения статуса работы
    IF NEW.submission_date IS NOT NULL AND NEW.defense_date IS NOT NULL THEN
        IF NEW.submission_date <= NEW.defense_date THEN
            IF NEW.theory_grade IS NOT NULL AND NEW.practice_grade IS NOT NULL AND 
               NEW.design_grade IS NOT NULL AND NEW.defense_grade IS NOT NULL THEN
                NEW.work_status := 'Защищена';
            ELSE
                NEW.work_status := 'На проверке';
            END IF;
        ELSE
            NEW.work_status := 'Долг';
        END IF;
    ELSIF NEW.submission_date IS NOT NULL THEN
        NEW.work_status := 'Принята';
    ELSIF NEW.topic_status = 'Принята' THEN
        NEW.work_status := 'Черновик';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ТРИГГЕРНАЯ ФУНКЦИЯ ВОЗВРАЩЕНИЯ СТАТУСА ТЕМЫ
CREATE OR REPLACE FUNCTION update_topic_status()
RETURNS TRIGGER AS $$
BEGIN
    -- Логика определения статуса темы
    IF NEW.topic IS NOT NULL AND NEW.topic != '' THEN
        IF NEW.topic_status = 'На проверке' AND NEW.work_link IS NOT NULL THEN
            NEW.topic_status := 'Принята';
        ELSIF NEW.topic_status IS NULL OR NEW.topic_status = '-' THEN
            NEW.topic_status := 'На проверке';
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ФУНКЦИЯ ГЕНЕРАЦИИ СПИСКА ЛОГИНОВ И ПАРОЛЕЙ СТУДЕНТОВ
CREATE OR REPLACE FUNCTION generate_student_credentials()
RETURNS TABLE(
    student_id INTEGER,
    last_name VARCHAR(36),
    first_name VARCHAR(15),
    generated_login VARCHAR(20),
    generated_password VARCHAR(14)
) 
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.student_id,
        s.last_name,
        s.first_name,
        s.login,
        s.password
    FROM student s;
END;
$$;

-- ТРИГГЕРНАЯ ФУНКЦИЯ ВЫСЧИТЫВАНИЯ ИТОГОВОЙ ОЦЕНКИ
CREATE OR REPLACE FUNCTION calculate_final_grade()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.theory_grade IS NOT NULL OR NEW.practice_grade IS NOT NULL OR 
       NEW.design_grade IS NOT NULL OR NEW.defense_grade IS NOT NULL THEN
        
        NEW.final_grade := (
            COALESCE(NEW.theory_grade, 0) + 
            COALESCE(NEW.practice_grade, 0) + 
            COALESCE(NEW.design_grade, 0) + 
            COALESCE(NEW.defense_grade, 0)
        ) / NULLIF(
            (CASE WHEN NEW.theory_grade IS NOT NULL THEN 1 ELSE 0 END) +
            (CASE WHEN NEW.practice_grade IS NOT NULL THEN 1 ELSE 0 END) +
            (CASE WHEN NEW.design_grade IS NOT NULL THEN 1 ELSE 0 END) +
            (CASE WHEN NEW.defense_grade IS NOT NULL THEN 1 ELSE 0 END),
        0);
    ELSE
        NEW.final_grade := NULL;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ТРИГГЕРНАЯ ФУНКЦИЯ ПРОВЕРКИ НАГРУЗКИ ПРЕПОДАВАТЕЛЯ
CREATE OR REPLACE FUNCTION check_supervisor_workload()
RETURNS TRIGGER AS $$
DECLARE
    v_student_count INTEGER;
BEGIN
    SELECT COUNT(DISTINCT student_id) INTO v_student_count
    FROM coursework_plan
    WHERE supervisor_id = NEW.supervisor_id;
    
    IF v_student_count > 10 THEN
        RAISE NOTICE 'Внимание! У преподавателя ID % уже % студентов. Максимальная нагрузка - 10 студентов.', 
                     NEW.supervisor_id, v_student_count;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ФУНКЦИЯ СОЗДАНИЯ ТАБЛИЦЫ (ФИО, ТЕМА, ДАТА СДАЧИ)
CREATE OR REPLACE FUNCTION create_coursework_report()
RETURNS TABLE(
    student_name TEXT,
    topic TEXT,
    submission_date DATE,
    defense_date DATE,
    work_status VARCHAR(15)
) 
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.last_name || ' ' || s.first_name || ' ' || COALESCE(s.middle_name, '') as student_name,
        c.topic,
        c.submission_date,
        c.defense_date,
        c.work_status
    FROM coursework c JOIN coursework_plan cp ON c.coursework_id = cp.coursework_id
    JOIN student s ON cp.student_id = s.student_id
    ORDER BY s.last_name, s.first_name;
END;
$$;

-- 1. Удаление кафедры artem
CREATE OR REPLACE PROCEDURE delete_department(pname TEXT)
LANGUAGE plpgsql
AS $$
DECLARE
    v_count INT;
BEGIN
    DELETE FROM department WHERE LOWER(name) = LOWER(pname);
    GET DIAGNOSTICS v_count = ROW_COUNT;
    IF v_count = 0 THEN
        RAISE EXCEPTION 'Department with name "%" not found', pname;
    END IF;
    RAISE NOTICE 'Department "%" deleted successfully', pname;
END;
$$;

-- 2. Изменение кафедры
CREATE OR REPLACE PROCEDURE update_department(
    pold_name TEXT,
    pnew_name TEXT DEFAULT NULL,
    pfaculty TEXT DEFAULT NULL,
    phead VARCHAR(150) DEFAULT NULL,
    pphone CHAR(12) DEFAULT NULL
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_dept_id INT;
    v_fac_id INT;
BEGIN
    SELECT department_id INTO v_dept_id
    FROM department
    WHERE LOWER(name) = LOWER(pold_name);

    IF v_dept_id IS NULL THEN
        RAISE EXCEPTION 'Department with name "%" not found', pold_name;
    END IF;

    IF pfaculty IS NOT NULL THEN
        SELECT faculty_id INTO v_fac_id
        FROM faculty
        WHERE LOWER(name) = LOWER(pfaculty);
        IF v_fac_id IS NULL THEN
            RAISE EXCEPTION 'Faculty "%" not found', pfaculty;
        END IF;
    END IF;

    UPDATE department
    SET
        name       = COALESCE(pnew_name, name),
        faculty_id = COALESCE(v_fac_id, faculty_id),
        head       = COALESCE(phead, head),
        phone      = COALESCE(pphone, phone)
    WHERE department_id = v_dept_id;

    RAISE NOTICE 'Department "%" updated successfully', pold_name;
END;
$$;

-- 3. Заполнение факультета с проверкой уникальности
CREATE OR REPLACE PROCEDURE insert_faculty(
    pname VARCHAR(100),
    pdean VARCHAR(150) DEFAULT NULL,
    pphone CHAR(12) DEFAULT NULL
)
LANGUAGE plpgsql
AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM faculty WHERE LOWER(name) = LOWER(pname)) THEN
        RAISE EXCEPTION 'Faculty with name "%" already exists', pname;
    END IF;

    INSERT INTO faculty (name, dean, phone_number)
    VALUES (pname, pdean, pphone);

    RAISE NOTICE 'Faculty "%" inserted successfully', pname;
END;
$$;

-- 4. Удаление факультета
CREATE OR REPLACE PROCEDURE delete_faculty(pname VARCHAR(100))
LANGUAGE plpgsql
AS $$
DECLARE
    v_count INT;
BEGIN
    DELETE FROM faculty WHERE LOWER(name) = LOWER(pname);
    GET DIAGNOSTICS v_count = ROW_COUNT;
    IF v_count = 0 THEN
        RAISE EXCEPTION 'Faculty with name "%" not found', pname;
    END IF;
    RAISE NOTICE 'Faculty "%" deleted successfully', pname;
END;
$$;

-- 5. Изменение факультета
CREATE OR REPLACE PROCEDURE update_faculty(
    pold_name VARCHAR(100),
    pnew_name VARCHAR(100) DEFAULT NULL,
    pdean VARCHAR(150) DEFAULT NULL,
    pphone CHAR(12) DEFAULT NULL
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_fac_id INT;
BEGIN
    SELECT faculty_id INTO v_fac_id
    FROM faculty
    WHERE LOWER(name) = LOWER(pold_name);

    IF v_fac_id IS NULL THEN
        RAISE EXCEPTION 'Faculty "%" not found', pold_name;
    END IF;

    UPDATE faculty
    SET
        name        = COALESCE(pnew_name, name),
        dean        = COALESCE(pdean, dean),
        phone_number = COALESCE(pphone, phone_number)
    WHERE faculty_id = v_fac_id;

    RAISE NOTICE 'Faculty "%" updated successfully', pold_name;
END;
$$;

-- 6. Добавление/синхронизация студентов группы
CREATE OR REPLACE PROCEDURE add_students_to_group(
    pgroup_name VARCHAR(15),
    pstudents_list TEXT[]
)
LANGUAGE plpgsql
AS $$
DECLARE
    student_record TEXT;
    student_json   JSON;
    v_group_id INT;
    v_student_id INT;
    v_login VARCHAR(20);
    v_password VARCHAR(14);
    v_record_book CHAR(6);
    v_last_name VARCHAR(36);
    v_first_name VARCHAR(15);
    v_middle_name VARCHAR(20);
BEGIN
    SELECT group_id INTO v_group_id
    FROM "group"
    WHERE LOWER(name) = LOWER(pgroup_name);

    IF v_group_id IS NULL THEN
        RAISE EXCEPTION 'Group "%" not found', pgroup_name;
    END IF;

    -- Проходим по массиву TEXT[], каждый элемент – строка JSON
    FOREACH student_record IN ARRAY pstudents_list
    LOOP
        student_json := student_record::JSON;

        v_record_book := student_json->>'recordbook';
        v_last_name   := student_json->>'lastname';
        v_first_name  := student_json->>'firstname';
        v_middle_name := student_json->>'middlename';

        SELECT student_id INTO v_student_id
        FROM student
        WHERE record_book = v_record_book
          AND LOWER(last_name) = LOWER(v_last_name);

        IF v_student_id IS NULL THEN
            v_login := transliterate(SUBSTRING(v_last_name FROM 1 FOR 5) || SUBSTRING(v_first_name FROM 1 FOR 1)) || LPAD(v_group_id::TEXT, 3, '0');
v_password := SUBSTRING(MD5(RANDOM()::TEXT) FROM 1 FOR 14);

            INSERT INTO student (
                login, password,
                last_name, first_name, middle_name,
                group_id, phone_number, email,
                record_book, leader
            )
            VALUES (
                v_login, v_password,
                v_last_name, v_first_name, v_middle_name,
                v_group_id, NULL, NULL,
                v_record_book, FALSE
            )
            RETURNING student_id INTO v_student_id;

            RAISE NOTICE 'New student created: % % (login: %, password: %)',
                v_last_name, v_first_name, v_login, v_password;
        ELSE
            UPDATE student
            SET last_name  = v_last_name,
                first_name = v_first_name,
                middle_name = v_middle_name,
                group_id   = v_group_id
            WHERE student_id = v_student_id;
        END IF;
    END LOOP;

    -- Удаляем тех, кого нет в новом списке
    DELETE FROM student s
    WHERE s.group_id = v_group_id
      AND NOT EXISTS (
        SELECT 1
        FROM unnest(pstudents_list) AS raw
        WHERE (raw::JSON)->>'recordbook' = s.record_book
      );

    RAISE NOTICE 'Students processing for group "%" completed', pgroup_name;
END;
$$;

-- 7. Изменение полей студента
CREATE OR REPLACE PROCEDURE update_student(
    pstudent_login VARCHAR(20),
    pphone CHAR(12) DEFAULT NULL,
    pemail VARCHAR(254) DEFAULT NULL,
    pleader BOOLEAN DEFAULT NULL
)
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM student WHERE login = pstudent_login) THEN
        RAISE EXCEPTION 'Student with login "%" not found', pstudent_login;
    END IF;

    UPDATE student
    SET
        phone_number = COALESCE(pphone, phone_number),
        email        = COALESCE(pemail, email),
        leader       = COALESCE(pleader, leader)
    WHERE login = pstudent_login;

    RAISE NOTICE 'Student "%" updated successfully', pstudent_login;
END;
$$;

-- 8. Создание записи курсовой работы преподавателем
CREATE OR REPLACE PROCEDURE insert_coursework(
    pstudent_login VARCHAR(20),
    psupervisor_login VARCHAR(20),
    pdiscipline_name TEXT,
    ptopic TEXT,
	pyear DATE
)
LANGUAGE plpgsql
AS $$
DECLARE
	v_coursework_id INT;
    v_student_id INT;
    v_supervisor_id INT;
    v_discipline_id INT;
BEGIN
    SELECT student_id INTO v_student_id
    FROM student
    WHERE login = pstudent_login;

    SELECT supervisor_id INTO v_supervisor_id
    FROM scientific_supervisor
    WHERE login = psupervisor_login;

    SELECT discipline_id INTO v_discipline_id
    FROM discipline
    WHERE LOWER(name) = LOWER(pdiscipline_name);

    IF v_student_id IS NULL THEN
        RAISE EXCEPTION 'Student "%" not found', pstudent_login;
    END IF;
    IF v_supervisor_id IS NULL THEN
        RAISE EXCEPTION 'Supervisor "%" not found', psupervisor_login;
    END IF;
    IF v_discipline_id IS NULL THEN
        RAISE EXCEPTION 'Discipline "%" not found', pdiscipline_name;
    END IF;

	INSERT INTO coursework(submission_date, defense_date,
        topic, topic_status, work_status)
	VALUES (NULL, NULL,
        ptopic, 'На проверке', 'Черновик');

	SELECT coursework_id INTO v_coursework_id
    FROM coursework
    ORDER BY coursework_id DESC
	LIMIT 1;
	
    INSERT INTO coursework_plan (
         student_id, supervisor_id, discipline_id, coursework_id, coursework_year
    )
    VALUES (
         v_student_id, v_supervisor_id, v_discipline_id, v_coursework_id, pyear
    );

    RAISE NOTICE 'Coursework created successfully';
END;
$$;

-- 9. Удаление курсовой работы
CREATE OR REPLACE PROCEDURE delete_coursework(pcoursework_id INT)
LANGUAGE plpgsql
AS $$
BEGIN
	DELETE FROM coursework_plan WHERE coursework_id = pcoursework_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Coursework with ID % not found', pcoursework_id;
    END IF;
	
    DELETE FROM coursework WHERE coursework_id = pcoursework_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Coursework with ID % not found', pcoursework_id;
    END IF;
    RAISE NOTICE 'Coursework % deleted successfully', pcoursework_id;
END;
$$;

-- 10. Изменение темы/ссылки курсовой студентом
CREATE OR REPLACE PROCEDURE update_coursework_student(
    pcoursework_id INT,
    ptopic TEXT DEFAULT NULL,
    pwork_link VARCHAR(255) DEFAULT NULL
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_current coursework%ROWTYPE;
BEGIN
    SELECT * INTO v_current
    FROM coursework
    WHERE coursework_id = pcoursework_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Coursework % not found', pcoursework_id;
    END IF;

    IF v_current.topic_status NOT IN ('Принята', '-', 'На проверке') OR
       v_current.work_status NOT IN ('Принята', 'Защищена', '-', 'На проверке', 'Черновик') THEN
        RAISE EXCEPTION
            'Cannot update: status is "%" / "%"',
            v_current.topic_status, v_current.work_status;
    END IF;

    UPDATE coursework
    SET
        topic     = COALESCE(ptopic, topic),
        work_link = COALESCE(pwork_link, work_link)
    WHERE coursework_id = pcoursework_id;
END;
$$;




-- ============================================
-- СОЗДАНИЕ ВСЕХ ТРИГГЕРОВ
-- ============================================

-- Триггер для итоговой оценки
CREATE OR REPLACE TRIGGER tr_calculate_final_grade
    BEFORE INSERT OR UPDATE OF theory_grade, practice_grade, design_grade, defense_grade
    ON coursework
    FOR EACH ROW
    EXECUTE FUNCTION calculate_final_grade();

-- Триггер для проверки нагрузки
CREATE OR REPLACE TRIGGER tr_check_supervisor_workload
    BEFORE INSERT ON coursework_plan
    FOR EACH ROW
    EXECUTE FUNCTION check_supervisor_workload();

-- Триггер для статуса работы
CREATE OR REPLACE TRIGGER tr_update_work_status
    BEFORE INSERT OR UPDATE OF submission_date, defense_date, theory_grade, practice_grade, design_grade, defense_grade, topic_status
    ON coursework
    FOR EACH ROW
    EXECUTE FUNCTION update_work_status();

-- Триггер для статуса темы
CREATE OR REPLACE TRIGGER tr_update_topic_status
    BEFORE INSERT OR UPDATE OF topic, work_link
    ON coursework
    FOR EACH ROW
    EXECUTE FUNCTION update_topic_status();

-- Вставка факультетов
INSERT INTO faculty (name, dean, phone_number) VALUES
('Факультет ИТ', 'Иванов И.И.', '+71111111111'),
('Факультет экономики', 'Петров П.П.', '+72222222222');

-- Вставка кафедр
INSERT INTO department (name, faculty_id, head, phone) VALUES
('Кафедра программирования', 1, 'Сидоров С.С.', '+73333333333'),
('Кафедра менеджмента', 2, 'Кузнецов К.К.', '+74444444444');

-- Вставка групп
INSERT INTO "group" (name, year_of_admission, department_id, education_type, study_duration) VALUES
('БПО09-23-01', '2023-09-01', 1, 'очная', 4),
('ЭКО-22', '2022-09-01', 2, 'заочная', 4);

-- Вставка студентов
INSERT INTO student (login, password, last_name, first_name, middle_name, group_id, phone_number, email, record_book, leader) VALUES
('ivanov21', 'pass1234', 'Иванов', 'Иван', 'Иванович', 1, '+79990001111', 'ivanov21@mail.com', '123456', false),
('petrov22', 'pass5678', 'Петров', 'Петр', NULL, 2, '+79990002222', 'petrov22@mail.com', '654321', true);

-- Вставка научных руководителей
INSERT INTO scientific_supervisor (login, password, last_name, first_name, middle_name, position, phone_number, email) VALUES
('prof_sidorov', 'profpass', 'Сидоров', 'Сидор', NULL, 'доцент', '+79990003333', 'sidorov@mail.com'),
('prof_ivanov', 'profpass2', 'Иванов', 'Иван', NULL, 'профессор', '+79990004444', 'ivanov2@mail.com');

-- Вставка дисциплин
INSERT INTO discipline (name, department_id, semester) VALUES
('Программирование', 1, 2),
('Менеджмент', 2, 3);

-- Вставка курсовой работы
INSERT INTO coursework (submission_date, defense_date, topic, topic_status, work_status) VALUES
('2023-12-01', '2024-01-15', 'Разработка веб-приложения', 'На проверке', '-'),
('2024-01-10', '2024-02-20', 'Исследование рынка', 'Принята', 'Черновик');

-- Вставка связей преподаватель - кафедра
INSERT INTO supervisor_department (supervisor_id, department_id) VALUES
(1,1),
(2,2);