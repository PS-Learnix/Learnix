CREATE OR REPLACE PROCEDURE public.sp_get_teacher_courses(
    IN p_id_teacher INT,
    OUT o_result_cursor REFCURSOR
)
    LANGUAGE plpgsql
AS $$
BEGIN
    OPEN o_result_cursor FOR
        SELECT c.id_course, c.name, cp.id_course_period, cp.section
        FROM courses c
                 JOIN course_periods cp ON c.id_course = cp.id_course
        WHERE c.id_teacher = p_id_teacher;
END;
$$;

CREATE OR REPLACE PROCEDURE public.sp_promedio_general_curso(
    IN p_id_course_period INT,
    OUT p_promedio NUMERIC
)
    LANGUAGE plpgsql AS $$
BEGIN
    SELECT COALESCE(AVG(
                            CASE g.value
                                WHEN 'AD' THEN 4.0
                                WHEN 'A'  THEN 3.0
                                WHEN 'B'  THEN 2.0
                                WHEN 'C'  THEN 1.0
                                ELSE 0.0
                                END
                    ), 0.0)
    INTO p_promedio
    FROM grades g
             JOIN activities a ON g.id_activity = a.id_activity
    WHERE a.id_course_period = p_id_course_period;
END;
$$;

CREATE OR REPLACE PROCEDURE public.sp_promedio_asistencia_curso(
    IN p_id_course_period INT,
    OUT p_asistencia_pct NUMERIC
)
    LANGUAGE plpgsql AS $$
DECLARE
    v_total_clases INT;
    v_asistencias INT;
BEGIN
    SELECT COUNT(*) INTO v_total_clases
    FROM attendances
    WHERE id_course_period = p_id_course_period;

    IF v_total_clases = 0 THEN
        p_asistencia_pct := 0;
    ELSE
        SELECT COUNT(*) INTO v_asistencias
        FROM attendances
        WHERE id_course_period = p_id_course_period AND did_attend = TRUE;

        p_asistencia_pct := ROUND((v_asistencias::NUMERIC / v_total_clases::NUMERIC) * 100);
    END IF;
END;
$$;

CREATE OR REPLACE PROCEDURE public.sp_estudiantes_riesgo_exito(
    IN p_id_course_period INT,
    OUT p_aprobados_pct NUMERIC,
    OUT p_riesgo_pct NUMERIC
)
    LANGUAGE plpgsql AS $$
DECLARE
    v_total_students INT;
    v_aprobados INT := 0;
    v_riesgo INT := 0;
    v_registro RECORD;
BEGIN

    SELECT COUNT(*) INTO v_total_students
    FROM enrollments
    WHERE id_course_period = p_id_course_period AND status = 'active';

    IF v_total_students = 0 THEN
        p_aprobados_pct := 0;
        p_riesgo_pct := 0;
    ELSE

        FOR v_registro IN (
            SELECT g.id_student,
                   AVG(CASE g.value
                           WHEN 'AD' THEN 4.0
                           WHEN 'A'  THEN 3.0
                           WHEN 'B'  THEN 2.0
                           WHEN 'C'  THEN 1.0
                           ELSE 0.0
                       END) as promedio_alumno
            FROM grades g
                     JOIN activities a ON g.id_activity = a.id_activity
            WHERE a.id_course_period = p_id_course_period
            GROUP BY g.id_student
        ) LOOP

                IF v_registro.promedio_alumno >= 2.5 THEN
                    v_aprobados := v_aprobados + 1;
                ELSE
                    v_riesgo := v_riesgo + 1;
                END IF;
            END LOOP;

        p_aprobados_pct := ROUND((v_aprobados::NUMERIC / v_total_students::NUMERIC) * 100);
        p_riesgo_pct := ROUND((v_riesgo::NUMERIC / v_total_students::NUMERIC) * 100);
    END IF;
END;
$$;