package com.learnix.web.controller.rest;

import com.learnix.web.dto.MobileDtos.*;
import com.learnix.web.security.AccessControlService;
import com.learnix.web.security.SecurityInputValidator;
import com.learnix.web.service.MobileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/mobile")
@RequiredArgsConstructor
public class MobileParentController {

    private final MobileService mobileService;
    private final AccessControlService accessControlService;
    private final SecurityInputValidator inputValidator;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MobileParentController.class);

    private static final Set<String> ALERT_STATUSES = Set.of("new", "read", "archived");
    private static final Set<String> ANNOUNCEMENT_PRIORITIES = Set.of("alta", "media", "baja", "normal");
    private static final Set<String> CITATION_STATUSES = Set.of("pending", "accepted", "rejected", "cancelled");
    private static final Set<String> REPORT_FORMATS = Set.of("pdf", "excel", "xlsx", "xls");

    @GetMapping("/parents/{parentId}/students/{studentId}/dashboard")
    public ResponseEntity<DashboardMobileResponse> getDashboard(
            @PathVariable Integer parentId,
            @PathVariable Integer studentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer authenticatedParentId = authenticatedParent(authHeader);
        requireParentStudent(parentId, studentId, authenticatedParentId);
        DashboardMobileResponse dashboard = mobileService.getDashboard(parentId, studentId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/students/{studentId}/progress")
    public ResponseEntity<ProgressMobileResponse> getProgress(
            @PathVariable Integer studentId,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) Integer courseId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer authenticatedParentId = authenticatedParent(authHeader);
        accessControlService.requireParentStudentAccess(authenticatedParentId, inputValidator.requirePositive(studentId, "studentId"));
        String safeTerm = inputValidator.optionalSafeText(term, "term", 20);
        if (courseId != null) {
            inputValidator.requirePositive(courseId, "courseId");
        }
        ProgressMobileResponse progress = mobileService.getProgress(studentId, safeTerm, courseId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/students/{studentId}/attendance")
    public ResponseEntity<AttendanceDetailResponse> getAttendance(
            @PathVariable Integer studentId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer authenticatedParentId = authenticatedParent(authHeader);
        accessControlService.requireParentStudentAccess(authenticatedParentId, inputValidator.requirePositive(studentId, "studentId"));
        String safeFrom = inputValidator.optionalSafeText(from, "from", 20);
        String safeTo = inputValidator.optionalSafeText(to, "to", 20);
        AttendanceDetailResponse attendance = mobileService.getAttendance(studentId, safeFrom, safeTo);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/parents/{parentId}/students/{studentId}/reminders")
    public ResponseEntity<RemindersResponse> getReminders(
            @PathVariable Integer parentId,
            @PathVariable Integer studentId,
            @RequestParam(required = false) String today,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer authenticatedParentId = authenticatedParent(authHeader);
        requireParentStudent(parentId, studentId, authenticatedParentId);
        String safeToday = inputValidator.optionalSafeText(today, "today", 20);
        RemindersResponse reminders = mobileService.getReminders(parentId, studentId, safeToday);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/parents/{parentId}/students/{studentId}/incidents")
    public ResponseEntity<IncidentsResponse> getIncidents(
            @PathVariable Integer parentId,
            @PathVariable Integer studentId,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer authenticatedParentId = authenticatedParent(authHeader);
        requireParentStudent(parentId, studentId, authenticatedParentId);
        String safeStatus = inputValidator.optionalAllowedValue(status, "status", ALERT_STATUSES);
        IncidentsResponse incidents = mobileService.getIncidents(parentId, studentId, safeStatus);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/parents/{parentId}/students/{studentId}/profile-summary")
    public ResponseEntity<ProfileSummaryResponse> getProfileSummary(
            @PathVariable Integer parentId,
            @PathVariable Integer studentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer authenticatedParentId = authenticatedParent(authHeader);
        requireParentStudent(parentId, studentId, authenticatedParentId);
        ProfileSummaryResponse profileSummary = mobileService.getProfileSummary(parentId, studentId);
        return ResponseEntity.ok(profileSummary);
    }

    @GetMapping("/parents/{parentId}/announcements")
    public ResponseEntity<AnnouncementsResponse> getAnnouncements(
            @PathVariable Integer parentId,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String sender,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer authenticatedParentId = authenticatedParent(authHeader);
        accessControlService.requireSameParent(parentId, authenticatedParentId);
        String safePriority = inputValidator.optionalAllowedValue(priority, "priority", ANNOUNCEMENT_PRIORITIES);
        String safeSender = inputValidator.optionalSafeText(sender, "sender", 120);
        String safeStatus = inputValidator.optionalAllowedValue(status, "status", ALERT_STATUSES);
        AnnouncementsResponse announcements = mobileService.getAnnouncements(parentId, safePriority, safeSender, safeStatus);
        return ResponseEntity.ok(announcements);
    }

    @PatchMapping("/parents/{parentId}/announcements/{announcementId}/read")
    public ResponseEntity<ReadAnnouncementResponse> markAnnouncementRead(
            @PathVariable Integer parentId,
            @PathVariable Integer announcementId,
            @RequestBody ReadAnnouncementRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer authenticatedParentId = authenticatedParent(authHeader);
        accessControlService.requireSameParent(parentId, authenticatedParentId);
        accessControlService.requireParentAnnouncementAccess(parentId, inputValidator.requirePositive(announcementId, "announcementId"));
        ReadAnnouncementResponse response = mobileService.markAnnouncementRead(parentId, announcementId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/students/{studentId}/reports")
    public ResponseEntity<ReportsResponse> getReports(
            @PathVariable Integer studentId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String format,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer authenticatedParentId = authenticatedParent(authHeader);
        accessControlService.requireParentStudentAccess(authenticatedParentId, inputValidator.requirePositive(studentId, "studentId"));
        String safeType = inputValidator.optionalSafeText(type, "type", 60);
        String safeFormat = inputValidator.optionalAllowedValue(format, "format", REPORT_FORMATS);
        ReportsResponse reports = mobileService.getReports(studentId, safeType, safeFormat);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/students/{studentId}/reports/{reportId}/download")
    public ResponseEntity<byte[]> downloadReport(
            @PathVariable Integer studentId,
            @PathVariable Integer reportId,
            @RequestParam String format,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer authenticatedParentId = authenticatedParent(authHeader);
        accessControlService.requireParentStudentAccess(authenticatedParentId, inputValidator.requirePositive(studentId, "studentId"));
        inputValidator.requirePositive(reportId, "reportId");
        String safeFormat = inputValidator.optionalAllowedValue(format, "format", REPORT_FORMATS);
        
        // Provisional binary data mock
        byte[] dummyContent = "Contenido de reporte provicional - Learnix Mobile".getBytes();
        HttpHeaders headers = new HttpHeaders();
        
        if ("excel".equalsIgnoreCase(safeFormat) || "xlsx".equalsIgnoreCase(safeFormat)) {
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
            @RequestBody PreferencesRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer authenticatedParentId = authenticatedParent(authHeader);
        accessControlService.requireSameParent(parentId, authenticatedParentId);
        if (request == null || request.darkMode() == null) {
            throw new IllegalArgumentException("darkMode es obligatorio.");
        }
        PreferencesResponse response = mobileService.updatePreferences(parentId, request.darkMode());
        return ResponseEntity.ok(response);
    }

    private Integer extractParentId(String authHeader) {
        return authenticatedParent(authHeader);
    }

    @GetMapping("/parents/{parentId}/students/{studentId}/citations")
    public ResponseEntity<CitationListResponse> getCitations(
            @PathVariable Integer parentId,
            @PathVariable Integer studentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer authenticatedParentId = authenticatedParent(authHeader);
        requireParentStudent(parentId, studentId, authenticatedParentId);
        String safeStatus = inputValidator.optionalAllowedValue(status, "status", CITATION_STATUSES);
        String safeFrom = inputValidator.optionalSafeText(from, "from", 20);
        String safeTo = inputValidator.optionalSafeText(to, "to", 20);
        CitationListResponse citations = mobileService.getParentStudentCitations(parentId, studentId, safeStatus, safeFrom, safeTo);
        return ResponseEntity.ok(citations);
    }

    @GetMapping("/citations/{citationId}")
    public ResponseEntity<CitationDto> getCitationDetail(
            @PathVariable Integer citationId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer parentId = extractParentId(authHeader);
        accessControlService.requireParentCitationAccess(parentId, inputValidator.requirePositive(citationId, "citationId"));
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
        accessControlService.requireParentCitationAccess(parentId, inputValidator.requirePositive(citationId, "citationId"));
        if (request == null || request.status() == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "code", "VALIDATION_ERROR",
                "message", "El estado es requerido."
            ));
        }
        try {
            String safeStatus = inputValidator.optionalAllowedValue(request.status(), "status", Set.of("accepted", "rejected"));
            RespondCitationResponse response = mobileService.respondToCitation(citationId, parentId, safeStatus);
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(response);
        } catch (org.springframework.jdbc.UncategorizedSQLException e) {
            log.warn("security_event=mobile_citation_response_conflict citationId={} parentId={}", citationId, parentId);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of(
                "code", "CONFLICT",
                "message", "No se pudo procesar la respuesta a la citacion."
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
        accessControlService.requireParentCitationAccess(parentId, inputValidator.requirePositive(citationId, "citationId"));
        if (request == null || request.confirmed() == null || !request.confirmed()) {
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
            log.warn("security_event=mobile_citation_confirm_conflict citationId={} parentId={}", citationId, parentId);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of(
                "code", "CONFLICT",
                "message", "No se pudo confirmar la citacion."
            ));
        }
    }

    @GetMapping("/citations/{citationId}/messages")
    public ResponseEntity<CitationMessagesResponse> getCitationMessages(
            @PathVariable Integer citationId,
            @RequestParam(required = false) String after,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer parentId = extractParentId(authHeader);
        accessControlService.requireParentCitationAccess(parentId, inputValidator.requirePositive(citationId, "citationId"));
        String safeAfter = inputValidator.optionalSafeText(after, "after", 30);
        CitationMessagesResponse messages = mobileService.getCitationMessages(citationId, parentId, safeAfter);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/citations/{citationId}/messages")
    public ResponseEntity<?> sendCitationMessage(
            @PathVariable Integer citationId,
            @RequestBody SendCitationMessageRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer parentId = extractParentId(authHeader);
        accessControlService.requireParentCitationAccess(parentId, inputValidator.requirePositive(citationId, "citationId"));
        if (request == null) {
            throw new IllegalArgumentException("body es obligatorio.");
        }
        String safeBody = inputValidator.requireSafeText(request.body(), "body", 1000);
        if (safeBody.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "code", "VALIDATION_ERROR",
                "message", "El cuerpo del mensaje no puede estar vacio."
            ));
        }
        try {
            SendCitationMessageResponse response = mobileService.sendCitationMessage(citationId, safeBody, parentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (org.springframework.jdbc.UncategorizedSQLException e) {
            log.warn("security_event=mobile_citation_message_conflict citationId={} parentId={}", citationId, parentId);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of(
                "code", "CONFLICT",
                "message", "No se pudo enviar el mensaje."
            ));
        }
    }

    @GetMapping("/citations/{citationId}/events")
    public ResponseEntity<CitationEventsResponse> getCitationEvents(
            @PathVariable Integer citationId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer parentId = extractParentId(authHeader);
        accessControlService.requireParentCitationAccess(parentId, inputValidator.requirePositive(citationId, "citationId"));
        CitationEventsResponse events = mobileService.getCitationEvents(citationId);
        return ResponseEntity.ok(events);
    }

    private Integer authenticatedParent(String authHeader) {
        return accessControlService.requireParentFromBearer(authHeader);
    }

    private void requireParentStudent(Integer parentId, Integer studentId, Integer authenticatedParentId) {
        inputValidator.requirePositive(parentId, "parentId");
        inputValidator.requirePositive(studentId, "studentId");
        accessControlService.requireSameParent(parentId, authenticatedParentId);
        accessControlService.requireParentStudentAccess(parentId, studentId);
    }
}

