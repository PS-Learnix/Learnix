-- =========================================================================
-- FLYWAY V19: JUSTIFICACIONES Y MENSAJERIA DIRIGIDA EN CITACIONES PMV3
-- =========================================================================

ALTER TABLE citation_recipients
    ADD COLUMN justification_status VARCHAR(20) NULL,
    ADD COLUMN justification_reviewed_at DATETIME NULL;

ALTER TABLE citation_messages
    ADD COLUMN target_id_parent INT NULL,
    ADD CONSTRAINT fk_citation_messages_target_parent
        FOREIGN KEY (target_id_parent) REFERENCES parents(id_parent) ON DELETE CASCADE;

DELIMITER //

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

    SELECT global_status, scheduled_at
    INTO v_global_status, v_scheduled_at
    FROM virtual_citations
    WHERE id_citation = p_id_citation;

    IF v_global_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La citacion no existe.';
    ELSEIF v_global_status = 'cancelled' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La citacion ha sido cancelada.';
    ELSEIF v_scheduled_at < NOW() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La citacion ya ha vencido.';
    ELSEIF p_status NOT IN ('accepted', 'rejected') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El estado de respuesta no es valido.';
    ELSEIF p_status = 'rejected' AND (p_reason IS NULL OR TRIM(p_reason) = '') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Debe registrar una razon para rechazar la citacion.';
    ELSE
        SELECT recipient_status INTO v_recipient_status
        FROM citation_recipients
        WHERE id_citation = p_id_citation AND id_parent = p_id_parent;

        IF v_recipient_status IS NULL THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El padre no pertenece a esta citacion.';
        ELSE
            UPDATE citation_recipients
            SET recipient_status = p_status,
                response_reason = CASE WHEN p_status = 'rejected' THEN TRIM(p_reason) ELSE NULL END,
                justification_status = CASE WHEN p_status = 'rejected' THEN 'pending_review' ELSE NULL END,
                justification_reviewed_at = NULL,
                responded_at = NOW()
            WHERE id_citation = p_id_citation AND id_parent = p_id_parent;

            INSERT INTO citation_events (id_citation, actor_type, actor_id, event_type, payload)
            VALUES (
                p_id_citation,
                'parent',
                p_id_parent,
                p_status,
                JSON_OBJECT('previousStatus', v_recipient_status, 'newStatus', p_status, 'reason', p_reason)
            );

            SET v_event_id = LAST_INSERT_ID();

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

DROP PROCEDURE IF EXISTS sp_get_citation_messages //
CREATE PROCEDURE sp_get_citation_messages(
    IN p_id_citation INT,
    IN p_id_parent INT,
    IN p_after DATETIME
)
BEGIN
    UPDATE citation_messages
    SET read_at = NOW()
    WHERE id_citation = p_id_citation
      AND sender_type <> 'parent'
      AND (target_id_parent IS NULL OR target_id_parent = p_id_parent)
      AND read_at IS NULL;

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
      AND (
          (cm.sender_type = 'parent' AND cm.sender_id = p_id_parent)
          OR (cm.sender_type <> 'parent' AND (cm.target_id_parent IS NULL OR cm.target_id_parent = p_id_parent))
      )
      AND (p_after IS NULL OR cm.sent_at > p_after)
    ORDER BY cm.sent_at ASC;
END //

DROP PROCEDURE IF EXISTS sp_send_citation_message //
CREATE PROCEDURE sp_send_citation_message(
    IN p_id_citation INT,
    IN p_sender_type VARCHAR(20),
    IN p_sender_id INT,
    IN p_body TEXT,
    IN p_target_id_parent INT
)
BEGIN
    DECLARE v_global_status VARCHAR(20);
    DECLARE v_message_id INT;

    SELECT global_status INTO v_global_status
    FROM virtual_citations
    WHERE id_citation = p_id_citation;

    IF v_global_status = 'cancelled' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La citacion esta cancelada. No se pueden enviar mensajes.';
    ELSEIF p_body IS NULL OR TRIM(p_body) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El cuerpo del mensaje no puede estar vacio.';
    ELSE
        INSERT INTO citation_messages (id_citation, sender_type, sender_id, body, sent_at, target_id_parent)
        VALUES (p_id_citation, p_sender_type, p_sender_id, TRIM(p_body), NOW(), p_target_id_parent);

        SET v_message_id = LAST_INSERT_ID();

        INSERT INTO citation_events (id_citation, actor_type, actor_id, event_type, payload)
        VALUES (
            p_id_citation,
            p_sender_type,
            p_sender_id,
            'message_sent',
            JSON_OBJECT('messageId', v_message_id, 'targetParentId', p_target_id_parent)
        );

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

DELIMITER ;
