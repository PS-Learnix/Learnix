-- 1. Obtener las actividades de un periodo de curso específico
CREATE OR REPLACE PROCEDURE public.sp_get_activities_by_period(
    IN p_id_course_period INT,
    OUT o_activities_cursor REFCURSOR
)
LANGUAGE plpgsql AS $$
BEGIN
    OPEN o_activities_cursor FOR
        SELECT id_activity, id_course_period, name, weight, created_at, updated_at
        FROM activities
        WHERE id_course_period = p_id_course_period
        ORDER BY created_at DESC;
END;
$$;

-- 2. Insertar una nueva actividad (Uso imperativo estricto)
CREATE OR REPLACE PROCEDURE public.sp_create_activity(
    IN p_id_course_period INT,
    IN p_name VARCHAR,
    IN p_weight DECIMAL,
    OUT o_id_activity INT
)
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO activities (id_course_period, name, weight)
    VALUES (p_id_course_period, TRIM(p_name), p_weight)
    RETURNING id_activity INTO o_id_activity;
END;
$$;