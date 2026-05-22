CREATE OR REPLACE PROCEDURE public.sp_toggle_attendance(
    IN p_id_student INT,
    IN p_id_course_period INT,
    IN p_id_teacher INT,
    IN p_date DATE,
    IN p_current_state VARCHAR, -- 'true', 'false' o 'null'
    OUT o_new_state VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    IF p_current_state = 'true' THEN
        -- Ciclo 2: De Asistió (true) cambia a Faltó (false)
        INSERT INTO attendances (id_student, id_course_period, id_teacher, date, did_attend)
        VALUES (p_id_student, p_id_course_period, p_id_teacher, p_date, FALSE)
        ON CONFLICT (id_student, id_course_period, date)
        DO UPDATE SET did_attend = FALSE, updated_at = CURRENT_TIMESTAMP;

        o_new_state := 'false';

    ELSIF p_current_state = 'false' THEN
        -- Ciclo 3: De Faltó (false) se elimina el registro (vuelve a null)
        DELETE FROM attendances
        WHERE id_student = p_id_student
          AND id_course_period = p_id_course_period
          AND date = p_date;

        o_new_state := 'null';

    ELSE
        -- Ciclo 1: De Sin Registro (null) cambia a Asistió (true)
        INSERT INTO attendances (id_student, id_course_period, id_teacher, date, did_attend)
        VALUES (p_id_student, p_id_course_period, p_id_teacher, p_date, TRUE);

        o_new_state := 'true';
    END IF;
END;
$$;

CREATE OR REPLACE PROCEDURE public.sp_get_course_students(
    IN p_id_course_period INT,
    OUT o_students_cursor REFCURSOR
)
LANGUAGE plpgsql AS $$
BEGIN
    OPEN o_students_cursor FOR
        SELECT s.id_student, s.first_name, s.last_name, s.dni
        FROM students s
        JOIN enrollments e ON s.id_student = e.id_student
        WHERE e.id_course_period = p_id_course_period AND e.status = 'active'
        ORDER BY s.last_name, s.first_name;
END;
$$;

CREATE OR REPLACE PROCEDURE public.sp_get_course_attendances(
    IN p_id_course_period INT,
    OUT o_attendances_cursor REFCURSOR
)
LANGUAGE plpgsql AS $$
BEGIN
    OPEN o_attendances_cursor FOR
        SELECT id_student, date, did_attend
        FROM attendances
        WHERE id_course_period = p_id_course_period;
END;
$$;

