CREATE TABLE IF NOT EXISTS student_observations (
                                                    id_observation INT AUTO_INCREMENT PRIMARY KEY,
                                                    id_student INT NOT NULL,
                                                    id_teacher INT NOT NULL,
                                                    comment TEXT NOT NULL,
                                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                    FOREIGN KEY (id_student) REFERENCES students(id_student) ON DELETE CASCADE,
                                                    FOREIGN KEY (id_teacher) REFERENCES users(id_user) ON DELETE CASCADE
);

DELIMITER //

CREATE PROCEDURE sp_add_student_observation(
    IN p_id_student INT,
    IN p_id_teacher INT,
    IN p_comment TEXT,
    OUT o_id_observation INT
)
BEGIN
    INSERT INTO student_observations (id_student, id_teacher, comment, created_at)
    VALUES (p_id_student, p_id_teacher, TRIM(p_comment), CURRENT_TIMESTAMP);

    SET o_id_observation = LAST_INSERT_ID();
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE sp_get_student_observations(
    IN p_id_student INT
)
BEGIN
    SELECT
        obs.id_observation,
        obs.comment,
        obs.created_at,
        CONCAT(u.first_name, ' ', u.last_name) AS teacher_name
    FROM student_observations obs
             JOIN users u ON obs.id_teacher = u.id_user
    WHERE obs.id_student = p_id_student
    ORDER BY obs.created_at DESC;
END //
DELIMITER ;
