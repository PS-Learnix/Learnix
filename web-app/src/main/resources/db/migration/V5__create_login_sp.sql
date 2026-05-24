DELIMITER //
CREATE PROCEDURE sp_authenticate_user(
    IN p_email VARCHAR(150),
    IN p_password VARCHAR(255),
    OUT o_id_user INT,
    OUT o_first_name VARCHAR(100),
    OUT o_last_name VARCHAR(100),
    OUT o_email VARCHAR(150)
)
BEGIN
    SELECT
        id_user,
        first_name,
        last_name,
        email
    INTO
        o_id_user,
        o_first_name,
        o_last_name,
        o_email
    FROM users
    WHERE email = TRIM(p_email)
                      AND password = TRIM(p_password)
                  LIMIT 1;
END //
DELIMITER ;