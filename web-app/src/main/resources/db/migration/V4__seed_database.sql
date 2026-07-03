-- 1. Insertar Usuarios (Profesores)
INSERT INTO users (first_name, last_name, email, password) VALUES
    ('Ana', 'Gómez', 'docente@learnix.com', '123456'),
    ('Luis', 'Martínez', 'luis@learnix.com', '123456'),
    ('María', 'López', 'maria@learnix.com', '123456'),
    ('Carlos', 'Rodríguez', 'carlos@learnix.com', '123456'),
    ('Elena', 'Sánchez', 'elena@learnix.com', '123456');

-- 2. Insertar Periodos (Año Escolar 2026)
INSERT INTO periods (name, start_date, end_date, is_closed) VALUES
    ('Año Escolar 2026', '2026-03-01', '2026-12-15', FALSE);

-- 3. Insertar Cursos (Materias)
INSERT INTO courses (id_teacher, name, description) VALUES
    ((SELECT id_user FROM users WHERE first_name = 'Ana'), 'Matemáticas', 'Curso de matemáticas para primaria.'),
    ((SELECT id_user FROM users WHERE first_name = 'Ana'), 'Lenguaje', 'Curso de lenguaje y comunicación.'),
    ((SELECT id_user FROM users WHERE first_name = 'Ana'), 'Ciencias Naturales', 'Curso de ciencias para primaria.'),
    ((SELECT id_user FROM users WHERE first_name = 'Ana'), 'Historia', 'Curso de historia del Perú y mundo.'),
    ((SELECT id_user FROM users WHERE first_name = 'Ana'), 'Arte', 'Curso de arte y manualidades.');

-- 4. Insertar Course Periods (Grados/Secciones)
INSERT INTO course_periods (id_course, id_period, section) VALUES
    ((SELECT id_course FROM courses WHERE name = 'Matemáticas'), (SELECT id_period FROM periods WHERE name = 'Año Escolar 2026'), 'A'),
    ((SELECT id_course FROM courses WHERE name = 'Matemáticas'), (SELECT id_period FROM periods WHERE name = 'Año Escolar 2026'), 'B'),
    ((SELECT id_course FROM courses WHERE name = 'Lenguaje'), (SELECT id_period FROM periods WHERE name = 'Año Escolar 2026'), 'A'),
    ((SELECT id_course FROM courses WHERE name = 'Lenguaje'), (SELECT id_period FROM periods WHERE name = 'Año Escolar 2026'), 'B'),
    ((SELECT id_course FROM courses WHERE name = 'Ciencias Naturales'), (SELECT id_period FROM periods WHERE name = 'Año Escolar 2026'), 'A'),
    ((SELECT id_course FROM courses WHERE name = 'Historia'), (SELECT id_period FROM periods WHERE name = 'Año Escolar 2026'), 'B'),
    ((SELECT id_course FROM courses WHERE name = 'Arte'), (SELECT id_period FROM periods WHERE name = 'Año Escolar 2026'), 'Única');