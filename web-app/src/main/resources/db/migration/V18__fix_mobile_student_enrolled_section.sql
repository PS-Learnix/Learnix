-- =========================================================================
-- FLYWAY V18: SECCION MATRICULADA PARA DASHBOARD MOVIL DE PADRES
-- =========================================================================

DELIMITER //

DROP PROCEDURE IF EXISTS sp_get_parent_students //
CREATE PROCEDURE sp_get_parent_students(
    IN p_id_parent INT
)
BEGIN
    SELECT
        s.id_student,
        CONCAT(s.first_name, ' ', s.last_name) AS fullName,
        COALESCE(
            (
                SELECT GROUP_CONCAT(DISTINCT cp.section ORDER BY cp.section SEPARATOR ', ')
                FROM enrollments e
                JOIN course_periods cp ON e.id_course_period = cp.id_course_period
                WHERE e.id_student = s.id_student
                  AND e.status = 'active'
                  AND cp.section IS NOT NULL
            ),
            'Sin seccion'
        ) AS gradeSection,
        ps.is_primary AS isPrimary
    FROM parent_students ps
    JOIN students s ON ps.id_student = s.id_student
    WHERE ps.id_parent = p_id_parent;
END //

DROP PROCEDURE IF EXISTS sp_get_student_detail_mobile //
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
        COALESCE(
            (
                SELECT GROUP_CONCAT(DISTINCT cp.section ORDER BY cp.section SEPARATOR ', ')
                FROM enrollments e
                JOIN course_periods cp ON e.id_course_period = cp.id_course_period
                WHERE e.id_student = s.id_student
                  AND e.status = 'active'
                  AND cp.section IS NOT NULL
            ),
            'Sin seccion'
        ) AS gradeSection,
        v_general_average AS generalAverage,
        v_attendance_pct AS attendancePercentage,
        v_academic_status AS academicStatus
    FROM students s
    WHERE s.id_student = p_id_student;
END //

DELIMITER ;
