-- MIGRACIÓN FLYWAY V15: CORRECCIÓN DE SINCRONIZACIÓN DE ESTADO DE ASISTENCIA
-- =========================================================================

DROP PROCEDURE IF EXISTS sp_toggle_attendance;

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
        INSERT INTO attendances (id_student, id_course_period, id_teacher, date, did_attend, status, created_at, updated_at)
        VALUES (p_id_student, p_id_course_period, p_id_teacher, p_date, FALSE, 'absent', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON DUPLICATE KEY UPDATE did_attend = FALSE, status = 'absent', updated_at = CURRENT_TIMESTAMP;

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
        INSERT INTO attendances (id_student, id_course_period, id_teacher, date, did_attend, status, created_at, updated_at)
        VALUES (p_id_student, p_id_course_period, p_id_teacher, p_date, TRUE, 'attended', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON DUPLICATE KEY UPDATE did_attend = TRUE, status = 'attended', updated_at = CURRENT_TIMESTAMP;

        SET o_new_state = 'true';
    END IF;
END //

DELIMITER ;

-- Sincronizar todos los registros existentes
UPDATE attendances 
SET status = CASE WHEN did_attend = TRUE THEN 'attended' ELSE 'absent' END;
