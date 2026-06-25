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
        Map<String, Object> inParams = Map.of(
                "p_id_citation", citationId,
                "p_id_parent", 0
        );
        List<CitationDto> list = executeAndConvertList(getCitationDetailCall, CitationDto.class, inParams, "citationDetail");
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Map<String, Object>> getCitationRecipients(Integer citationId) {
        String sql = "SELECT cr.id_recipient as idRecipient, cr.recipient_status as status, cr.response_reason as reason, " +
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

    public List<CitationMessageDto> getCitationMessages(Integer citationId, String after) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_id_citation", citationId);
        inParams.put("p_id_parent", 0);
        inParams.put("p_after", after != null ? Timestamp.valueOf(LocalDateTime.parse(after)) : null);
        return executeAndConvertList(getCitationMessagesCall, CitationMessageDto.class, inParams, "messagesList");
    }

    public CitationMessageDto sendTeacherMessage(Integer citationId, String body, Integer teacherId) {
        Map<String, Object> inParams = Map.of(
                "p_id_citation", citationId,
                "p_sender_type", "user",
                "p_sender_id", teacherId,
                "p_body", body
        );
        List<CitationMessageDto> list = executeAndConvertList(sendCitationMessageCall, CitationMessageDto.class, inParams, "sendMessageResult");
        return list.isEmpty() ? null : list.get(0);
    }

    public List<CitationEventDto> getCitationEvents(Integer citationId) {
        return executeAndConvertList(getCitationEventsCall, CitationEventDto.class, "p_id_citation", citationId, "eventsList");
    }
}
