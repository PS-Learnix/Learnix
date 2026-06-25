package com.learnix.web.dto;

import java.util.List;

public class MobileDtos {

    // 1. Login
    public record LoginRequest(String email, String password) {}
    
    public record ParentDto(Integer id, String firstName, String lastName, String email) {}
    
    public record ParentProfileDto(Integer id, String fullName, String email) {}
    
    public record StudentMobileDto(Integer id, String fullName, String gradeSection, Boolean isPrimary) {}
    
    public record LoginResponse(String token, ParentDto parent, List<StudentMobileDto> students) {}

    // 2. Dashboard
    public record StudentDetailDto(Integer id, String fullName, String gradeSection, Double generalAverage, Double attendancePercentage, String academicStatus) {}
    
    public record CourseAverageDto(Integer courseId, Integer coursePeriodId, String courseName, Double average) {}
    
    public record ActivityMobileDto(Integer id, String name, String courseName, String term, String date, String grade, String status) {}
    
    public record AttendanceStatsDto(Long attendedDays, Long absentDays, Long lateDays, Double attendancePercentage) {}
    
    public record ReportDashboardDto(Integer id, String title, String description, List<String> formats) {}
    
    public record DashboardMobileResponse(
            StudentDetailDto student,
            List<CourseAverageDto> courseAverages,
            List<ActivityMobileDto> activities,
            AttendanceStatsDto attendanceStats,
            List<ReportDashboardDto> reports
    ) {}

    // 3. Progress
    public record TermProgressDto(String term, Double average) {}
    
    public record ActivityProgressDto(Integer id, String name, Integer courseId, String courseName, String term, String date, String grade, String status) {}
    
    public record CourseFilterDto(Integer id, String name) {}
    
    public record AvailableFiltersDto(List<String> terms, List<CourseFilterDto> courses) {}
    
    public record ProgressMobileResponse(
            List<TermProgressDto> termProgress,
            List<ActivityProgressDto> activities,
            AvailableFiltersDto availableFilters
    ) {}

    // 4. Attendance
    public record AttendanceDayDto(String date, String status) {}
    
    public record AttendanceDetailResponse(AttendanceStatsDto stats, List<AttendanceDayDto> days) {}

    // 5. Reminders
    public record ReminderDto(Integer id, String type, String title, String detail, String date, String severity) {}
    
    public record RemindersResponse(Integer count, List<ReminderDto> items) {}

    // 6. Incidents
    public record IncidentDto(Integer id, String title, String detail, String date, String status, String severity) {}
    
    public record IncidentsResponse(List<IncidentDto> items) {}

    // 7. Profile Summary
    public record StudentProfileDto(Integer id, String fullName, String gradeSection) {}
    
    public record AnnouncementPreviewDto(Integer id, String title, String sender, String date) {}
    
    public record IncidentPreviewDto(Integer id, String title, String detail) {}
    
    public record ProfileSummaryResponse(
            ParentProfileDto parent,
            StudentProfileDto student,
            List<AnnouncementPreviewDto> announcementPreview,
            List<IncidentPreviewDto> incidentPreview
    ) {}

    // 8. Announcements
    public record FiltersDto(List<String> priorities, List<String> senders, List<String> statuses) {}
    
    public record AnnouncementDto(Integer id, String title, String body, String sender, String priority, String date, String status) {}
    
    public record AnnouncementsResponse(FiltersDto filters, List<AnnouncementDto> items) {}

    // 9. Read Announcement
    public record ReadAnnouncementRequest(Boolean read) {}
    
    public record ReadAnnouncementResponse(Integer id, String status, String readAt) {}

    // 10. Reports
    public record ReportFileDto(String format, String url) {}
    
    public record FlatReportDto(Integer id, String title, String description, String format, String url, String generatedAt) {}
    
    public record ReportResponseDto(Integer id, String title, String description, List<String> formats, List<ReportFileDto> files, String generatedAt) {}
    
    public record ReportsResponse(List<ReportResponseDto> items) {}

    // 11. Preferences
    public record PreferencesRequest(Boolean darkMode) {}
    
    public record PreferencesResponse(Integer parentId, Boolean darkMode, String updatedAt) {}

    // 12. Citations PMV3
    public record CitationDto(
        Integer id,
        String title,
        String detail,
        String teacherName,
        String scheduledAt,
        String status,
        String mode,
        String meetingUrl,
        String scope,
        Integer unreadMessages
    ) {}

    public record CitationListResponse(List<CitationDto> items) {}

    public record RespondCitationRequest(String status, String reason) {}

    public record RespondCitationResponse(CitationDto citation, Integer eventId) {}

    public record ConfirmCitationRequest(Boolean confirmed) {}

    public record ConfirmCitationResponse(CitationDto citation, String confirmedAt) {}

    public record CitationMessageDto(
        Integer id,
        Integer citationId,
        String senderName,
        String senderRole,
        String body,
        String sentAt,
        Boolean isFromParent,
        Boolean isRead
    ) {}

    public record CitationMessagesResponse(List<CitationMessageDto> items) {}

    public record SendCitationMessageRequest(String body) {}

    public record SendCitationMessageResponse(CitationMessageDto message) {}

    public record CitationEventDto(
        Integer id,
        String eventType,
        String actorName,
        String actorRole,
        String createdAt,
        String payload
    ) {}

    public record CitationEventsResponse(List<CitationEventDto> items) {}
}

