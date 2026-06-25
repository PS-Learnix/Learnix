-- =========================================================================
-- MIGRACIÓN FLYWAY V17: PROCEDIMIENTOS ALMACENADOS DE CITACIONES PARA DOCENTES (WEB)
-- =========================================================================

DELIMITER //

DROP PROCEDURE IF EXISTS sp_create_virtual_citation //
CREATE PROCEDURE sp_create_virtual_citation(
    IN p_id_student INT,
    IN p_id_course_period INT,
    IN p_id_teacher INT,
    IN p_title VARCHAR(150),
    IN p_detail TEXT,
    IN p_scheduled_at DATETIME,
    IN p_mode VARCHAR(20),
    IN p_meeting_url VARCHAR(500),
    IN p_scope VARCHAR(20),
    OUT o_id_citation INT
)
BEGIN
    DECLARE v_citation_id INT;
    
    -- Insert citation
    INSERT INTO virtual_citations (id_student, id_course_period, id_teacher, created_by_user_id, title, detail, scheduled_at, mode, meeting_url, scope, global_status)
    VALUES (p_id_student, p_id_course_period, p_id_teacher, p_id_teacher, p_title, p_detail, p_scheduled_at, p_mode, p_meeting_url, p_scope, 'scheduled');
    
    SET v_citation_id = LAST_INSERT_ID();
    
    -- Insert recipients based on scope
    IF p_scope = 'individual' THEN
        INSERT INTO citation_recipients (id_citation, id_parent, id_student, recipient_status)
        SELECT v_citation_id, ps.id_parent, p_id_student, 'pending'
        FROM parent_students ps
        WHERE ps.id_student = p_id_student;
    ELSE
        INSERT INTO citation_recipients (id_citation, id_parent, id_student, recipient_status)
        SELECT DISTINCT v_citation_id, ps.id_parent, e.id_student, 'pending'
        FROM enrollments e
        JOIN parent_students ps ON e.id_student = ps.id_student
        WHERE e.id_course_period = p_id_course_period AND e.status = 'active';
    END IF;
    
    -- Insert initial event
    INSERT INTO citation_events (id_citation, actor_type, actor_id, event_type, payload)
    VALUES (v_citation_id, 'user', p_id_teacher, 'created', JSON_OBJECT('scope', p_scope));
    
    SET o_id_citation = v_citation_id;
END //

DROP PROCEDURE IF EXISTS sp_get_teacher_citations //
CREATE PROCEDURE sp_get_teacher_citations(
    IN p_id_teacher INT
)
BEGIN
    SELECT 
        vc.id_citation AS id,
        vc.id_student AS studentId,
        (SELECT CONCAT(s.first_name, ' ', s.last_name) FROM students s WHERE s.id_student = vc.id_student) AS studentName,
        vc.id_course_period AS coursePeriodId,
        (
            SELECT CONCAT(c.name, ' - ', cp.section)
            FROM course_periods cp
            JOIN courses c ON cp.id_course = c.id_course
            WHERE cp.id_course_period = vc.id_course_period
        ) AS courseSectionName,
        vc.title AS title,
        vc.detail AS detail,
        vc.scheduled_at AS scheduledAt,
        vc.mode AS mode,
        vc.meeting_url AS meetingUrl,
        vc.scope AS scope,
        vc.global_status AS globalStatus,
        (
            SELECT COUNT(*) 
            FROM citation_recipients cr 
            WHERE cr.id_citation = vc.id_citation
        ) AS totalRecipients,
        (
            SELECT COUNT(*) 
            FROM citation_recipients cr 
            WHERE cr.id_citation = vc.id_citation AND cr.recipient_status = 'accepted'
        ) AS acceptedRecipients,
        (
            SELECT COUNT(*) 
            FROM citation_recipients cr 
            WHERE cr.id_citation = vc.id_citation AND cr.recipient_status = 'confirmed'
        ) AS confirmedRecipients,
        (
            SELECT COUNT(*) 
            FROM citation_messages cm 
            WHERE cm.id_citation = vc.id_citation 
              AND cm.sender_type = 'parent'
              AND cm.read_at IS NULL
        ) AS unreadMessages
    FROM virtual_citations vc
    WHERE vc.id_teacher = p_id_teacher
    ORDER BY vc.scheduled_at DESC;
END //

DROP PROCEDURE IF EXISTS sp_cancel_virtual_citation //
CREATE PROCEDURE sp_cancel_virtual_citation(
    IN p_id_citation INT,
    IN p_id_teacher INT
)
BEGIN
    DECLARE v_global_status VARCHAR(20);
    
    SELECT global_status INTO v_global_status
    FROM virtual_citations
    WHERE id_citation = p_id_citation AND id_teacher = p_id_teacher;
    
    IF v_global_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La citación no existe o no tiene permisos sobre ella.';
    ELSE
        UPDATE virtual_citations
        SET global_status = 'cancelled'
        WHERE id_citation = p_id_citation;
        
        UPDATE citation_recipients
        SET recipient_status = 'cancelled'
        WHERE id_citation = p_id_citation;
        
        INSERT INTO citation_events (id_citation, actor_type, actor_id, event_type, payload)
        VALUES (p_id_citation, 'user', p_id_teacher, 'cancelled', JSON_OBJECT('previousStatus', v_global_status, 'newStatus', 'cancelled'));
    END IF;
END //

DELIMITER ;
