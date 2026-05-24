DELIMITER //
CREATE PROCEDURE sp_get_student_report_card(
    IN p_id_student INT,
    IN p_id_course_period INT
)
BEGIN
    SELECT
        a.name AS activity_name,
        a.weight AS activity_weight,
        COALESCE(g.value, 'Sin calificar') AS grade_value,
        a.created_at
    FROM activities a
             LEFT JOIN grades g ON g.id_activity = a.id_activity AND g.id_student = p_id_student
    WHERE a.id_course_period = p_id_course_period
    ORDER BY a.created_at ASC;
END //
DELIMITER ;