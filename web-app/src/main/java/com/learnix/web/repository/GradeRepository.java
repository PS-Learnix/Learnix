package com.learnix.web.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnix.web.dto.ActivityStatsResponse;
import com.learnix.web.dto.StudentGradeResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class GradeRepository extends BaseProcedureRepository {

    private SimpleJdbcCall getActivityGradesCall;
    private SimpleJdbcCall statsRegistroNotasCall;
    private SimpleJdbcCall saveStudentGradeCall;

    public GradeRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        super(jdbcTemplate, objectMapper);
    }

    @PostConstruct
    public void init() {
        this.getActivityGradesCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_activity_grades")
                .returningResultSet("gradesList",
                        new DataClassRowMapper<>(StudentGradeResponse.class));

        this.statsRegistroNotasCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_stats_registro_notas");

        this.saveStudentGradeCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_save_student_grade");
    }

    public List<StudentGradeResponse> getActivityGrades(Integer idActivity) {
        return executeAndConvertList(getActivityGradesCall, StudentGradeResponse.class, "p_id_activity", idActivity, "gradesList");
    }

    public ActivityStatsResponse getActivityStats(Integer idActivity) {
        Map<String, Object> out = statsRegistroNotasCall.execute(Map.of("p_id_activity", idActivity));
        return new ActivityStatsResponse(
                (Integer) out.get("p_total_estudiantes"),
                (Integer) out.get("p_aprobados"),
                (Integer) out.get("p_desaprobados")
        );
    }

    public void saveStudentGrade(Integer idStudent, Integer idActivity, String value) {
        Map<String, Object> inParams = Map.of(
                "p_id_student", idStudent,
                "p_id_activity", idActivity,
                "p_value", value.trim()
        );
        saveStudentGradeCall.execute(inParams);
    }
}
