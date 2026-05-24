DELIMITER //
CREATE PROCEDURE sp_toggle_attendance(
    IN p_id_student INT,
    IN p_id_course_period INT,
    IN p_id_teacher INT,
    IN p_date DATE,
    IN p_current_state VARCHAR(10), -- 'true', 'false' o 'null'
    OUT o_new_state VARCHAR(10)
)
BEGIN
    IF p_current_state = 'true' THEN
        -- Cambia de Asistió (true) a Faltó (false)
        INSERT INTO attendances (id_student, id_course_period, id_teacher, date, did_attend, created_at, updated_at)
        VALUES (p_id_student, p_id_course_period, p_id_teacher, p_date, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON DUPLICATE KEY UPDATE did_attend = FALSE, updated_at = CURRENT_TIMESTAMP;

        SET o_new_state = 'false';

    ELSEIF p_current_state = 'false' THEN
        -- De Faltó (false) se elimina el registro (vuelve a null)
        DELETE FROM attendances
        WHERE id_student = p_id_student
          AND id_course_period = p_id_course_period
          AND date = p_date;

        SET o_new_state = 'null';

    ELSE
        -- De Sin Registro (null) cambia a Asistió (true)
        INSERT INTO attendances (id_student, id_course_period, id_teacher, date, did_attend, created_at, updated_at)
        VALUES (p_id_student, p_id_course_period, p_id_teacher, p_date, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

        SET o_new_state = 'true';
    END IF;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE sp_get_course_students(
    IN p_id_course_period INT
)
BEGIN
    SELECT s.id_student, s.first_name, s.last_name, s.dni
    FROM students s
             JOIN enrollments e ON s.id_student = e.id_student
    WHERE e.id_course_period = p_id_course_period AND e.status = 'active'
    ORDER BY s.last_name, s.first_name;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE sp_get_course_attendances(
    IN p_id_course_period INT
)
BEGIN
    SELECT id_student, date, did_attend
    FROM attendances
    WHERE id_course_period = p_id_course_period;
END //
DELIMITER ;
