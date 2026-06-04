package com.learnix.web.service;

import com.learnix.web.dto.ReportCardItem;
import com.learnix.web.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public List<ReportCardItem> getStudentReportCardSp(Integer idStudent, Integer idCoursePeriod) {
        return reportRepository.getStudentReportCard(idStudent, idCoursePeriod);
    }
}