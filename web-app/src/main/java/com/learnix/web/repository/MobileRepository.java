package com.learnix.web.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnix.web.dto.MobileDtos.*;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MobileRepository extends BaseProcedureRepository {

    private SimpleJdbcCall authenticateParentCall;
    private SimpleJdbcCall getParentStudentsCall;
    private SimpleJdbcCall getStudentDetailMobileCall;
    private SimpleJdbcCall getStudentCourseAveragesCall;
    private SimpleJdbcCall getStudentActivitiesDashboardCall;
    private SimpleJdbcCall getStudentAttendanceStatsCall;
    private SimpleJdbcCall getStudentReportsDashboardCall;
    private SimpleJdbcCall getStudentTermProgressCall;
    private SimpleJdbcCall getStudentProgressActivitiesCall;
    private SimpleJdbcCall getStudentAttendanceDetailCall;
    private SimpleJdbcCall getStudentRemindersCall;
    private SimpleJdbcCall getStudentIncidentsCall;
    private SimpleJdbcCall getParentProfileSummaryCall;
    private SimpleJdbcCall getParentAnnouncementsCall;
    private SimpleJdbcCall markAnnouncementReadCall;
    private SimpleJdbcCall getStudentReportsCall;
    private SimpleJdbcCall updateParentPreferencesCall;
    private SimpleJdbcCall getParentStudentCitationsCall;
    private SimpleJdbcCall getCitationDetailCall;
    private SimpleJdbcCall respondToCitationCall;
    private SimpleJdbcCall confirmCitationCall;
    private SimpleJdbcCall getCitationMessagesCall;
    private SimpleJdbcCall sendCitationMessageCall;
    private SimpleJdbcCall getCitationEventsCall;

    public MobileRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        super(jdbcTemplate, objectMapper);
    }

    @PostConstruct
    public void init() {
        this.authenticateParentCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_authenticate_parent");

        this.getParentStudentsCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_parent_students")
                .returningResultSet("studentsList", (rs, rowNum) -> new StudentMobileDto(
                        rs.getInt("id_student"),
                        rs.getString("fullName"),
                        rs.getString("gradeSection"),
                        rs.getBoolean("isPrimary")
                ));

        this.getStudentDetailMobileCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_student_detail_mobile")
                .returningResultSet("studentDetail", new DataClassRowMapper<>(StudentDetailDto.class));

        this.getStudentCourseAveragesCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_student_course_averages")
                .returningResultSet("averagesList", new DataClassRowMapper<>(CourseAverageDto.class));

        this.getStudentActivitiesDashboardCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_student_activities_dashboard")
                .returningResultSet("activitiesList", new DataClassRowMapper<>(ActivityMobileDto.class));

        this.getStudentAttendanceStatsCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_student_attendance_stats")
                .returningResultSet("statsList", new DataClassRowMapper<>(AttendanceStatsDto.class));

        this.getStudentReportsDashboardCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_student_reports_dashboard")
                .returningResultSet("reportsList", (rs, rowNum) -> {
                    String formatsStr = rs.getString("formats");
                    List<String> formatsList = formatsStr != null ? List.of(formatsStr.split(",")) : List.of();
                    return new ReportDashboardDto(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            formatsList
                    );
                });

        this.getStudentTermProgressCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_student_term_progress")
                .returningResultSet("progressList", new DataClassRowMapper<>(TermProgressDto.class));

        this.getStudentProgressActivitiesCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_student_progress_activities")
                .returningResultSet("activitiesList", new DataClassRowMapper<>(ActivityProgressDto.class));

        this.getStudentAttendanceDetailCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_student_attendance_detail")
                .returningResultSet("daysList", new DataClassRowMapper<>(AttendanceDayDto.class));

        this.getStudentRemindersCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_student_reminders")
                .returningResultSet("remindersList", new DataClassRowMapper<>(ReminderDto.class));

        this.getStudentIncidentsCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_student_incidents")
                .returningResultSet("incidentsList", new DataClassRowMapper<>(IncidentDto.class));

        this.getParentProfileSummaryCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_parent_profile_summary")
                .returningResultSet("profileList", new DataClassRowMapper<>(ParentProfileDto.class));

        this.getParentAnnouncementsCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_parent_announcements")
                .returningResultSet("announcementsList", new DataClassRowMapper<>(AnnouncementDto.class));

        this.markAnnouncementReadCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_mark_announcement_read")
                .returningResultSet("readResult", (rs, rowNum) -> new ReadAnnouncementResponse(
                        rs.getInt("id"),
                        rs.getString("status"),
                        rs.getTimestamp("readAt") != null ? rs.getTimestamp("readAt").toInstant().toString() : null
                ));

        this.getStudentReportsCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_student_reports")
                .returningResultSet("reportsList", new DataClassRowMapper<>(FlatReportDto.class));

        this.updateParentPreferencesCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_update_parent_preferences")
                .returningResultSet("prefResult", (rs, rowNum) -> new PreferencesResponse(
                        rs.getInt("parentId"),
                        rs.getBoolean("darkMode"),
                        rs.getTimestamp("updatedAt") != null ? rs.getTimestamp("updatedAt").toInstant().toString() : null
                ));

        this.getParentStudentCitationsCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_parent_student_citations")
                .returningResultSet("citationsList", (rs, rowNum) -> new CitationDto(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("detail"),
                        rs.getString("teacherName"),
                        rs.getTimestamp("scheduledAt") != null ? rs.getTimestamp("scheduledAt").toInstant().toString().substring(0, 19) : null,
                        rs.getString("status"),
                        rs.getString("mode"),
                        rs.getString("meetingUrl"),
                        rs.getString("scope"),
                        rs.getInt("unreadMessages")
                ));

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

        this.respondToCitationCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_respond_to_citation")
                .returningResultSet("respondResult", (rs, rowNum) -> {
                    CitationDto citation = new CitationDto(
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
                    );
                    return new RespondCitationResponse(citation, rs.getInt("eventId"));
                });

        this.confirmCitationCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_confirm_citation")
                .returningResultSet("confirmResult", (rs, rowNum) -> {
                    CitationDto citation = new CitationDto(
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
                    );
                    return new ConfirmCitationResponse(citation, rs.getTimestamp("confirmedAt") != null ? rs.getTimestamp("confirmedAt").toInstant().toString().substring(0, 19) : null);
                });

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

    public ParentDto authenticateParent(String email, String password) {
        Map<String, Object> inParams = Map.of(
                "p_email", email.trim(),
                "p_password", password.trim()
        );
        Map<String, Object> out = authenticateParentCall.execute(inParams);
        if (out.get("o_id_parent") == null) {
            return null;
        }
        return new ParentDto(
                (Integer) out.get("o_id_parent"),
                (String) out.get("o_first_name"),
                (String) out.get("o_last_name"),
                (String) out.get("o_email")
        );
    }

    public List<StudentMobileDto> getParentStudents(Integer parentId) {
        return executeAndConvertList(getParentStudentsCall, StudentMobileDto.class, "p_id_parent", parentId, "studentsList");
    }

    public StudentDetailDto getStudentDetail(Integer studentId) {
        List<StudentDetailDto> list = executeAndConvertList(getStudentDetailMobileCall, StudentDetailDto.class, "p_id_student", studentId, "studentDetail");
        return list.isEmpty() ? null : list.get(0);
    }

    public List<CourseAverageDto> getStudentCourseAverages(Integer studentId) {
        return executeAndConvertList(getStudentCourseAveragesCall, CourseAverageDto.class, "p_id_student", studentId, "averagesList");
    }

    public List<ActivityMobileDto> getStudentActivitiesDashboard(Integer studentId) {
        return executeAndConvertList(getStudentActivitiesDashboardCall, ActivityMobileDto.class, "p_id_student", studentId, "activitiesList");
    }

    public AttendanceStatsDto getStudentAttendanceStats(Integer studentId) {
        List<AttendanceStatsDto> list = executeAndConvertList(getStudentAttendanceStatsCall, AttendanceStatsDto.class, "p_id_student", studentId, "statsList");
        return list.isEmpty() ? new AttendanceStatsDto(0L, 0L, 0L, 100.0) : list.get(0);
    }

    public List<ReportDashboardDto> getStudentReportsDashboard(Integer studentId) {
        return executeAndConvertList(getStudentReportsDashboardCall, ReportDashboardDto.class, "p_id_student", studentId, "reportsList");
    }

    public List<TermProgressDto> getStudentTermProgress(Integer studentId) {
        return executeAndConvertList(getStudentTermProgressCall, TermProgressDto.class, "p_id_student", studentId, "progressList");
    }

    public List<ActivityProgressDto> getStudentProgressActivities(Integer studentId, String term, Integer courseId) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_id_student", studentId);
        inParams.put("p_term", term);
        inParams.put("p_course_id", courseId);
        return executeAndConvertList(getStudentProgressActivitiesCall, ActivityProgressDto.class, inParams, "activitiesList");
    }

    public List<AttendanceDayDto> getStudentAttendanceDetail(Integer studentId, LocalDate from, LocalDate to) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_id_student", studentId);
        inParams.put("p_from", from != null ? Date.valueOf(from) : null);
        inParams.put("p_to", to != null ? Date.valueOf(to) : null);
        return executeAndConvertList(getStudentAttendanceDetailCall, AttendanceDayDto.class, inParams, "daysList");
    }

    public List<ReminderDto> getStudentReminders(Integer studentId, Integer parentId, LocalDate today) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_id_student", studentId);
        inParams.put("p_id_parent", parentId);
        inParams.put("p_today", Date.valueOf(today));
        return executeAndConvertList(getStudentRemindersCall, ReminderDto.class, inParams, "remindersList");
    }

    public List<IncidentDto> getStudentIncidents(Integer studentId, Integer parentId, String status) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_id_student", studentId);
        inParams.put("p_id_parent", parentId);
        inParams.put("p_status", status);
        return executeAndConvertList(getStudentIncidentsCall, IncidentDto.class, inParams, "incidentsList");
    }

    public ParentProfileDto getParentProfileSummary(Integer parentId) {
        List<ParentProfileDto> list = executeAndConvertList(getParentProfileSummaryCall, ParentProfileDto.class, "p_id_parent", parentId, "profileList");
        return list.isEmpty() ? null : list.get(0);
    }

    public List<AnnouncementDto> getParentAnnouncements(Integer parentId, String priority, String sender, String status) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_id_parent", parentId);
        inParams.put("p_priority", priority);
        inParams.put("p_sender", sender);
        inParams.put("p_status", status);
        return executeAndConvertList(getParentAnnouncementsCall, AnnouncementDto.class, inParams, "announcementsList");
    }

    public ReadAnnouncementResponse markAnnouncementRead(Integer parentId, Integer announcementId) {
        Map<String, Object> inParams = Map.of(
                "p_id_parent", parentId,
                "p_id_announcement", announcementId
        );
        List<ReadAnnouncementResponse> list = executeAndConvertList(markAnnouncementReadCall, ReadAnnouncementResponse.class, inParams, "readResult");
        return list.isEmpty() ? null : list.get(0);
    }

    public List<FlatReportDto> getStudentReports(Integer studentId, String type, String format) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_id_student", studentId);
        inParams.put("p_type", type);
        inParams.put("p_format", format);
        return executeAndConvertList(getStudentReportsCall, FlatReportDto.class, inParams, "reportsList");
    }

    public PreferencesResponse updateParentPreferences(Integer parentId, Boolean darkMode) {
        Map<String, Object> inParams = Map.of(
                "p_id_parent", parentId,
                "p_dark_mode", darkMode
        );
        List<PreferencesResponse> list = executeAndConvertList(updateParentPreferencesCall, PreferencesResponse.class, inParams, "prefResult");
        return list.isEmpty() ? null : list.get(0);
    }

    public List<CitationDto> getParentStudentCitations(Integer parentId, Integer studentId, String status, String from, String to) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_id_parent", parentId);
        inParams.put("p_id_student", studentId);
        inParams.put("p_status", status);
        inParams.put("p_from", from != null ? Date.valueOf(from) : null);
        inParams.put("p_to", to != null ? Date.valueOf(to) : null);
        return executeAndConvertList(getParentStudentCitationsCall, CitationDto.class, inParams, "citationsList");
    }

    public CitationDto getCitationDetail(Integer citationId, Integer parentId) {
        Map<String, Object> inParams = Map.of(
                "p_id_citation", citationId,
                "p_id_parent", parentId
        );
        List<CitationDto> list = executeAndConvertList(getCitationDetailCall, CitationDto.class, inParams, "citationDetail");
        return list.isEmpty() ? null : list.get(0);
    }

    public RespondCitationResponse respondToCitation(Integer citationId, Integer parentId, String status, String reason) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_id_citation", citationId);
        inParams.put("p_id_parent", parentId);
        inParams.put("p_status", status);
        inParams.put("p_reason", reason);
        List<RespondCitationResponse> list = executeAndConvertList(respondToCitationCall, RespondCitationResponse.class, inParams, "respondResult");
        return list.isEmpty() ? null : list.get(0);
    }

    public ConfirmCitationResponse confirmCitation(Integer citationId, Integer parentId) {
        Map<String, Object> inParams = Map.of(
                "p_id_citation", citationId,
                "p_id_parent", parentId
        );
        List<ConfirmCitationResponse> list = executeAndConvertList(confirmCitationCall, ConfirmCitationResponse.class, inParams, "confirmResult");
        return list.isEmpty() ? null : list.get(0);
    }

    public List<CitationMessageDto> getCitationMessages(Integer citationId, Integer parentId, String after) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_id_citation", citationId);
        inParams.put("p_id_parent", parentId);
        inParams.put("p_after", after != null ? java.sql.Timestamp.valueOf(java.time.LocalDateTime.parse(after)) : null);
        return executeAndConvertList(getCitationMessagesCall, CitationMessageDto.class, inParams, "messagesList");
    }

    public CitationMessageDto sendCitationMessage(Integer citationId, String body, Integer parentId) {
        Map<String, Object> inParams = Map.of(
                "p_id_citation", citationId,
                "p_sender_type", "parent",
                "p_sender_id", parentId,
                "p_body", body
        );
        List<CitationMessageDto> list = executeAndConvertList(sendCitationMessageCall, CitationMessageDto.class, inParams, "sendMessageResult");
        return list.isEmpty() ? null : list.get(0);
    }

    public List<CitationEventDto> getCitationEvents(Integer citationId) {
        return executeAndConvertList(getCitationEventsCall, CitationEventDto.class, "p_id_citation", citationId, "eventsList");
    }
}
