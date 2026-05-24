-- 6. Tabla de Inscripciones (Relación Alumno - Grado)
CREATE TABLE enrollments (
                             id_enrollment INT AUTO_INCREMENT PRIMARY KEY,
                             id_student INT NOT NULL,
                             id_course_period INT NOT NULL,
                             status VARCHAR(20) DEFAULT 'active',
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (id_student) REFERENCES students(id_student) ON DELETE CASCADE,
                             FOREIGN KEY (id_course_period) REFERENCES course_periods(id_course_period) ON DELETE CASCADE,
                             UNIQUE (id_student, id_course_period)
);

-- 7. Tabla de Asistencias (Con PK compuesta numérica)
CREATE TABLE attendances (
                             id_student INT NOT NULL,
                             id_course_period INT NOT NULL,
                             id_teacher INT NOT NULL,
                             date DATE NOT NULL,
                             did_attend BOOLEAN DEFAULT TRUE,
                             observation VARCHAR(255),
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             FOREIGN KEY (id_student) REFERENCES students(id_student) ON DELETE CASCADE,
                             FOREIGN KEY (id_course_period) REFERENCES course_periods(id_course_period) ON DELETE CASCADE,
                             FOREIGN KEY (id_teacher) REFERENCES users(id_user),
                             PRIMARY KEY (id_student, id_course_period, date)
);

-- 8. Tabla de Actividades (Tareas/Exámenes)
CREATE TABLE activities (
                            id_activity INT AUTO_INCREMENT PRIMARY KEY,
                            id_course_period INT NOT NULL,
                            name VARCHAR(100) NOT NULL,
                            weight DECIMAL(5,2),
                            due_date DATE,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            FOREIGN KEY (id_course_period) REFERENCES course_periods(id_course_period) ON DELETE CASCADE
);

-- 9. Tabla de Calificaciones (Letras para Primaria)
CREATE TABLE grades (
                        id_activity INT NOT NULL,
                        id_student INT NOT NULL,
                        value VARCHAR(5) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (id_activity) REFERENCES activities(id_activity) ON DELETE CASCADE,
                        FOREIGN KEY (id_student) REFERENCES students(id_student) ON DELETE CASCADE,
                        PRIMARY KEY (id_activity, id_student)
);