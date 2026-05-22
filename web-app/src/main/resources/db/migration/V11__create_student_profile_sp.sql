CREATE OR REPLACE PROCEDURE public.sp_get_student_dashboard_courses(
    IN p_id_student INT,
    OUT o_result_cursor REFCURSOR
)
LANGUAGE plpgsql AS $$
BEGIN
    OPEN o_result_cursor FOR
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
END;
$$;