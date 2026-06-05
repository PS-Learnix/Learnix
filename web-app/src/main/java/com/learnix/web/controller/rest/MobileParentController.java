package com.learnix.web.controller.rest;

import com.learnix.web.dto.MobileDtos.*;
import com.learnix.web.service.MobileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile")
@RequiredArgsConstructor
public class MobileParentController {

    private final MobileService mobileService;

    @GetMapping("/parents/{parentId}/students/{studentId}/dashboard")
    public ResponseEntity<DashboardMobileResponse> getDashboard(
            @PathVariable Integer parentId,
            @PathVariable Integer studentId) {
        System.out.println("--> MobileParentController: getDashboard called. parentId=" + parentId + ", studentId=" + studentId);
        DashboardMobileResponse dashboard = mobileService.getDashboard(parentId, studentId);
        System.out.println("<-- MobileParentController: getDashboard returned studentId=" + (dashboard.student() != null ? dashboard.student().id() : null));
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/students/{studentId}/progress")
    public ResponseEntity<ProgressMobileResponse> getProgress(
            @PathVariable Integer studentId,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) Integer courseId) {
        ProgressMobileResponse progress = mobileService.getProgress(studentId, term, courseId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/students/{studentId}/attendance")
    public ResponseEntity<AttendanceDetailResponse> getAttendance(
            @PathVariable Integer studentId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        System.out.println("--> MobileParentController: getAttendance called. studentId=" + studentId + ", from=" + from + ", to=" + to);
        AttendanceDetailResponse attendance = mobileService.getAttendance(studentId, from, to);
        System.out.println("<-- MobileParentController: getAttendance returned days count=" + (attendance.days() != null ? attendance.days().size() : 0));
        if (attendance.days() != null) {
            for (int i = 0; i < Math.min(attendance.days().size(), 10); i++) {
                var d = attendance.days().get(i);
                System.out.println("    Day[" + i + "]: date=" + d.date() + ", status=" + d.status());
            }
        }
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/parents/{parentId}/students/{studentId}/reminders")
    public ResponseEntity<RemindersResponse> getReminders(
            @PathVariable Integer parentId,
            @PathVariable Integer studentId,
            @RequestParam(required = false) String today) {
        RemindersResponse reminders = mobileService.getReminders(parentId, studentId, today);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/parents/{parentId}/students/{studentId}/incidents")
    public ResponseEntity<IncidentsResponse> getIncidents(
            @PathVariable Integer parentId,
            @PathVariable Integer studentId,
            @RequestParam(required = false) String status) {
        IncidentsResponse incidents = mobileService.getIncidents(parentId, studentId, status);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/parents/{parentId}/students/{studentId}/profile-summary")
    public ResponseEntity<ProfileSummaryResponse> getProfileSummary(
            @PathVariable Integer parentId,
            @PathVariable Integer studentId) {
        ProfileSummaryResponse profileSummary = mobileService.getProfileSummary(parentId, studentId);
        return ResponseEntity.ok(profileSummary);
    }

    @GetMapping("/parents/{parentId}/announcements")
    public ResponseEntity<AnnouncementsResponse> getAnnouncements(
            @PathVariable Integer parentId,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String sender,
            @RequestParam(required = false) String status) {
        AnnouncementsResponse announcements = mobileService.getAnnouncements(parentId, priority, sender, status);
        return ResponseEntity.ok(announcements);
    }

    @PatchMapping("/parents/{parentId}/announcements/{announcementId}/read")
    public ResponseEntity<ReadAnnouncementResponse> markAnnouncementRead(
            @PathVariable Integer parentId,
            @PathVariable Integer announcementId,
            @RequestBody ReadAnnouncementRequest request) {
        ReadAnnouncementResponse response = mobileService.markAnnouncementRead(parentId, announcementId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/students/{studentId}/reports")
    public ResponseEntity<ReportsResponse> getReports(
            @PathVariable Integer studentId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String format) {
        ReportsResponse reports = mobileService.getReports(studentId, type, format);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/students/{studentId}/reports/{reportId}/download")
    public ResponseEntity<byte[]> downloadReport(
            @PathVariable Integer studentId,
            @PathVariable Integer reportId,
            @RequestParam String format) {
        
        // Provisional binary data mock
        byte[] dummyContent = "Contenido de reporte provicional - Learnix Mobile".getBytes();
        HttpHeaders headers = new HttpHeaders();
        
        if ("excel".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format)) {
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "rendimiento-academico.xlsx");
        } else {
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "rendimiento-academico.pdf");
        }
        
        headers.setContentLength(dummyContent.length);
        return new ResponseEntity<>(dummyContent, headers, HttpStatus.OK);
    }

    @PatchMapping("/parents/{parentId}/preferences")
    public ResponseEntity<PreferencesResponse> updatePreferences(
            @PathVariable Integer parentId,
            @RequestBody PreferencesRequest request) {
        PreferencesResponse response = mobileService.updatePreferences(parentId, request.darkMode());
        return ResponseEntity.ok(response);
    }
}
