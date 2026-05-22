-- 1. SP para obtener las métricas globales del profesor
CREATE OR REPLACE PROCEDURE public.sp_get_teacher_stats(
    IN p_id_teacher INT,
    OUT o_total_courses INT,
    OUT o_total_students INT
)
LANGUAGE plpgsql AS $$
BEGIN
    -- Contamos los cursos distintos asignados al profesor
    SELECT COUNT(DISTINCT id_course) INTO o_total_courses
    FROM courses
    WHERE id_teacher = p_id_teacher;

    -- Contamos los estudiantes únicos inscritos en sus secciones
    SELECT COUNT(DISTINCT e.id_student) INTO o_total_students
    FROM enrollments e
    JOIN course_periods cp ON e.id_course_period = cp.id_course_period
    JOIN courses c ON cp.id_course = c.id_course
    WHERE c.id_teacher = p_id_teacher AND e.status = 'active';
END;
$$;

-- 2. SP para listar los cursos del profesor junto con el conteo de secciones
CREATE OR REPLACE PROCEDURE public.sp_get_teacher_profile_courses(
    IN p_id_teacher INT,
    OUT o_courses_cursor REFCURSOR
)
LANGUAGE plpgsql AS $$
BEGIN
    OPEN o_courses_cursor FOR
        SELECT c.id_course, c.name, COUNT(cp.id_course_period) AS total_sections
        FROM courses c
        LEFT JOIN course_periods cp ON c.id_course = cp.id_course
        WHERE c.id_teacher = p_id_teacher
        GROUP BY c.id_course, c.name
        ORDER BY c.name;
END;
$$;