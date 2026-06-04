package com.learnix.web.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnix.web.dto.AttendanceRecord;
import com.learnix.web.dto.StudentResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class AttendanceRepository extends BaseProcedureRepository {

    private SimpleJdbcCall getCourseStudentsCall;
    private SimpleJdbcCall getCourseAttendancesCall;
    private SimpleJdbcCall toggleAttendanceCall;

    public AttendanceRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        super(jdbcTemplate, objectMapper);
    }

    @PostConstruct
    public void init() {
        this.getCourseStudentsCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_course_students")
                .returningResultSet("studentsList",
                        new DataClassRowMapper<>(StudentResponse.class));

        this.getCourseAttendancesCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_course_attendances")
                .returningResultSet("attendancesList",
                        new DataClassRowMapper<>(AttendanceRecord.class));

        this.toggleAttendanceCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_toggle_attendance");
    }

    public List<StudentResponse> getCourseStudents(Integer idCoursePeriod) {
        return executeAndConvertList(getCourseStudentsCall, StudentResponse.class, "p_id_course_period", idCoursePeriod, "studentsList");
    }

    public List<AttendanceRecord> getCourseAttendances(Integer idCoursePeriod) {
        return executeAndConvertList(getCourseAttendancesCall, AttendanceRecord.class, "p_id_course_period", idCoursePeriod, "attendancesList");
    }

    public String toggleAttendance(Integer idStudent, Integer idCoursePeriod, Integer idTeacher, LocalDate date, String currentState) {
        Map<String, Object> inParams = Map.of(
                "p_id_student", idStudent,
                "p_id_course_period", idCoursePeriod,
                "p_id_teacher", idTeacher,
                "p_date", Date.valueOf(date),
                "p_current_state", currentState != null ? currentState.trim() : "null"
        );
        Map<String, Object> out = toggleAttendanceCall.execute(inParams);
        return (String) out.get("o_new_state");
    }
}
