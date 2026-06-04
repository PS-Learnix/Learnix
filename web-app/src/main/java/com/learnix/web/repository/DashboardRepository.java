package com.learnix.web.repository;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnix.web.dto.DashboardCourseResponse;
import com.learnix.web.dto.DashboardStatsResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public class DashboardRepository extends BaseProcedureRepository{

    public SimpleJdbcCall getTeacherCoursesCall;
    public SimpleJdbcCall overallCourseAverageCall;
    public SimpleJdbcCall averageCourseAttendanceCall;
    public SimpleJdbcCall studentsRiskSuccessCall;

    public DashboardRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        super(jdbcTemplate, objectMapper);
    }

    @PostConstruct
    public void init() {
        this.getTeacherCoursesCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_teacher_courses")
                .returningResultSet("coursesList",
                        new DataClassRowMapper<>(DashboardCourseResponse.class));
        this.overallCourseAverageCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_overall_course_average");
        this.averageCourseAttendanceCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_average_course_attendance");
        this.studentsRiskSuccessCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_students_risk_success");
    }

    public List<DashboardCourseResponse> getTeacherCoursesList(Integer idTeacher) {
        return executeAndConvertList(
                getTeacherCoursesCall, DashboardCourseResponse.class,
                "p_id_teacher", idTeacher, "coursesList"
        );
    }

    public DashboardStatsResponse getDashboardStats(Integer idCoursePeriod) {
        Map<?, ?> averageMap = executeAndConvert(overallCourseAverageCall, Map.class,
                "p_id_course_period", idCoursePeriod);
        Map<?, ?> attendanceMap = executeAndConvert(averageCourseAttendanceCall, Map.class,
                "p_id_course_period", idCoursePeriod);
        Map<?, ?> riskSuccessMap = executeAndConvert(studentsRiskSuccessCall, Map.class,
                "p_id_course_period", idCoursePeriod);

        String overallAverage = "0.00";
        if (averageMap != null) {
            Object avgValue = averageMap.get("o_promedio") != null
                    ? averageMap.get("o_promedio")
                    : averageMap.get("promedio");
            if (avgValue != null) overallAverage = avgValue.toString();
        }

        String averageAttendance = "0.00";
        if (attendanceMap != null) {
            Object attValue = attendanceMap.get("o_asistencia") != null
                    ? attendanceMap.get("o_asistencia")
                    : attendanceMap.get("asistencia");
            if (attValue != null) averageAttendance = attValue.toString();
        }

        BigDecimal approved = BigDecimal.ZERO;
        if (riskSuccessMap != null && riskSuccessMap.get("o_aprobados_pct") != null) {
            approved = new BigDecimal(riskSuccessMap.get("o_aprobados_pct").toString());
        }

        BigDecimal risk = BigDecimal.ZERO;
        if (riskSuccessMap != null && riskSuccessMap.get("o_riesgo_pct") != null) {
            risk = new BigDecimal(riskSuccessMap.get("o_riesgo_pct").toString());
        }

        return new DashboardStatsResponse(
                overallAverage,
                averageAttendance,
                approved + "%",
                risk + "%"
        );
    }

}
