-- 1. Insertar Usuarios (Profesores)
INSERT INTO users (first_name, last_name, email, password) VALUES
    ('Ana', 'Gómez', 'docente@learnix.com', '123456'),
    ('Luis', 'Martínez', 'luis@learnix.com', '123456'),
    ('María', 'López', 'maria@learnix.com', '123456'),
    ('Carlos', 'Rodríguez', 'carlos@learnix.com', '123456'),
    ('Elena', 'Sánchez', 'elena@learnix.com', '123456');

-- 2. Insertar Estudiantes
INSERT INTO students (first_name, last_name, dni, birth_date) VALUES
    ('Juan', 'Pérez', '70000001', '2018-05-15'),
    ('María', 'García', '70000002', '2018-08-20'),
    ('Pedro', 'Sánchez', '70000003', '2019-02-10'),
    ('Lucía', 'Martínez', '70000004', '2019-07-05'),
    ('Sofía', 'López', '70000005', '2018-11-30'),
    ('Diego', 'Ramírez', '70000006', '2019-01-12'),
    ('Valentina', 'Díaz', '70000007', '2018-09-18'),
    ('Jorge', 'Hernández', '70000008', '2019-04-25'),
    ('Camila', 'Fernández', '70000009', '2018-12-03'),
    ('Alejandro', 'Morales', '70000010', '2019-06-14');

-- 3. Insertar Periodos (Año Escolar 2026)
INSERT INTO periods (name, start_date, end_date, is_closed) VALUES
    ('Año Escolar 2026', '2026-03-01', '2026-12-15', FALSE);

-- 4. Insertar Cursos (Materias)
INSERT INTO courses (id_teacher, name, description) VALUES
    ((SELECT id_user FROM users WHERE first_name = 'Ana'), 'Matemáticas', 'Curso de matemáticas para primaria.'),
    ((SELECT id_user FROM users WHERE first_name = 'Ana'), 'Lenguaje', 'Curso de lenguaje y comunicación.'),
    ((SELECT id_user FROM users WHERE first_name = 'Ana'), 'Ciencias Naturales', 'Curso de ciencias para primaria.'),
    ((SELECT id_user FROM users WHERE first_name = 'Ana'), 'Historia', 'Curso de historia del Perú y mundo.'),
    ((SELECT id_user FROM users WHERE first_name = 'Ana'), 'Arte', 'Curso de arte y manualidades.');

-- 5. Insertar Course Periods (Grados/Secciones)
INSERT INTO course_periods (id_course, id_period, section) VALUES
    ((SELECT id_course FROM courses WHERE name = 'Matemáticas'), (SELECT id_period FROM periods WHERE name = 'Año Escolar 2026'), 'A'),
    ((SELECT id_course FROM courses WHERE name = 'Matemáticas'), (SELECT id_period FROM periods WHERE name = 'Año Escolar 2026'), 'B'),
    ((SELECT id_course FROM courses WHERE name = 'Lenguaje'), (SELECT id_period FROM periods WHERE name = 'Año Escolar 2026'), 'A'),
    ((SELECT id_course FROM courses WHERE name = 'Lenguaje'), (SELECT id_period FROM periods WHERE name = 'Año Escolar 2026'), 'B'),
    ((SELECT id_course FROM courses WHERE name = 'Ciencias Naturales'), (SELECT id_period FROM periods WHERE name = 'Año Escolar 2026'), 'A'),
    ((SELECT id_course FROM courses WHERE name = 'Historia'), (SELECT id_period FROM periods WHERE name = 'Año Escolar 2026'), 'B'),
    ((SELECT id_course FROM courses WHERE name = 'Arte'), (SELECT id_period FROM periods WHERE name = 'Año Escolar 2026'), 'Única');

