-- =========================================================================
-- MIGRACIÓN FLYWAY V16: TABLAS Y PROCEDIMIENTOS ALMACENADOS PARA CITACIONES PMV3
-- =========================================================================

-- 1. Eliminar tabla citations obsoleta
DROP TABLE IF EXISTS citations;

-- 2. Crear tablas para módulo de Citaciones Virtuales PMV3
CREATE TABLE IF NOT EXISTS virtual_citations (
    id_citation INT AUTO_INCREMENT PRIMARY KEY,
    id_student INT NULL,
    id_course_period INT NULL,
    id_teacher INT NULL,
    created_by_user_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    detail TEXT,
    scheduled_at DATETIME NOT NULL,
    mode VARCHAR(20) NOT NULL, -- 'virtual', 'in_person'
    meeting_url VARCHAR(500) NULL,
    scope VARCHAR(20) NOT NULL, -- 'individual', 'section', 'mass'
    global_status VARCHAR(20) NOT NULL DEFAULT 'scheduled', -- 'scheduled', 'cancelled', 'completed'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_student) REFERENCES students(id_student) ON DELETE CASCADE,
    FOREIGN KEY (id_course_period) REFERENCES course_periods(id_course_period) ON DELETE CASCADE,
    FOREIGN KEY (id_teacher) REFERENCES users(id_user) ON DELETE SET NULL,
    FOREIGN KEY (created_by_user_id) REFERENCES users(id_user) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS citation_recipients (
    id_recipient INT AUTO_INCREMENT PRIMARY KEY,
    id_citation INT NOT NULL,
    id_parent INT NOT NULL,
    id_student INT NOT NULL,
    recipient_status VARCHAR(20) NOT NULL DEFAULT 'pending', -- 'pending', 'accepted', 'rejected', 'confirmed', 'cancelled'
    response_reason TEXT NULL,
    read_at DATETIME NULL,
    responded_at DATETIME NULL,
    confirmed_at DATETIME NULL,
    UNIQUE KEY uq_citation_parent_student (id_citation, id_parent, id_student),
    FOREIGN KEY (id_citation) REFERENCES virtual_citations(id_citation) ON DELETE CASCADE,
    FOREIGN KEY (id_parent) REFERENCES parents(id_parent) ON DELETE CASCADE,
    FOREIGN KEY (id_student) REFERENCES students(id_student) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS citation_messages (
    id_message INT AUTO_INCREMENT PRIMARY KEY,
    id_citation INT NOT NULL,
    sender_type VARCHAR(20) NOT NULL, -- 'parent', 'user', 'system'
    sender_id INT NOT NULL,
    body TEXT NOT NULL,
    sent_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at DATETIME NULL,
    FOREIGN KEY (id_citation) REFERENCES virtual_citations(id_citation) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS citation_events (
    id_event INT AUTO_INCREMENT PRIMARY KEY,
    id_citation INT NOT NULL,
    actor_type VARCHAR(20) NOT NULL, -- 'parent', 'user', 'system'
    actor_id INT NOT NULL,
    event_type VARCHAR(40) NOT NULL, -- 'created', 'sent', 'read', 'accepted', 'rejected', 'confirmed', 'message_sent', 'cancelled'
    payload JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_citation) REFERENCES virtual_citations(id_citation) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS teacher_availability_slots (
    id_slot INT AUTO_INCREMENT PRIMARY KEY,
    id_teacher INT NOT NULL,
    starts_at DATETIME NOT NULL,
    ends_at DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'available', -- 'available', 'reserved', 'blocked'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_teacher) REFERENCES users(id_user) ON DELETE CASCADE
);

-- 3. Crear Procedimientos Almacenados
DELIMITER //

DROP PROCEDURE IF EXISTS sp_get_parent_student_citations //
CREATE PROCEDURE sp_get_parent_student_citations(
    IN p_id_parent INT,
    IN p_id_student INT,
    IN p_status VARCHAR(20),
    IN p_from DATE,
    IN p_to DATE
)
BEGIN
    SELECT 
        vc.id_citation AS id,
        vc.title AS title,
        vc.detail AS detail,
        CONCAT(u.first_name, ' ', u.last_name) AS teacherName,
        vc.scheduled_at AS scheduledAt,
        cr.recipient_status AS status,
        vc.mode AS mode,
        vc.meeting_url AS meetingUrl,
        vc.scope AS scope,
        (
            SELECT COUNT(*) 
            FROM citation_messages cm 
            WHERE cm.id_citation = vc.id_citation 
              AND cm.sender_type <> 'parent'
              AND cm.read_at IS NULL
        ) AS unreadMessages
    FROM citation_recipients cr
    JOIN virtual_citations vc ON cr.id_citation = vc.id_citation
    LEFT JOIN users u ON vc.id_teacher = u.id_user
    WHERE cr.id_parent = p_id_parent 
      AND cr.id_student = p_id_student
      AND (p_status IS NULL OR p_status = '' OR cr.recipient_status = p_status)
      AND (p_from IS NULL OR CAST(vc.scheduled_at AS DATE) >= p_from)
      AND (p_to IS NULL OR CAST(vc.scheduled_at AS DATE) <= p_to)
    ORDER BY vc.scheduled_at DESC;
END //

DROP PROCEDURE IF EXISTS sp_get_citation_detail //
CREATE PROCEDURE sp_get_citation_detail(
    IN p_id_citation INT,
    IN p_id_parent INT
)
BEGIN
    SELECT 
        vc.id_citation AS id,
        vc.title AS title,
        vc.detail AS detail,
        CONCAT(u.first_name, ' ', u.last_name) AS teacherName,
        vc.scheduled_at AS scheduledAt,
        cr.recipient_status AS status,
        vc.mode AS mode,
        vc.meeting_url AS meetingUrl,
        vc.global_status AS globalStatus,
        vc.created_at AS createdAt,
        vc.updated_at AS updatedAt
    FROM virtual_citations vc
    LEFT JOIN users u ON vc.id_teacher = u.id_user
    LEFT JOIN citation_recipients cr ON vc.id_citation = cr.id_citation AND cr.id_parent = p_id_parent
    WHERE vc.id_citation = p_id_citation;
END //

DROP PROCEDURE IF EXISTS sp_respond_to_citation //
CREATE PROCEDURE sp_respond_to_citation(
    IN p_id_citation INT,
    IN p_id_parent INT,
    IN p_status VARCHAR(20),
    IN p_reason TEXT
)
BEGIN
    DECLARE v_global_status VARCHAR(20);
    DECLARE v_scheduled_at DATETIME;
    DECLARE v_recipient_status VARCHAR(20);
    DECLARE v_event_id INT DEFAULT NULL;
    
    -- Check citation existence and status
    SELECT global_status, scheduled_at 
    INTO v_global_status, v_scheduled_at
    FROM virtual_citations
    WHERE id_citation = p_id_citation;
    
    IF v_global_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La citación no existe.';
    ELSEIF v_global_status = 'cancelled' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La citación ha sido cancelada.';
    ELSEIF v_scheduled_at < NOW() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La citación ya ha vencido.';
    ELSE
        -- Check recipient status
        SELECT recipient_status INTO v_recipient_status
        FROM citation_recipients
        WHERE id_citation = p_id_citation AND id_parent = p_id_parent;
        
        IF v_recipient_status IS NULL THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El padre no pertenece a esta citación.';
        ELSE
            -- Update status
            UPDATE citation_recipients
            SET recipient_status = p_status,
                response_reason = p_reason,
                responded_at = NOW()
            WHERE id_citation = p_id_citation AND id_parent = p_id_parent;
            
            -- Insert event
            INSERT INTO citation_events (id_citation, actor_type, actor_id, event_type, payload)
            VALUES (p_id_citation, 'parent', p_id_parent, p_status, 
                    JSON_OBJECT('previousStatus', v_recipient_status, 'newStatus', p_status, 'reason', p_reason));
            
            SET v_event_id = LAST_INSERT_ID();
            
            -- Return the result
            SELECT 
                vc.id_citation AS id,
                vc.title AS title,
                vc.detail AS detail,
                CONCAT(u.first_name, ' ', u.last_name) AS teacherName,
                vc.scheduled_at AS scheduledAt,
                cr.recipient_status AS status,
                vc.mode AS mode,
                vc.meeting_url AS meetingUrl,
                v_event_id AS eventId
            FROM virtual_citations vc
            LEFT JOIN users u ON vc.id_teacher = u.id_user
            JOIN citation_recipients cr ON vc.id_citation = cr.id_citation
            WHERE vc.id_citation = p_id_citation AND cr.id_parent = p_id_parent;
        END IF;
    END IF;
END //

DROP PROCEDURE IF EXISTS sp_confirm_citation //
CREATE PROCEDURE sp_confirm_citation(
    IN p_id_citation INT,
    IN p_id_parent INT
)
BEGIN
    DECLARE v_recipient_status VARCHAR(20);
    DECLARE v_global_status VARCHAR(20);
    
    SELECT recipient_status INTO v_recipient_status
    FROM citation_recipients
    WHERE id_citation = p_id_citation AND id_parent = p_id_parent;
    
    SELECT global_status INTO v_global_status
    FROM virtual_citations
    WHERE id_citation = p_id_citation;
    
    IF v_recipient_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El padre no pertenece a esta citación.';
    ELSEIF v_global_status = 'cancelled' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La citación ha sido cancelada.';
    ELSE
        -- Update status
        UPDATE citation_recipients
        SET recipient_status = 'confirmed',
            confirmed_at = NOW()
        WHERE id_citation = p_id_citation AND id_parent = p_id_parent;
        
        -- Insert event
        INSERT INTO citation_events (id_citation, actor_type, actor_id, event_type, payload)
        VALUES (p_id_citation, 'parent', p_id_parent, 'confirmed', 
                JSON_OBJECT('previousStatus', v_recipient_status, 'newStatus', 'confirmed'));
                
        -- Return result
        SELECT 
            vc.id_citation AS id,
            vc.title AS title,
            vc.detail AS detail,
            CONCAT(u.first_name, ' ', u.last_name) AS teacherName,
            vc.scheduled_at AS scheduledAt,
            cr.recipient_status AS status,
            vc.mode AS mode,
            vc.meeting_url AS meetingUrl,
            cr.confirmed_at AS confirmedAt
        FROM virtual_citations vc
        LEFT JOIN users u ON vc.id_teacher = u.id_user
        JOIN citation_recipients cr ON vc.id_citation = cr.id_citation
        WHERE vc.id_citation = p_id_citation AND cr.id_parent = p_id_parent;
    END IF;
END //

DROP PROCEDURE IF EXISTS sp_get_citation_messages //
CREATE PROCEDURE sp_get_citation_messages(
    IN p_id_citation INT,
    IN p_id_parent INT,
    IN p_after DATETIME
)
BEGIN
    -- Mark messages as read
    UPDATE citation_messages
    SET read_at = NOW()
    WHERE id_citation = p_id_citation
      AND sender_type <> 'parent'
      AND read_at IS NULL;
      
    -- Get messages
    SELECT 
        cm.id_message AS id,
        cm.id_citation AS citationId,
        CASE cm.sender_type
            WHEN 'parent' THEN (SELECT CONCAT(first_name, ' ', last_name) FROM parents WHERE id_parent = cm.sender_id)
            WHEN 'user' THEN (SELECT CONCAT(first_name, ' ', last_name) FROM users WHERE id_user = cm.sender_id)
            ELSE 'Sistema'
        END AS senderName,
        CASE cm.sender_type
            WHEN 'parent' THEN 'Padre'
            WHEN 'user' THEN 'Docente'
            ELSE 'Sistema'
        END AS senderRole,
        cm.body AS body,
        cm.sent_at AS sentAt,
        (cm.sender_type = 'parent' AND cm.sender_id = p_id_parent) AS isFromParent,
        (cm.read_at IS NOT NULL) AS isRead
    FROM citation_messages cm
    WHERE cm.id_citation = p_id_citation
      AND (p_after IS NULL OR cm.sent_at > p_after)
    ORDER BY cm.sent_at ASC;
END //

DROP PROCEDURE IF EXISTS sp_send_citation_message //
CREATE PROCEDURE sp_send_citation_message(
    IN p_id_citation INT,
    IN p_sender_type VARCHAR(20),
    IN p_sender_id INT,
    IN p_body TEXT
)
BEGIN
    DECLARE v_global_status VARCHAR(20);
    DECLARE v_message_id INT;
    
    SELECT global_status INTO v_global_status
    FROM virtual_citations
    WHERE id_citation = p_id_citation;
    
    IF v_global_status = 'cancelled' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La citación está cancelada. No se pueden enviar mensajes.';
    ELSE
        INSERT INTO citation_messages (id_citation, sender_type, sender_id, body, sent_at)
        VALUES (p_id_citation, p_sender_type, p_sender_id, p_body, NOW());
        
        SET v_message_id = LAST_INSERT_ID();
        
        -- Event logging
        INSERT INTO citation_events (id_citation, actor_type, actor_id, event_type, payload)
        VALUES (p_id_citation, p_sender_type, p_sender_id, 'message_sent', 
                JSON_OBJECT('messageId', v_message_id));
                
        -- Return details
        SELECT 
            cm.id_message AS id,
            cm.id_citation AS citationId,
            CASE cm.sender_type
                WHEN 'parent' THEN (SELECT CONCAT(first_name, ' ', last_name) FROM parents WHERE id_parent = cm.sender_id)
                WHEN 'user' THEN (SELECT CONCAT(first_name, ' ', last_name) FROM users WHERE id_user = cm.sender_id)
                ELSE 'Sistema'
            END AS senderName,
            CASE cm.sender_type
                WHEN 'parent' THEN 'Padre'
                WHEN 'user' THEN 'Docente'
                ELSE 'Sistema'
            END AS senderRole,
            cm.body AS body,
            cm.sent_at AS sentAt,
            (cm.sender_type = 'parent') AS isFromParent,
            (cm.read_at IS NOT NULL) AS isRead
        FROM citation_messages cm
        WHERE cm.id_message = v_message_id;
    END IF;
END //

DROP PROCEDURE IF EXISTS sp_get_citation_events //
CREATE PROCEDURE sp_get_citation_events(
    IN p_id_citation INT
)
BEGIN
    SELECT 
        ce.id_event AS id,
        ce.event_type AS eventType,
        CASE ce.actor_type
            WHEN 'parent' THEN (SELECT CONCAT(first_name, ' ', last_name) FROM parents WHERE id_parent = ce.actor_id)
            WHEN 'user' THEN (SELECT CONCAT(first_name, ' ', last_name) FROM users WHERE id_user = ce.actor_id)
            ELSE 'Sistema'
        END AS actorName,
        CASE ce.actor_type
            WHEN 'parent' THEN 'Padre'
            WHEN 'user' THEN 'Docente'
            ELSE 'Sistema'
        END AS actorRole,
        ce.created_at AS createdAt,
        ce.payload AS payload
    FROM citation_events ce
    WHERE ce.id_citation = p_id_citation
    ORDER BY ce.created_at ASC;
END //

DROP PROCEDURE IF EXISTS sp_get_student_reminders //
CREATE PROCEDURE sp_get_student_reminders(
    IN p_id_student INT,
    IN p_id_parent INT,
    IN p_today DATE
)
BEGIN
    SELECT * FROM (
        SELECT
            a.id_activity AS id,
            'activityDue' AS type,
            a.name AS title,
            CONCAT(c.name, ' vence el ', DATE_FORMAT(a.due_date, '%Y-%m-%d'), ' (', COALESCE(a.term, 'B1'), ').') AS detail,
            a.due_date AS date,
            'warning' AS severity
        FROM enrollments e
        JOIN course_periods cp ON e.id_course_period = cp.id_course_period
        JOIN courses c ON cp.id_course = c.id_course
        JOIN activities a ON cp.id_course_period = a.id_course_period
        LEFT JOIN grades g ON a.id_activity = g.id_activity AND g.id_student = p_id_student
        WHERE e.id_student = p_id_student
          AND g.value IS NULL
          AND a.due_date >= p_today
          AND a.due_date <= DATE_ADD(p_today, INTERVAL 4 DAY)
        
        UNION ALL
        
        SELECT
            vc.id_citation AS id,
            'citation' AS type,
            vc.title AS title,
            CONCAT('Programada para el ', DATE_FORMAT(vc.scheduled_at, '%Y-%m-%d %H:%i'), '.') AS detail,
            CAST(vc.scheduled_at AS DATE) AS date,
            'info' AS severity
        FROM citation_recipients cr
        JOIN virtual_citations vc ON cr.id_citation = vc.id_citation
        WHERE cr.id_student = p_id_student
          AND cr.id_parent = p_id_parent
          AND cr.recipient_status NOT IN ('rejected', 'cancelled')
          AND vc.global_status = 'scheduled'
          AND vc.scheduled_at >= DATE_ADD(p_today, INTERVAL 7 DAY)
          AND vc.scheduled_at <= DATE_ADD(p_today, INTERVAL 11 DAY)
    ) AS results
    ORDER BY date ASC;
END //

DELIMITER ;

-- 4. Semilla de datos para pruebas
-- Citación individual pendiente
INSERT INTO virtual_citations (id_student, id_teacher, created_by_user_id, title, detail, scheduled_at, mode, meeting_url, scope)
VALUES (1, 1, 1, 'Citacion virtual con tutoria', 'Revision de avance academico y acuerdos de apoyo en casa.', DATE_ADD(NOW(), INTERVAL 9 DAY), 'virtual', 'https://meet.learnix.edu/cita-101', 'individual');

SET @cit_id = LAST_INSERT_ID();

INSERT INTO citation_recipients (id_citation, id_parent, id_student, recipient_status)
VALUES (@cit_id, 1, 1, 'pending');

INSERT INTO citation_messages (id_citation, sender_type, sender_id, body, sent_at, read_at)
VALUES (@cit_id, 'user', 1, 'Buenas tardes, solicito una reunion para revisar el avance.', DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL);

INSERT INTO citation_events (id_citation, actor_type, actor_id, event_type, payload)
VALUES (@cit_id, 'user', 1, 'created', JSON_OBJECT('newStatus', 'pending'));
