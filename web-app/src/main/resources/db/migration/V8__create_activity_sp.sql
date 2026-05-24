DELIMITER //
CREATE PROCEDURE sp_get_activities_by_period(
    IN p_id_course_period INT
)
BEGIN
    SELECT id_activity, id_course_period, name, weight, created_at, updated_at
    FROM activities
    WHERE id_course_period = p_id_course_period
    ORDER BY created_at DESC;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE sp_create_activity(
    IN p_id_course_period INT,
    IN p_name VARCHAR(100),
    IN p_weight DECIMAL(5,2),
    OUT o_id_activity INT
)
BEGIN
    INSERT INTO activities (id_course_period, name, weight, created_at, updated_at)
    VALUES (p_id_course_period, TRIM(p_name), p_weight, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

    SET o_id_activity = LAST_INSERT_ID();
END //
DELIMITER ;
