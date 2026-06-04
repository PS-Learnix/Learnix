package com.learnix.web.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnix.web.dto.ObservationResponse;
import com.learnix.web.dto.StudentProfileCourseDto;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class StudentProfileRepository extends BaseProcedureRepository {

    private SimpleJdbcCall getStudentCoursesCall;
    private SimpleJdbcCall getStudentObservationsCall;
    private SimpleJdbcCall addStudentObservationCall;

    public StudentProfileRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        super(jdbcTemplate, objectMapper);
    }

    @PostConstruct
    public void init() {
        this.getStudentCoursesCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_student_dashboard_courses")
                .returningResultSet("coursesList",
                        new DataClassRowMapper<>(StudentProfileCourseDto.class));

        this.getStudentObservationsCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_student_observations")
                .returningResultSet("observationsList",
                        new DataClassRowMapper<>(ObservationResponse.class));

        this.addStudentObservationCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_add_student_observation");
    }

    public List<StudentProfileCourseDto> getStudentCourses(Integer idStudent) {
        return executeAndConvertList(getStudentCoursesCall, StudentProfileCourseDto.class, "p_id_student", idStudent, "coursesList");
    }

    public List<ObservationResponse> getStudentObservations(Integer idStudent) {
        return executeAndConvertList(getStudentObservationsCall, ObservationResponse.class, "p_id_student", idStudent, "observationsList");
    }

    public Integer addStudentObservation(Integer idStudent, Integer idTeacher, String comment) {
        Map<String, Object> inParams = Map.of(
                "p_id_student", idStudent,
                "p_id_teacher", idTeacher,
                "p_comment", comment.trim()
        );
        Map<String, Object> out = addStudentObservationCall.execute(inParams);
        return (Integer) out.get("o_id_observation");
    }
}
