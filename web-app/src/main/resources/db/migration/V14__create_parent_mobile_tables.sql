-- =========================================================================
-- MIGRACIÓN FLYWAY V14: TABLAS Y PROCEDIMIENTOS ALMACENADOS PARA APP MÓVIL
-- =========================================================================

-- 1. Modificar tablas existentes
ALTER TABLE activities ADD COLUMN term VARCHAR(10);
ALTER TABLE attendances ADD COLUMN status VARCHAR(20) DEFAULT 'attended';

UPDATE attendances SET status = CASE WHEN did_attend = TRUE THEN 'attended' ELSE 'absent' END;

-- 2. Crear nuevas tablas
CREATE TABLE IF NOT EXISTS parents (
    id_parent INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    phone VARCHAR(30),
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS parent_students (
    id_parent INT NOT NULL,
    id_student INT NOT NULL,
    relationship VARCHAR(40) NOT NULL DEFAULT 'Apoderado',
    is_primary BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_parent, id_student),
    FOREIGN KEY (id_parent) REFERENCES parents(id_parent) ON DELETE CASCADE,
    FOREIGN KEY (id_student) REFERENCES students(id_student) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS parent_alerts (
    id_alert INT AUTO_INCREMENT PRIMARY KEY,
    id_student INT NOT NULL,
    id_parent INT,
    type VARCHAR(40) NOT NULL,
    title VARCHAR(150) NOT NULL,
    detail TEXT,
    severity VARCHAR(20) NOT NULL DEFAULT 'info',
    status VARCHAR(20) NOT NULL DEFAULT 'new',
    source_table VARCHAR(80),
    source_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    archived_at TIMESTAMP NULL,
    event_date DATETIME NULL,
    FOREIGN KEY (id_student) REFERENCES students(id_student) ON DELETE CASCADE,
    FOREIGN KEY (id_parent) REFERENCES parents(id_parent) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS announcements (
    id_announcement INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    body TEXT NOT NULL,
    sender_name VARCHAR(150) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'normal',
    published_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS announcement_recipients (
    id_announcement INT NOT NULL,
    id_parent INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'new',
    read_at TIMESTAMP NULL,
    PRIMARY KEY (id_announcement, id_parent),
    FOREIGN KEY (id_announcement) REFERENCES announcements(id_announcement) ON DELETE CASCADE,
    FOREIGN KEY (id_parent) REFERENCES parents(id_parent) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS academic_reports (
    id_report INT AUTO_INCREMENT PRIMARY KEY,
    id_student INT NOT NULL,
    report_type VARCHAR(60) NOT NULL,
    title VARCHAR(150) NOT NULL,
    format VARCHAR(20) NOT NULL,
    file_url VARCHAR(500),
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_student) REFERENCES students(id_student) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS citations (
    id_citation INT AUTO_INCREMENT PRIMARY KEY,
    id_student INT NOT NULL,
    id_parent INT,
    title VARCHAR(150) NOT NULL,
    detail TEXT,
    scheduled_at DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'scheduled',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_student) REFERENCES students(id_student) ON DELETE CASCADE,
    FOREIGN KEY (id_parent) REFERENCES parents(id_parent) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS parent_preferences (
    id_parent INT PRIMARY KEY,
    dark_mode BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_parent) REFERENCES parents(id_parent) ON DELETE CASCADE
);

-- 3. Semilla de datos para pruebas
INSERT INTO parents (first_name, last_name, email, phone, password) VALUES
    ('Carlos', 'Pérez', 'padre@learnix.com', '+51 987654321', '123456');

INSERT INTO parent_students (id_parent, id_student, relationship, is_primary) VALUES
    ((SELECT id_parent FROM parents WHERE email = 'padre@learnix.com'), (SELECT id_student FROM students WHERE first_name = 'Juan' LIMIT 1), 'Padre', TRUE);

UPDATE activities SET term = 'B1' WHERE MOD(id_activity, 2) = 0;
UPDATE activities SET term = 'B2' WHERE MOD(id_activity, 2) = 1;

INSERT INTO parent_alerts (id_student, id_parent, type, title, detail, severity, status, event_date) VALUES
    ((SELECT id_student FROM students WHERE first_name = 'Juan' LIMIT 1),
     (SELECT id_parent FROM parents WHERE email = 'padre@learnix.com'),
     'citation', 'Citación con tutor', 'Reunión con el tutor de Juan Pérez el 15/06/2026 a las 10:00 AM.', 'info', 'new', '2026-06-15 10:00:00');

INSERT INTO parent_alerts (id_student, id_parent, type, title, detail, severity, status) VALUES
    ((SELECT id_student FROM students WHERE first_name = 'Juan' LIMIT 1),
     (SELECT id_parent FROM parents WHERE email = 'padre@learnix.com'),
     'incident', 'Incidencia disciplinaria', 'Observación registrada durante el recreo por conducta inadecuada.', 'critical', 'new');

INSERT INTO announcements (title, body, sender_name, priority) VALUES
    ('Reunión general de padres', 'Estimados padres de familia, se les convoca a la primera reunión general del año para conversar sobre las actividades escolares.', 'Dirección', 'Alta');

INSERT INTO announcement_recipients (id_announcement, id_parent, status) VALUES
    ((SELECT id_announcement FROM announcements WHERE title = 'Reunión general de padres' LIMIT 1),
     (SELECT id_parent FROM parents WHERE email = 'padre@learnix.com'), 'new');

INSERT INTO academic_reports (id_student, report_type, title, format, file_url) VALUES
    ((SELECT id_student FROM students WHERE first_name = 'Juan' LIMIT 1), 'Rendimiento académico', 'Libreta de Notas B1', 'pdf', 'https://learnix.local/reports/1.pdf'),
    ((SELECT id_student FROM students WHERE first_name = 'Juan' LIMIT 1), 'Rendimiento académico', 'Libreta de Notas B1', 'excel', 'https://learnix.local/reports/1.xlsx');

INSERT INTO parent_preferences (id_parent, dark_mode) VALUES
    ((SELECT id_parent FROM parents WHERE email = 'padre@learnix.com'), FALSE);

-- 4. Creación de Procedimientos Almacenados
DELIMITER //

CREATE PROCEDURE sp_authenticate_parent(
    IN p_email VARCHAR(150),
    IN p_password VARCHAR(255),
    OUT o_id_parent INT,
    OUT o_first_name VARCHAR(100),
    OUT o_last_name VARCHAR(100),
    OUT o_email VARCHAR(150)
)
BEGIN
    SELECT id_parent, first_name, last_name, email
    INTO o_id_parent, o_first_name, o_last_name, o_email
    FROM parents
    WHERE email = TRIM(p_email) AND password = TRIM(p_password)
    LIMIT 1;
END //

CREATE PROCEDURE sp_get_parent_students(
    IN p_id_parent INT
)
BEGIN
    SELECT
        s.id_student,
        CONCAT(s.first_name, ' ', s.last_name) AS fullName,
        (
            SELECT CONCAT(p.name, ' - ', cp.section)
            FROM enrollments e
            JOIN course_periods cp ON e.id_course_period = cp.id_course_period
            JOIN periods p ON cp.id_period = p.id_period
            WHERE e.id_student = s.id_student AND e.status = 'active'
            LIMIT 1
        ) AS gradeSection,
        ps.is_primary AS isPrimary
    FROM parent_students ps
    JOIN students s ON ps.id_student = s.id_student
    WHERE ps.id_parent = p_id_parent;
END //

CREATE PROCEDURE sp_get_student_detail_mobile(
    IN p_id_student INT
)
BEGIN
    DECLARE v_general_average DECIMAL(10,2) DEFAULT 0.0;
    DECLARE v_attendance_pct DECIMAL(5,2) DEFAULT 0.0;
    DECLARE v_academic_status VARCHAR(20) DEFAULT 'good';
    
    SELECT COALESCE(AVG(
        CASE g.value
            WHEN 'AD' THEN 4.0
            WHEN 'A'  THEN 3.0
            WHEN 'B'  THEN 2.0
            WHEN 'C'  THEN 1.0
            ELSE 0.0
        END
    ), 0.0) INTO v_general_average
    FROM grades g
    JOIN activities a ON g.id_activity = a.id_activity
    WHERE g.id_student = p_id_student;

    SELECT COALESCE(
        (COUNT(CASE WHEN status = 'attended' THEN 1 END) / COUNT(*)) * 100,
        100.0
    ) INTO v_attendance_pct
    FROM attendances
    WHERE id_student = p_id_student;
    
    IF v_general_average >= 3.5 THEN
        SET v_academic_status = 'excellent';
    ELSEIF v_general_average < 2.5 THEN
        SET v_academic_status = 'atRisk';
    ELSE
        SET v_academic_status = 'good';
    END IF;

    SELECT
        s.id_student AS id,
        CONCAT(s.first_name, ' ', s.last_name) AS fullName,
        (
            SELECT CONCAT(p.name, ' - ', cp.section)
            FROM enrollments e
            JOIN course_periods cp ON e.id_course_period = cp.id_course_period
            JOIN periods p ON cp.id_period = p.id_period
            WHERE e.id_student = s.id_student AND e.status = 'active'
            LIMIT 1
        ) AS gradeSection,
        v_general_average AS generalAverage,
        v_attendance_pct AS attendancePercentage,
        v_academic_status AS academicStatus
    FROM students s
    WHERE s.id_student = p_id_student;
END //

CREATE PROCEDURE sp_get_student_course_averages(
    IN p_id_student INT
)
BEGIN
    SELECT
        c.id_course AS courseId,
        cp.id_course_period AS coursePeriodId,
        c.name AS courseName,
        COALESCE(AVG(
            CASE g.value
                WHEN 'AD' THEN 4.0
                WHEN 'A'  THEN 3.0
                WHEN 'B'  THEN 2.0
                WHEN 'C'  THEN 1.0
                ELSE 0.0
            END
        ), 0.0) AS average
    FROM enrollments e
    JOIN course_periods cp ON e.id_course_period = cp.id_course_period
    JOIN courses c ON cp.id_course = c.id_course
    LEFT JOIN activities a ON cp.id_course_period = a.id_course_period
    LEFT JOIN grades g ON g.id_activity = a.id_activity AND g.id_student = p_id_student
    WHERE e.id_student = p_id_student AND e.status = 'active'
    GROUP BY c.id_course, cp.id_course_period, c.name;
END //

CREATE PROCEDURE sp_get_student_activities_dashboard(
    IN p_id_student INT
)
BEGIN
    SELECT
        a.id_activity AS id,
        a.name AS name,
        c.name AS courseName,
        COALESCE(a.term, 'B1') AS term,
        a.due_date AS date,
        COALESCE(g.value, 'Sin calificar') AS grade,
        CASE WHEN g.value IS NOT NULL THEN 'Calificada' ELSE 'Pendiente' END AS status
    FROM enrollments e
    JOIN course_periods cp ON e.id_course_period = cp.id_course_period
    JOIN courses c ON cp.id_course = c.id_course
    JOIN activities a ON cp.id_course_period = a.id_course_period
    LEFT JOIN grades g ON a.id_activity = g.id_activity AND g.id_student = p_id_student
    WHERE e.id_student = p_id_student AND e.status = 'active'
    ORDER BY a.due_date DESC
    LIMIT 5;
END //

CREATE PROCEDURE sp_get_student_attendance_stats(
    IN p_id_student INT
)
BEGIN
    SELECT
        COUNT(CASE WHEN status = 'attended' THEN 1 END) AS attendedDays,
        COUNT(CASE WHEN status = 'absent' THEN 1 END) AS absentDays,
        COUNT(CASE WHEN status = 'late' THEN 1 END) AS lateDays,
        COALESCE(
            (COUNT(CASE WHEN status = 'attended' THEN 1 END) / COUNT(*)) * 100,
            100.0
        ) AS attendancePercentage
    FROM attendances
    WHERE id_student = p_id_student;
END //

CREATE PROCEDURE sp_get_student_reports_dashboard(
    IN p_id_student INT
)
BEGIN
    SELECT
        MIN(id_report) AS id,
        title,
        report_type AS description,
        GROUP_CONCAT(format) AS formats
    FROM academic_reports
    WHERE id_student = p_id_student
    GROUP BY title, report_type;
END //

CREATE PROCEDURE sp_get_student_term_progress(
    IN p_id_student INT
)
BEGIN
    SELECT
        COALESCE(a.term, 'B1') AS term,
        COALESCE(AVG(
            CASE g.value
                WHEN 'AD' THEN 4.0
                WHEN 'A'  THEN 3.0
                WHEN 'B'  THEN 2.0
                WHEN 'C'  THEN 1.0
                ELSE 0.0
            END
        ), 0.0) AS average
    FROM enrollments e
    JOIN course_periods cp ON e.id_course_period = cp.id_course_period
    JOIN activities a ON cp.id_course_period = a.id_course_period
    JOIN grades g ON a.id_activity = g.id_activity AND g.id_student = p_id_student
    WHERE e.id_student = p_id_student AND e.status = 'active'
    GROUP BY COALESCE(a.term, 'B1')
    ORDER BY term;
END //

CREATE PROCEDURE sp_get_student_progress_activities(
    IN p_id_student INT,
    IN p_term VARCHAR(10),
    IN p_course_id INT
)
BEGIN
    SELECT
        a.id_activity AS id,
        a.name AS name,
        c.id_course AS courseId,
        c.name AS courseName,
        COALESCE(a.term, 'B1') AS term,
        a.due_date AS date,
        COALESCE(g.value, 'Sin calificar') AS grade,
        CASE WHEN g.value IS NOT NULL THEN 'Calificada' ELSE 'Pendiente' END AS status
    FROM enrollments e
    JOIN course_periods cp ON e.id_course_period = cp.id_course_period
    JOIN courses c ON cp.id_course = c.id_course
    JOIN activities a ON cp.id_course_period = a.id_course_period
    LEFT JOIN grades g ON a.id_activity = g.id_activity AND g.id_student = p_id_student
    WHERE e.id_student = p_id_student
      AND (p_term IS NULL OR p_term = '' OR COALESCE(a.term, 'B1') = p_term)
      AND (p_course_id IS NULL OR p_course_id = 0 OR c.id_course = p_course_id)
    ORDER BY a.due_date DESC;
END //

CREATE PROCEDURE sp_get_student_attendance_detail(
    IN p_id_student INT,
    IN p_from DATE,
    IN p_to DATE
)
BEGIN
    SELECT
        date,
        status
    FROM attendances
    WHERE id_student = p_id_student
      AND (p_from IS NULL OR date >= p_from)
      AND (p_to IS NULL OR date <= p_to)
    ORDER BY date DESC;
END //

CREATE PROCEDURE sp_get_student_reminders(
    IN p_id_student INT,
    IN p_id_parent INT,
    IN p_today DATE
)
BEGIN
    SELECT * FROM (
        SELECT
            a.id_activity AS id,
            'activityDue' AS type,
            a.name AS title,
            CONCAT(c.name, ' vence el ', DATE_FORMAT(a.due_date, '%Y-%m-%d'), ' (', COALESCE(a.term, 'B1'), ').') AS detail,
            a.due_date AS date,
            'warning' AS severity
        FROM enrollments e
        JOIN course_periods cp ON e.id_course_period = cp.id_course_period
        JOIN courses c ON cp.id_course = c.id_course
        JOIN activities a ON cp.id_course_period = a.id_course_period
        LEFT JOIN grades g ON a.id_activity = g.id_activity AND g.id_student = p_id_student
        WHERE e.id_student = p_id_student
          AND g.value IS NULL
          AND a.due_date >= p_today
          AND a.due_date <= DATE_ADD(p_today, INTERVAL 4 DAY)
        
        UNION ALL
        
        SELECT
            id_alert AS id,
            'citation' AS type,
            title,
            detail,
            CAST(event_date AS DATE) AS date,
            severity
        FROM parent_alerts
        WHERE id_student = p_id_student
          AND (p_id_parent IS NULL OR id_parent = p_id_parent)
          AND type = 'citation'
          AND event_date >= DATE_ADD(p_today, INTERVAL 7 DAY)
          AND event_date <= DATE_ADD(p_today, INTERVAL 11 DAY)
    ) AS results
    ORDER BY date ASC;
END //

CREATE PROCEDURE sp_get_student_incidents(
    IN p_id_student INT,
    IN p_id_parent INT,
    IN p_status VARCHAR(20)
)
BEGIN
    SELECT
        id_alert AS id,
        title,
        detail,
        CAST(created_at AS DATE) AS date,
        status,
        severity
    FROM parent_alerts
    WHERE id_student = p_id_student
      AND (p_id_parent IS NULL OR id_parent = p_id_parent)
      AND type = 'incident'
      AND (p_status IS NULL OR p_status = '' OR status = p_status)
    ORDER BY created_at DESC;
END //

CREATE PROCEDURE sp_get_parent_profile_summary(
    IN p_id_parent INT
)
BEGIN
    SELECT id_parent AS id, CONCAT(first_name, ' ', last_name) AS fullName, email
    FROM parents
    WHERE id_parent = p_id_parent;
END //

CREATE PROCEDURE sp_get_parent_announcements(
    IN p_id_parent INT,
    IN p_priority VARCHAR(20),
    IN p_sender VARCHAR(150),
    IN p_status VARCHAR(20)
)
BEGIN
    SELECT
        a.id_announcement AS id,
        a.title,
        a.body,
        a.sender_name AS sender,
        a.priority,
        CAST(a.published_at AS DATE) AS date,
        ar.status
    FROM announcements a
    JOIN announcement_recipients ar ON a.id_announcement = ar.id_announcement
    WHERE ar.id_parent = p_id_parent
      AND (p_priority IS NULL OR p_priority = '' OR a.priority = p_priority)
      AND (p_sender IS NULL OR p_sender = '' OR a.sender_name = p_sender)
      AND (p_status IS NULL OR p_status = '' OR ar.status = p_status)
    ORDER BY a.published_at DESC;
END //

CREATE PROCEDURE sp_mark_announcement_read(
    IN p_id_parent INT,
    IN p_id_announcement INT
)
BEGIN
    UPDATE announcement_recipients
    SET status = 'read', read_at = CURRENT_TIMESTAMP
    WHERE id_parent = p_id_parent AND id_announcement = p_id_announcement;
    
    SELECT
        id_announcement AS id,
        status,
        read_at AS readAt
    FROM announcement_recipients
    WHERE id_parent = p_id_parent AND id_announcement = p_id_announcement;
END //

CREATE PROCEDURE sp_get_student_reports(
    IN p_id_student INT,
    IN p_type VARCHAR(60),
    IN p_format VARCHAR(20)
)
BEGIN
    SELECT
        id_report AS id,
        title,
        report_type AS description,
        format,
        file_url AS url,
        generated_at AS generatedAt
    FROM academic_reports
    WHERE id_student = p_id_student
      AND (p_type IS NULL OR p_type = '' OR report_type = p_type)
      AND (p_format IS NULL OR p_format = '' OR format = p_format)
    ORDER BY generated_at DESC;
END //

CREATE PROCEDURE sp_update_parent_preferences(
    IN p_id_parent INT,
    IN p_dark_mode BOOLEAN
)
BEGIN
    INSERT INTO parent_preferences (id_parent, dark_mode, updated_at)
    VALUES (p_id_parent, p_dark_mode, CURRENT_TIMESTAMP)
    ON DUPLICATE KEY UPDATE dark_mode = p_dark_mode, updated_at = CURRENT_TIMESTAMP;
    
    SELECT id_parent AS parentId, dark_mode AS darkMode, updated_at AS updatedAt
    FROM parent_preferences
    WHERE id_parent = p_id_parent;
END //

DELIMITER ;
