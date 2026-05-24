-- 4. Tabla de Cursos (Materias)
CREATE TABLE courses (
                         id_course INT AUTO_INCREMENT PRIMARY KEY,
                         id_teacher INT,
                         name VARCHAR(100) NOT NULL,
                         description TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         FOREIGN KEY (id_teacher) REFERENCES users(id_user) ON DELETE SET NULL
);

-- 5. Tabla Intermedia: Course Periods (Grados/Secciones por año)
CREATE TABLE course_periods (
                                id_course_period INT AUTO_INCREMENT PRIMARY KEY,
                                id_course INT NOT NULL,
                                id_period INT NOT NULL,
                                section VARCHAR(10),
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                FOREIGN KEY (id_course) REFERENCES courses(id_course) ON DELETE CASCADE,
                                FOREIGN KEY (id_period) REFERENCES periods(id_period) ON DELETE CASCADE
);