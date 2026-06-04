package com.learnix.web.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnix.web.dto.ReportCardItem;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ReportRepository extends BaseProcedureRepository {

    private SimpleJdbcCall getStudentReportCardCall;

    public ReportRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        super(jdbcTemplate, objectMapper);
    }

    @PostConstruct
    public void init() {
        this.getStudentReportCardCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_student_report_card")
                .returningResultSet("reportList",
                        new DataClassRowMapper<>(ReportCardItem.class));
    }

    public List<ReportCardItem> getStudentReportCard(Integer idStudent, Integer idCoursePeriod) {
        Map<String, Object> inParams = Map.of(
                "p_id_student", idStudent,
                "p_id_course_period", idCoursePeriod
        );
        return executeAndConvertList(getStudentReportCardCall, ReportCardItem.class, inParams, "reportList");
    }
}
