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

    private Integer extractParentId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return 1;
        }
        String token = authHeader.substring(7);
        if (token.startsWith("jwt-token-parent-")) {
            try {
                String[] parts = token.split("-");
                if (parts.length >= 4) {
                    return Integer.parseInt(parts[3]);
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return 1;
    }

    @GetMapping("/parents/{parentId}/students/{studentId}/citations")
    public ResponseEntity<CitationListResponse> getCitations(
            @PathVariable Integer parentId,
            @PathVariable Integer studentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        CitationListResponse citations = mobileService.getParentStudentCitations(parentId, studentId, status, from, to);
        return ResponseEntity.ok(citations);
    }

    @GetMapping("/citations/{citationId}")
    public ResponseEntity<CitationDto> getCitationDetail(
            @PathVariable Integer citationId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer parentId = extractParentId(authHeader);
        CitationDto citation = mobileService.getCitationDetail(citationId, parentId);
        if (citation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(citation);
    }

    @PatchMapping("/citations/{citationId}/response")
    public ResponseEntity<?> respondToCitation(
            @PathVariable Integer citationId,
            @RequestBody RespondCitationRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer parentId = extractParentId(authHeader);
        if (request.status() == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "code", "VALIDATION_ERROR",
                "message", "El estado es requerido."
            ));
        }
        try {
            RespondCitationResponse response = mobileService.respondToCitation(citationId, parentId, request.status());
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(response);
        } catch (org.springframework.jdbc.UncategorizedSQLException e) {
            String msg = e.getSQLException() != null ? e.getSQLException().getMessage() : e.getMessage();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of(
                "code", "CONFLICT",
                "message", msg != null ? msg : "Error al procesar la respuesta a la citación."
            ));
        }
    }

    @Deprecated
    @PatchMapping("/citations/{citationId}/confirm")
    public ResponseEntity<?> confirmCitation(
            @PathVariable Integer citationId,
            @RequestBody ConfirmCitationRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer parentId = extractParentId(authHeader);
        if (request.confirmed() == null || !request.confirmed()) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "code", "VALIDATION_ERROR",
                "message", "Debe confirmar con confirmed = true."
            ));
        }
        try {
            ConfirmCitationResponse response = mobileService.confirmCitation(citationId, parentId);
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(response);
        } catch (org.springframework.jdbc.UncategorizedSQLException e) {
            String msg = e.getSQLException() != null ? e.getSQLException().getMessage() : e.getMessage();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of(
                "code", "CONFLICT",
                "message", msg != null ? msg : "Error al confirmar la citación."
            ));
        }
    }

    @GetMapping("/citations/{citationId}/messages")
    public ResponseEntity<CitationMessagesResponse> getCitationMessages(
            @PathVariable Integer citationId,
            @RequestParam(required = false) String after,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer parentId = extractParentId(authHeader);
        CitationMessagesResponse messages = mobileService.getCitationMessages(citationId, parentId, after);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/citations/{citationId}/messages")
    public ResponseEntity<?> sendCitationMessage(
            @PathVariable Integer citationId,
            @RequestBody SendCitationMessageRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer parentId = extractParentId(authHeader);
        if (request.body() == null || request.body().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "code", "VALIDATION_ERROR",
                "message", "El cuerpo del mensaje no puede estar vacío."
            ));
        }
        try {
            SendCitationMessageResponse response = mobileService.sendCitationMessage(citationId, request.body(), parentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (org.springframework.jdbc.UncategorizedSQLException e) {
            String msg = e.getSQLException() != null ? e.getSQLException().getMessage() : e.getMessage();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of(
                "code", "CONFLICT",
                "message", msg != null ? msg : "Error al enviar el mensaje."
            ));
        }
    }

    @GetMapping("/citations/{citationId}/events")
    public ResponseEntity<CitationEventsResponse> getCitationEvents(
            @PathVariable Integer citationId) {
        CitationEventsResponse events = mobileService.getCitationEvents(citationId);
        return ResponseEntity.ok(events);
    }
}

