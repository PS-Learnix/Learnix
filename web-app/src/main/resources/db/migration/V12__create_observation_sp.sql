-- 1. Tabla de observaciones académicas
CREATE TABLE IF NOT EXISTS public.student_observations (
    id_observation SERIAL PRIMARY KEY,
    id_student INT NOT NULL,
    id_teacher INT NOT NULL,
    comment TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_obs_student FOREIGN KEY (id_student) REFERENCES public.students(id_student) ON DELETE CASCADE,
    CONSTRAINT fk_obs_teacher FOREIGN KEY (id_teacher) REFERENCES public.users(id_user) ON DELETE CASCADE
);

-- 2. SP para registrar una nueva observación
CREATE OR REPLACE PROCEDURE public.sp_add_student_observation(
    IN p_id_student INT,
    IN p_id_teacher INT,
    IN p_comment TEXT,
    OUT o_id_observation INT
)
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO public.student_observations (id_student, id_teacher, comment)
    VALUES (p_id_student, p_id_teacher, TRIM(p_comment))
    RETURNING id_observation INTO o_id_observation;
END;
$$;

-- 3. SP para listar las observaciones de un alumno de forma descendente
CREATE OR REPLACE PROCEDURE public.sp_get_student_observations(
    IN p_id_student INT,
    OUT o_result_cursor REFCURSOR
)
LANGUAGE plpgsql AS $$
BEGIN
    OPEN o_result_cursor FOR
        SELECT obs.id_observation, obs.comment, obs.created_at, u.first_name || ' ' || u.last_name AS teacher_name
        FROM public.student_observations obs
        JOIN public.users u ON obs.id_teacher = u.id_user
        WHERE obs.id_student = p_id_student
        ORDER BY obs.created_at DESC;
END;
$$;