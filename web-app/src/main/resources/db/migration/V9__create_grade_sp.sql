-- 1. SP para obtener las estadísticas de la actividad
CREATE OR REPLACE PROCEDURE public.sp_stats_registro_notas(
    IN p_id_activity INT,
    OUT p_total_estudiantes INT,
    OUT p_aprobados INT,
    OUT p_desaprobados INT
)
LANGUAGE plpgsql AS $$
BEGIN
    -- Contamos el total de alumnos inscritos en el curso de esta actividad
    SELECT COUNT(*) INTO p_total_estudiantes
    FROM enrollments e
    JOIN activities a ON e.id_course_period = a.id_course_period
    WHERE a.id_activity = p_id_activity AND e.status = 'active';

    -- Aprobados (AD, A)
    SELECT COUNT(*) INTO p_aprobados
    FROM grades g
    WHERE g.id_activity = p_id_activity AND g.value IN ('AD', 'A');

    -- Desaprobados (B, C)
    SELECT COUNT(*) INTO p_desaprobados
    FROM grades g
    WHERE g.id_activity = p_id_activity AND g.value IN ('B', 'C');
END;
$$;

-- 2. SP para listar estudiantes y sus notas (Left Join imperativo)
CREATE OR REPLACE PROCEDURE public.sp_get_activity_grades(
    IN p_id_activity INT,
    OUT o_result_cursor REFCURSOR
)
LANGUAGE plpgsql AS $$
BEGIN
    OPEN o_result_cursor FOR
        SELECT s.id_student, s.first_name, s.last_name, g.value AS grade_value
        FROM students s
        JOIN enrollments e ON s.id_student = e.id_student
        JOIN activities a ON e.id_course_period = a.id_course_period
        LEFT JOIN grades g ON s.id_student = g.id_student AND g.id_activity = p_id_activity
        WHERE a.id_activity = p_id_activity
        ORDER BY s.last_name, s.first_name;
END;
$$;

-- 3. SP para guardar o actualizar la nota (On Conflict)
CREATE OR REPLACE PROCEDURE public.sp_save_student_grade(
    IN p_id_student INT,
    IN p_id_activity INT,
    IN p_value VARCHAR
)
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO grades (id_student, id_activity, value, updated_at)
    VALUES (p_id_student, p_id_activity, TRIM(p_value), CURRENT_TIMESTAMP)
    ON CONFLICT (id_student, id_activity)
    DO UPDATE SET value = TRIM(p_value), updated_at = CURRENT_TIMESTAMP;
END;
$$;