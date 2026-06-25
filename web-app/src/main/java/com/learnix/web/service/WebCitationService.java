package com.learnix.web.service;

import com.learnix.web.dto.MobileDtos.CitationDto;
import com.learnix.web.dto.MobileDtos.CitationEventDto;
import com.learnix.web.dto.MobileDtos.CitationMessageDto;
import com.learnix.web.repository.WebCitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebCitationService {

    private final JdbcTemplate jdbcTemplate;
    private final WebCitationRepository webCitationRepository;

    @Transactional
    public Integer createCitation(Integer studentId, Integer coursePeriodId, Integer teacherId,
                                  String title, String detail, String scheduledAt,
                                  String mode, String meetingUrl, String scope) {
        return webCitationRepository.createCitation(
                studentId, coursePeriodId, teacherId, title, detail, scheduledAt, mode, meetingUrl, scope
        );
    }

    public List<Map<String, Object>> getTeacherCitations(Integer teacherId) {
        return webCitationRepository.getTeacherCitations(teacherId);
    }

    public CitationDto getCitationDetail(Integer citationId, Integer teacherId) {
        return webCitationRepository.getCitationDetail(citationId, teacherId);
    }

    public List<Map<String, Object>> getCitationRecipients(Integer citationId) {
        return webCitationRepository.getCitationRecipients(citationId);
    }

    public Map<String, Object> getCitationResponseSummary(Integer citationId) {
        return webCitationRepository.getCitationResponseSummary(citationId);
    }

    public List<Map<String, Object>> getCommunicationContacts(Integer citationId) {
        return webCitationRepository.getCommunicationContacts(citationId);
    }

    public List<CitationMessageDto> getCitationMessages(Integer citationId, Integer parentId, String after) {
        return webCitationRepository.getCitationMessages(citationId, parentId, after);
    }

    @Transactional
    public CitationMessageDto sendTeacherMessage(Integer citationId, String body, Integer teacherId, Integer parentId) {
        return webCitationRepository.sendTeacherMessage(citationId, body, teacherId, parentId);
    }

    @Transactional
    public void reviewJustification(Integer citationId, Integer recipientId, Integer teacherId, String reviewStatus) {
        if (!"justified".equals(reviewStatus) && !"not_justified".equals(reviewStatus)) {
            throw new IllegalArgumentException("Estado de justificacion no valido.");
        }
        webCitationRepository.reviewJustification(citationId, recipientId, teacherId, reviewStatus);
    }

    @Transactional
    public void cancelCitation(Integer citationId, Integer teacherId) {
        webCitationRepository.cancelCitation(citationId, teacherId);
    }

    public List<CitationEventDto> getCitationEvents(Integer citationId) {
        return webCitationRepository.getCitationEvents(citationId);
    }

    public List<Map<String, Object>> getActiveStudents() {
        return jdbcTemplate.queryForList("SELECT id_student as id, CONCAT(last_name, ', ', first_name) as fullName FROM students ORDER BY last_name, first_name");
    }

    public List<Map<String, Object>> getActiveSections() {
        return jdbcTemplate.queryForList(
                "SELECT cp.id_course_period as id, CONCAT(c.name, ' - ', cp.section) as name " +
                "FROM course_periods cp " +
                "JOIN courses c ON cp.id_course = c.id_course " +
                "ORDER BY c.name, cp.section"
        );
    }
}
