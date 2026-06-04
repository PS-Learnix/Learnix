package com.learnix.web.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnix.web.dto.TeacherProfileCourse;
import com.learnix.web.dto.TeacherStatsResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ProfileRepository extends BaseProcedureRepository {

    private SimpleJdbcCall getTeacherStatsCall;
    private SimpleJdbcCall getTeacherProfileCoursesCall;

    public ProfileRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        super(jdbcTemplate, objectMapper);
    }

    @PostConstruct
    public void init() {
        this.getTeacherStatsCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_teacher_stats");

        this.getTeacherProfileCoursesCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_teacher_profile_courses")
                .returningResultSet("coursesList",
                        new DataClassRowMapper<>(TeacherProfileCourse.class));
    }

    public TeacherStatsResponse getTeacherStats(Integer idTeacher) {
        Map<String, Object> out = getTeacherStatsCall.execute(Map.of("p_id_teacher", idTeacher));
        return new TeacherStatsResponse(
                (Integer) out.get("o_total_courses"),
                (Integer) out.get("o_total_students")
        );
    }

    public List<TeacherProfileCourse> getTeacherProfileCourses(Integer idTeacher) {
        return executeAndConvertList(getTeacherProfileCoursesCall, TeacherProfileCourse.class, "p_id_teacher", idTeacher, "coursesList");
    }
}
