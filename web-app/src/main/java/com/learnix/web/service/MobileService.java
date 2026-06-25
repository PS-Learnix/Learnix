package com.learnix.web.service;

import com.learnix.web.dto.MobileDtos.*;
import com.learnix.web.repository.MobileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MobileService {

    private final MobileRepository mobileRepository;

    public LoginResponse login(String email, String password) {
        ParentDto parent = mobileRepository.authenticateParent(email, password);
        if (parent == null) {
            return null;
        }
        List<StudentMobileDto> students = mobileRepository.getParentStudents(parent.id());
        String token = "jwt-token-parent-" + parent.id() + "-" + UUID.randomUUID().toString().substring(0, 8);
        return new LoginResponse(token, parent, students);
    }

    public DashboardMobileResponse getDashboard(Integer parentId, Integer studentId) {
        StudentDetailDto student = mobileRepository.getStudentDetail(studentId);
        List<CourseAverageDto> courseAverages = mobileRepository.getStudentCourseAverages(studentId);
        List<ActivityMobileDto> activities = mobileRepository.getStudentActivitiesDashboard(studentId);
        AttendanceStatsDto attendanceStats = mobileRepository.getStudentAttendanceStats(studentId);
        List<ReportDashboardDto> reports = mobileRepository.getStudentReportsDashboard(studentId);

        return new DashboardMobileResponse(student, courseAverages, activities, attendanceStats, reports);
    }

    public ProgressMobileResponse getProgress(Integer studentId, String term, Integer courseId) {
        List<TermProgressDto> termProgress = mobileRepository.getStudentTermProgress(studentId);
        List<ActivityProgressDto> activities = mobileRepository.getStudentProgressActivities(studentId, term, courseId);

        // Build dynamically from student's enrolled courses
        List<CourseAverageDto> averages = mobileRepository.getStudentCourseAverages(studentId);
        List<CourseFilterDto> courses = averages.stream()
                .map(a -> new CourseFilterDto(a.courseId(), a.courseName()))
                .distinct()
                .toList();

        AvailableFiltersDto availableFilters = new AvailableFiltersDto(
                List.of("B1", "B2", "B3", "B4"),
                courses
        );

        return new ProgressMobileResponse(termProgress, activities, availableFilters);
    }

    public AttendanceDetailResponse getAttendance(Integer studentId, String fromStr, String toStr) {
        LocalDate from = fromStr != null ? LocalDate.parse(fromStr) : null;
        LocalDate to = toStr != null ? LocalDate.parse(toStr) : null;

        AttendanceStatsDto stats = mobileRepository.getStudentAttendanceStats(studentId);
        List<AttendanceDayDto> days = mobileRepository.getStudentAttendanceDetail(studentId, from, to);

        return new AttendanceDetailResponse(stats, days);
    }

    public RemindersResponse getReminders(Integer parentId, Integer studentId, String todayStr) {
        LocalDate today = todayStr != null ? LocalDate.parse(todayStr) : LocalDate.now();
        List<ReminderDto> reminders = mobileRepository.getStudentReminders(studentId, parentId, today);
        return new RemindersResponse(reminders.size(), reminders);
    }

    public IncidentsResponse getIncidents(Integer parentId, Integer studentId, String status) {
        List<IncidentDto> incidents = mobileRepository.getStudentIncidents(studentId, parentId, status);
        return new IncidentsResponse(incidents);
    }

    public ProfileSummaryResponse getProfileSummary(Integer parentId, Integer studentId) {
        ParentProfileDto parent = mobileRepository.getParentProfileSummary(parentId);
        
        StudentDetailDto studentDetail = mobileRepository.getStudentDetail(studentId);
        StudentProfileDto student = null;
        if (studentDetail != null) {
            student = new StudentProfileDto(studentDetail.id(), studentDetail.fullName(), studentDetail.gradeSection());
        }

        // Preview announcements (up to 3)
        List<AnnouncementDto> announcements = mobileRepository.getParentAnnouncements(parentId, null, null, null);
        List<AnnouncementPreviewDto> announcementPreview = announcements.stream()
                .limit(3)
                .map(a -> new AnnouncementPreviewDto(a.id(), a.title(), a.sender(), a.date()))
                .toList();

        // Preview incidents (up to 3)
        List<IncidentDto> incidents = mobileRepository.getStudentIncidents(studentId, parentId, null);
        List<IncidentPreviewDto> incidentPreview = incidents.stream()
                .limit(3)
                .map(i -> new IncidentPreviewDto(i.id(), i.title(), i.detail()))
                .toList();

        return new ProfileSummaryResponse(parent, student, announcementPreview, incidentPreview);
    }

    public AnnouncementsResponse getAnnouncements(Integer parentId, String priority, String sender, String status) {
        List<AnnouncementDto> items = mobileRepository.getParentAnnouncements(parentId, priority, sender, status);
        
        FiltersDto filters = new FiltersDto(
                List.of("Alta", "Media", "Normal"),
                List.of("Dirección", "Coordinación", "Secretaría Académica"),
                List.of("new", "read")
        );

        return new AnnouncementsResponse(filters, items);
    }

    public ReadAnnouncementResponse markAnnouncementRead(Integer parentId, Integer announcementId) {
        return mobileRepository.markAnnouncementRead(parentId, announcementId);
    }

    public ReportsResponse getReports(Integer studentId, String type, String format) {
        List<FlatReportDto> flatReports = mobileRepository.getStudentReports(studentId, type, format);
        
        // Group reports with the same title to aggregate formats and files
        Map<String, List<FlatReportDto>> grouped = flatReports.stream()
                .collect(Collectors.groupingBy(FlatReportDto::title, LinkedHashMap::new, Collectors.toList()));

        List<ReportResponseDto> items = new ArrayList<>();
        for (Map.Entry<String, List<FlatReportDto>> entry : grouped.entrySet()) {
            List<FlatReportDto> group = entry.getValue();
            FlatReportDto first = group.get(0);
            
            List<String> formats = group.stream().map(FlatReportDto::format).distinct().toList();
            List<ReportFileDto> files = group.stream().map(f -> new ReportFileDto(f.format(), f.url())).toList();
            
            items.add(new ReportResponseDto(first.id(), first.title(), first.description(), formats, files, first.generatedAt()));
        }

        return new ReportsResponse(items);
    }

    public PreferencesResponse updatePreferences(Integer parentId, Boolean darkMode) {
        return mobileRepository.updateParentPreferences(parentId, darkMode);
    }

    public CitationListResponse getParentStudentCitations(Integer parentId, Integer studentId, String status, String from, String to) {
        List<CitationDto> items = mobileRepository.getParentStudentCitations(parentId, studentId, status, from, to);
        return new CitationListResponse(items);
    }

    public CitationDto getCitationDetail(Integer citationId, Integer parentId) {
        return mobileRepository.getCitationDetail(citationId, parentId);
    }

    public RespondCitationResponse respondToCitation(Integer citationId, Integer parentId, String status, String reason) {
        return mobileRepository.respondToCitation(citationId, parentId, status, reason);
    }

    public ConfirmCitationResponse confirmCitation(Integer citationId, Integer parentId) {
        return mobileRepository.confirmCitation(citationId, parentId);
    }

    public CitationMessagesResponse getCitationMessages(Integer citationId, Integer parentId, String after) {
        List<CitationMessageDto> items = mobileRepository.getCitationMessages(citationId, parentId, after);
        return new CitationMessagesResponse(items);
    }

    public SendCitationMessageResponse sendCitationMessage(Integer citationId, String body, Integer parentId) {
        CitationMessageDto message = mobileRepository.sendCitationMessage(citationId, body, parentId);
        return new SendCitationMessageResponse(message);
    }

    public CitationEventsResponse getCitationEvents(Integer citationId) {
        List<CitationEventDto> items = mobileRepository.getCitationEvents(citationId);
        return new CitationEventsResponse(items);
    }
}

