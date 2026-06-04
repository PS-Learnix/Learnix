DELIMITER //
CREATE PROCEDURE sp_get_teacher_courses(
    IN p_id_teacher INT
)
BEGIN
    SELECT c.id_course, c.name, cp.id_course_period, cp.section
    FROM courses c
             JOIN course_periods cp ON c.id_course = cp.id_course
    WHERE c.id_teacher = p_id_teacher;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE sp_overall_course_average(
    IN p_id_course_period INT,
    OUT o_promedio DECIMAL(10,2)
)
BEGIN
    SELECT COALESCE(AVG(
                            CASE g.value
                                WHEN 'AD' THEN 4.0
                                WHEN 'A'  THEN 3.0
                                WHEN 'B'  THEN 2.0
                                WHEN 'C'  THEN 1.0
                                ELSE 0.0
                                END
                    ), 0.0) INTO o_promedio
    FROM grades g
             JOIN activities a ON g.id_activity = a.id_activity
    WHERE a.id_course_period = p_id_course_period;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE sp_get_student_dashboard_courses(
    IN p_id_student INT
)
BEGIN
    SELECT
        cp.id_course_period,
        c.name AS course_name,
        COALESCE(SUM(
                         CASE g.value
                             WHEN 'AD' THEN 4.0
                             WHEN 'A'  THEN 3.0
                             WHEN 'B'  THEN 2.0
                             WHEN 'C'  THEN 1.0
                             ELSE 0.0
                             END * (a.weight / 100.0)
                 ), 0.0) AS promedio
    FROM enrollments e
             JOIN course_periods cp ON e.id_course_period = cp.id_course_period
             JOIN courses c ON cp.id_course = c.id_course
             JOIN activities a ON cp.id_course_period = a.id_course_period
             LEFT JOIN grades g ON g.id_student = e.id_student AND g.id_activity = a.id_activity
    WHERE e.id_student = p_id_student
    GROUP BY cp.id_course_period, c.name
    ORDER BY c.name;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE sp_average_course_attendance(
    IN p_id_course_period INT,
    OUT o_asistencia_pct DECIMAL(5,2)
)
BEGIN
    DECLARE v_total_clases INT;
    DECLARE v_asistencias INT;

    SELECT COUNT(*) INTO v_total_clases
    FROM attendances
    WHERE id_course_period = p_id_course_period;

    IF v_total_clases = 0 THEN
        SET o_asistencia_pct = 0;
    ELSE
        SELECT COUNT(*) INTO v_asistencias
        FROM attendances
        WHERE id_course_period = p_id_course_period AND did_attend = 1;

        SET o_asistencia_pct = ROUND((v_asistencias / v_total_clases) * 100);
    END IF;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE sp_students_risk_success(
    IN p_id_course_period INT,
    OUT o_aprobados_pct DECIMAL(5,2),
    OUT o_riesgo_pct DECIMAL(5,2)
)
BEGIN
    DECLARE v_total_students INT;
    DECLARE v_aprobados INT DEFAULT 0;
    DECLARE v_riesgo INT DEFAULT 0;

    -- 1. Obtener total de estudiantes activos
    SELECT COUNT(*) INTO v_total_students
    FROM enrollments
    WHERE id_course_period = p_id_course_period AND status = 'active';

    -- 2. Validar si hay estudiantes para evitar división por cero
    IF v_total_students = 0 THEN
        SET o_aprobados_pct = 0;
        SET o_riesgo_pct = 0;
    ELSE
        -- 3. Agrupamos y contamos aprobados/riesgo en una sola consulta
        SELECT
            SUM(CASE WHEN sub.promedio_alumno >= 2.5 THEN 1 ELSE 0 END),
            SUM(CASE WHEN sub.promedio_alumno < 2.5 THEN 1 ELSE 0 END)
        INTO v_aprobados, v_riesgo
        FROM (
                 SELECT g.id_student,
                        AVG(CASE g.value
                                WHEN 'AD' THEN 4.0
                                WHEN 'A'  THEN 3.0
                                WHEN 'B'  THEN 2.0
                                WHEN 'C'  THEN 1.0
                                ELSE 0.0
                            END) AS promedio_alumno
                 FROM grades g
                          JOIN activities a ON g.id_activity = a.id_activity
                 WHERE a.id_course_period = p_id_course_period
                 GROUP BY g.id_student
             ) AS sub;

        -- 4. Asegurar que si el conteo da NULL (por falta de notas), sea 0
        SET v_aprobados = IFNULL(v_aprobados, 0);
        SET v_riesgo = IFNULL(v_riesgo, 0);

        -- 5. Cálculos finales
        SET o_aprobados_pct = ROUND((v_aprobados / v_total_students) * 100);
        SET o_riesgo_pct = ROUND((v_riesgo / v_total_students) * 100);
    END IF;
END //
DELIMITER ;