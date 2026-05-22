CREATE OR REPLACE PROCEDURE sp_authenticate_user(
    IN p_email VARCHAR,
    IN p_password VARCHAR,
    OUT o_id_user INT,
    OUT o_first_name VARCHAR,
    OUT o_last_name VARCHAR,
    OUT o_email VARCHAR
)
    LANGUAGE plpgsql
AS $$
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

END;
$$;