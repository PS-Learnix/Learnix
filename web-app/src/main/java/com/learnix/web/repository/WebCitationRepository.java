package com.learnix.web.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnix.web.dto.MobileDtos.*;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class WebCitationRepository extends BaseProcedureRepository {

    private SimpleJdbcCall createVirtualCitationCall;
    private SimpleJdbcCall getTeacherCitationsCall;
    private SimpleJdbcCall cancelVirtualCitationCall;
    private SimpleJdbcCall getCitationDetailCall;
    private SimpleJdbcCall getCitationMessagesCall;
    private SimpleJdbcCall sendCitationMessageCall;
    private SimpleJdbcCall getCitationEventsCall;

    public WebCitationRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        super(jdbcTemplate, objectMapper);
    }

    @PostConstruct
    public void init() {
        this.createVirtualCitationCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_create_virtual_citation");

        this.getTeacherCitationsCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_teacher_citations")
                .returningResultSet("citationsList", (rs, rowNum) -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getInt("id"));
                    map.put("studentId", rs.getObject("studentId"));
                    map.put("studentName", rs.getString("studentName"));
                    map.put("coursePeriodId", rs.getObject("coursePeriodId"));
                    map.put("courseSectionName", rs.getString("courseSectionName"));
                    map.put("title", rs.getString("title"));
                    map.put("detail", rs.getString("detail"));
                    map.put("scheduledAt", rs.getTimestamp("scheduledAt") != null ? rs.getTimestamp("scheduledAt").toInstant().toString().substring(0, 19) : null);
                    map.put("mode", rs.getString("mode"));
                    map.put("meetingUrl", rs.getString("meetingUrl"));
                    map.put("scope", rs.getString("scope"));
                    map.put("globalStatus", rs.getString("globalStatus"));
                    map.put("totalRecipients", rs.getInt("totalRecipients"));
                    map.put("acceptedRecipients", rs.getInt("acceptedRecipients"));
                    map.put("confirmedRecipients", rs.getInt("confirmedRecipients"));
                    map.put("unreadMessages", rs.getInt("unreadMessages"));
                    return map;
                });

        this.cancelVirtualCitationCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_cancel_virtual_citation");

        this.getCitationDetailCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_citation_detail")
                .returningResultSet("citationDetail", (rs, rowNum) -> new CitationDto(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("detail"),
                        rs.getString("teacherName"),
                        rs.getTimestamp("scheduledAt") != null ? rs.getTimestamp("scheduledAt").toInstant().toString().substring(0, 19) : null,
                        rs.getString("status"),
                        rs.getString("mode"),
                        rs.getString("meetingUrl"),
                        null,
                        null
                ));

        this.getCitationMessagesCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_citation_messages")
                .returningResultSet("messagesList", (rs, rowNum) -> new CitationMessageDto(
                        rs.getInt("id"),
                        rs.getInt("citationId"),
                        rs.getString("senderName"),
                        rs.getString("senderRole"),
                        rs.getString("body"),
                        rs.getTimestamp("sentAt") != null ? rs.getTimestamp("sentAt").toInstant().toString().substring(0, 19) : null,
                        rs.getBoolean("isFromParent"),
                        rs.getBoolean("isRead")
                ));

        this.sendCitationMessageCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_send_citation_message")
                .returningResultSet("sendMessageResult", (rs, rowNum) -> new CitationMessageDto(
                        rs.getInt("id"),
                        rs.getInt("citationId"),
                        rs.getString("senderName"),
                        rs.getString("senderRole"),
                        rs.getString("body"),
                        rs.getTimestamp("sentAt") != null ? rs.getTimestamp("sentAt").toInstant().toString().substring(0, 19) : null,
                        rs.getBoolean("isFromParent"),
                        rs.getBoolean("isRead")
                ));

        this.getCitationEventsCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_citation_events")
                .returningResultSet("eventsList", (rs, rowNum) -> new CitationEventDto(
                        rs.getInt("id"),
                        rs.getString("eventType"),
                        rs.getString("actorName"),
                        rs.getString("actorRole"),
                        rs.getTimestamp("createdAt") != null ? rs.getTimestamp("createdAt").toInstant().toString().substring(0, 19) : null,
                        rs.getString("payload")
                ));
    }

    public Integer createCitation(Integer studentId, Integer coursePeriodId, Integer teacherId,
                                  String title, String detail, String scheduledAt,
                                  String mode, String meetingUrl, String scope) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_id_student", studentId);
        inParams.put("p_id_course_period", coursePeriodId);
        inParams.put("p_id_teacher", teacherId);
        inParams.put("p_title", title);
        inParams.put("p_detail", detail);
        
        // Parse date formatted as 'YYYY-MM-DDTHH:mm'
        LocalDateTime ldt = LocalDateTime.parse(scheduledAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        inParams.put("p_scheduled_at", Timestamp.valueOf(ldt));
        inParams.put("p_mode", mode);
        inParams.put("p_meeting_url", meetingUrl);
        inParams.put("p_scope", scope);

        Map<String, Object> out = createVirtualCitationCall.execute(inParams);
        return (Integer) out.get("o_id_citation");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Map<String, Object>> getTeacherCitations(Integer teacherId) {
        List list = executeAndConvertList(getTeacherCitationsCall, Map.class, "p_id_teacher", teacherId, "citationsList");
        return (List<Map<String, Object>>) list;
    }

    public void cancelCitation(Integer citationId, Integer teacherId) {
        Map<String, Object> inParams = Map.of(
                "p_id_citation", citationId,
                "p_id_teacher", teacherId
        );
        cancelVirtualCitationCall.execute(inParams);
    }

    public CitationDto getCitationDetail(Integer citationId, Integer teacherId) {
        String sql = """
                SELECT
                    vc.id_citation AS id,
                    vc.title AS title,
                    vc.detail AS detail,
                    CONCAT(u.first_name, ' ', u.last_name) AS teacherName,
                    vc.scheduled_at AS scheduledAt,
                    vc.global_status AS status,
                    vc.mode AS mode,
                    vc.meeting_url AS meetingUrl
                FROM virtual_citations vc
                LEFT JOIN users u ON vc.id_teacher = u.id_user
                WHERE vc.id_citation = ? AND vc.id_teacher = ?
                """;
        List<CitationDto> list = jdbcTemplate.query(sql, (rs, rowNum) -> new CitationDto(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("detail"),
                rs.getString("teacherName"),
                rs.getTimestamp("scheduledAt") != null ? rs.getTimestamp("scheduledAt").toInstant().toString().substring(0, 19) : null,
                rs.getString("status"),
                rs.getString("mode"),
                rs.getString("meetingUrl"),
                null,
                null
        ), citationId, teacherId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Map<String, Object>> getCitationRecipients(Integer citationId) {
        String sql = "SELECT cr.id_recipient as idRecipient, cr.recipient_status as status, " +
                "cr.id_parent as parentId, " +
                "CONCAT(p.first_name, ' ', p.last_name) as parentName, " +
                "CONCAT(s.first_name, ' ', s.last_name) as studentName, " +
                "cr.read_at as readAt, cr.responded_at as respondedAt, cr.confirmed_at as confirmedAt " +
                "FROM citation_recipients cr " +
                "JOIN parents p ON cr.id_parent = p.id_parent " +
                "JOIN students s ON cr.id_student = s.id_student " +
                "WHERE cr.id_citation = ? " +
                "ORDER BY s.last_name, s.first_name";
        return jdbcTemplate.queryForList(sql, citationId);
    }

    public Map<String, Object> getCitationResponseSummary(Integer citationId) {
        String sql = """
                SELECT
                    COUNT(CASE WHEN recipient_status IN ('accepted', 'confirmed') THEN 1 END) AS acceptedCount,
                    COUNT(CASE WHEN recipient_status = 'rejected' THEN 1 END) AS rejectedCount,
                    COUNT(CASE WHEN recipient_status = 'pending' THEN 1 END) AS pendingCount,
                    COUNT(*) AS totalCount
                FROM citation_recipients
                WHERE id_citation = ?
                """;
        return jdbcTemplate.queryForMap(sql, citationId);
    }

    public List<Map<String, Object>> getCommunicationContacts(Integer citationId) {
        String sql = """
                SELECT
                    cr.id_recipient AS idRecipient,
                    cr.id_parent AS parentId,
                    cr.id_student AS studentId,
                    CONCAT(p.first_name, ' ', p.last_name) AS parentName,
                    CONCAT(s.first_name, ' ', s.last_name) AS studentName,
                    cr.recipient_status AS status,
                    (
                        SELECT cm.body
                        FROM citation_messages cm
                        WHERE cm.id_citation = cr.id_citation
                          AND (
                              (cm.sender_type = 'parent' AND cm.sender_id = cr.id_parent)
                              OR (cm.sender_type <> 'parent' AND (cm.target_id_parent IS NULL OR cm.target_id_parent = cr.id_parent))
                          )
                        ORDER BY cm.sent_at DESC
                        LIMIT 1
                    ) AS lastMessage,
                    (
                        SELECT COUNT(*)
                        FROM citation_messages cm
                        WHERE cm.id_citation = cr.id_citation
                          AND cm.sender_type = 'parent'
                          AND cm.sender_id = cr.id_parent
                          AND cm.read_at IS NULL
                    ) AS unreadMessages
                FROM citation_recipients cr
                JOIN parents p ON cr.id_parent = p.id_parent
                JOIN students s ON cr.id_student = s.id_student
                WHERE cr.id_citation = ?
                ORDER BY unreadMessages DESC, s.last_name, s.first_name
                """;
        return jdbcTemplate.queryForList(sql, citationId);
    }

    public List<CitationMessageDto> getCitationMessages(Integer citationId, Integer parentId, String after) {
        Timestamp afterTimestamp = after != null ? Timestamp.valueOf(LocalDateTime.parse(after)) : null;
        jdbcTemplate.update(
                "UPDATE citation_messages SET read_at = NOW() WHERE id_citation = ? AND sender_type = 'parent' AND sender_id = ? AND read_at IS NULL",
                citationId, parentId
        );
        String sql = """
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
                WHERE cm.id_citation = ?
                  AND (
                      (cm.sender_type = 'parent' AND cm.sender_id = ?)
                      OR (cm.sender_type <> 'parent' AND (cm.target_id_parent IS NULL OR cm.target_id_parent = ?))
                  )
                  AND (? IS NULL OR cm.sent_at > ?)
                ORDER BY cm.sent_at ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new CitationMessageDto(
                rs.getInt("id"),
                rs.getInt("citationId"),
                rs.getString("senderName"),
                rs.getString("senderRole"),
                rs.getString("body"),
                rs.getTimestamp("sentAt") != null ? rs.getTimestamp("sentAt").toInstant().toString().substring(0, 19) : null,
                rs.getBoolean("isFromParent"),
                rs.getBoolean("isRead")
        ), citationId, parentId, parentId, afterTimestamp, afterTimestamp);
    }

    public CitationMessageDto sendTeacherMessage(Integer citationId, String body, Integer teacherId, Integer parentId) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_id_citation", citationId);
        inParams.put("p_sender_type", "user");
        inParams.put("p_sender_id", teacherId);
        inParams.put("p_body", body);
        inParams.put("p_target_id_parent", parentId);
        List<CitationMessageDto> list = executeAndConvertList(sendCitationMessageCall, CitationMessageDto.class, inParams, "sendMessageResult");
        return list.isEmpty() ? null : list.get(0);
    }

    public List<CitationEventDto> getCitationEvents(Integer citationId) {
        return executeAndConvertList(getCitationEventsCall, CitationEventDto.class, "p_id_citation", citationId, "eventsList");
    }
}