-- 6. Insertar Inscripciones (Estudiantes en Cursos)
INSERT INTO enrollments (id_student, id_course_period, status) VALUES
    -- Matemáticas A
    ((SELECT id_student FROM students WHERE first_name = 'Juan'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Matemáticas') AND section = 'A'), 'active'),
    ((SELECT id_student FROM students WHERE first_name = 'María'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Matemáticas') AND section = 'A'), 'active'),
    ((SELECT id_student FROM students WHERE first_name = 'Pedro'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Matemáticas') AND section = 'A'), 'active'),
    -- Matemáticas B
    ((SELECT id_student FROM students WHERE first_name = 'Lucía'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Matemáticas') AND section = 'B'), 'active'),
    ((SELECT id_student FROM students WHERE first_name = 'Sofía'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Matemáticas') AND section = 'B'), 'active'),
    -- Lenguaje A
    ((SELECT id_student FROM students WHERE first_name = 'Juan'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Lenguaje') AND section = 'A'), 'active'),
    ((SELECT id_student FROM students WHERE first_name = 'Diego'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Lenguaje') AND section = 'A'), 'active'),
    -- Lenguaje B
    ((SELECT id_student FROM students WHERE first_name = 'Valentina'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Lenguaje') AND section = 'B'), 'active'),
    ((SELECT id_student FROM students WHERE first_name = 'Jorge'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Lenguaje') AND section = 'B'), 'active'),
    -- Ciencias Naturales A
    ((SELECT id_student FROM students WHERE first_name = 'Pedro'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Ciencias Naturales') AND section = 'A'), 'active'),
    ((SELECT id_student FROM students WHERE first_name = 'Camila'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Ciencias Naturales') AND section = 'A'), 'active'),
    -- Historia B
    ((SELECT id_student FROM students WHERE first_name = 'Sofía'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Historia') AND section = 'B'), 'active'),
    ((SELECT id_student FROM students WHERE first_name = 'Alejandro'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Historia') AND section = 'B'), 'active'),
    -- Arte Única
    ((SELECT id_student FROM students WHERE first_name = 'Lucía'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Arte') AND section = 'Única'), 'active'),
    ((SELECT id_student FROM students WHERE first_name = 'Diego'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Arte') AND section = 'Única'), 'active');

-- 7. Insertar Asistencias (Ejemplo para una semana)
INSERT INTO attendances (id_student, id_course_period, id_teacher, date, did_attend, observation) VALUES
    -- Matemáticas A - Ana Gómez
    ((SELECT id_student FROM students WHERE first_name = 'Juan'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Matemáticas') AND section = 'A'), (SELECT id_user FROM users WHERE first_name = 'Ana'), '2026-04-28', TRUE, NULL),
    ((SELECT id_student FROM students WHERE first_name = 'María'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Matemáticas') AND section = 'A'), (SELECT id_user FROM users WHERE first_name = 'Ana'), '2026-04-28', TRUE, NULL),
    ((SELECT id_student FROM students WHERE first_name = 'Pedro'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Matemáticas') AND section = 'A'), (SELECT id_user FROM users WHERE first_name = 'Ana'), '2026-04-28', FALSE, 'Enfermo'),
    -- Lenguaje A - Luis Martínez
    ((SELECT id_student FROM students WHERE first_name = 'Juan'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Lenguaje') AND section = 'A'), (SELECT id_user FROM users WHERE first_name = 'Luis'), '2026-04-28', TRUE, NULL),
    ((SELECT id_student FROM students WHERE first_name = 'Diego'), (SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Lenguaje') AND section = 'A'), (SELECT id_user FROM users WHERE first_name = 'Luis'), '2026-04-28', TRUE, NULL);

-- 8. Insertar Actividades (Tareas/Exámenes)
INSERT INTO activities (id_course_period, name, weight, due_date) VALUES
    -- Matemáticas A
    ((SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Matemáticas') AND section = 'A'), 'Examen de Suma y Resta', 30.00, '2026-05-10'),
    ((SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Matemáticas') AND section = 'A'), 'Tarea de Geometría', 10.00, '2026-05-05'),
    -- Lenguaje A
    ((SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Lenguaje') AND section = 'A'), 'Redacción de Cuento', 20.00, '2026-05-12'),
    -- Ciencias Naturales A
    ((SELECT id_course_period FROM course_periods WHERE id_course = (SELECT id_course FROM courses WHERE name = 'Ciencias Naturales') AND section = 'A'), 'Proyecto de Plantas', 25.00, '2026-05-15');

-- 9. Insertar Calificaciones (Notas en Letras)
INSERT INTO grades (id_activity, id_student, value) VALUES
    -- Examen de Suma y Resta (Matemáticas A)
    ((SELECT id_activity FROM activities WHERE name = 'Examen de Suma y Resta'), (SELECT id_student FROM students WHERE first_name = 'Juan'), 'A'),
    ((SELECT id_activity FROM activities WHERE name = 'Examen de Suma y Resta'), (SELECT id_student FROM students WHERE first_name = 'María'), 'AD'),
    ((SELECT id_activity FROM activities WHERE name = 'Examen de Suma y Resta'), (SELECT id_student FROM students WHERE first_name = 'Pedro'), 'B'),
    -- Tarea de Geometría (Matemáticas A)
    ((SELECT id_activity FROM activities WHERE name = 'Tarea de Geometría'), (SELECT id_student FROM students WHERE first_name = 'Juan'), 'AD'),
    ((SELECT id_activity FROM activities WHERE name = 'Tarea de Geometría'), (SELECT id_student FROM students WHERE first_name = 'María'), 'A'),
    -- Redacción de Cuento (Lenguaje A)
    ((SELECT id_activity FROM activities WHERE name = 'Redacción de Cuento'), (SELECT id_student FROM students WHERE first_name = 'Juan'), 'B'),
    ((SELECT id_activity FROM activities WHERE name = 'Redacción de Cuento'), (SELECT id_student FROM students WHERE first_name = 'Diego'), 'C');