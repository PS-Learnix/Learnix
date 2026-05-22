package com.learnix.web.service;

import com.learnix.web.dto.AttendanceRecord;
import com.learnix.web.dto.StudentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByPeriodSp(Integer idCoursePeriod) {
        return jdbcTemplate.execute((ConnectionCallback<List<StudentResponse>>) connection -> {
            CallableStatement cs = connection.prepareCall("CALL public.sp_get_course_students(?, ?)");
            cs.setInt(1, idCoursePeriod);
            cs.registerOutParameter(2, Types.OTHER);
            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                List<StudentResponse> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new StudentResponse(
                            rs.getInt("id_student"), rs.getString("first_name"),
                            rs.getString("last_name"), rs.getString("dni")
                    ));
                }
                return list;
            }
        });
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecord> getAttendancesByPeriodSp(Integer idCoursePeriod) {
        return jdbcTemplate.execute((ConnectionCallback<List<AttendanceRecord>>) connection -> {
            CallableStatement cs = connection.prepareCall("CALL public.sp_get_course_attendances(?, ?)");
            cs.setInt(1, idCoursePeriod);
            cs.registerOutParameter(2, Types.OTHER);
            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                List<AttendanceRecord> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new AttendanceRecord(
                            rs.getInt("id_student"),
                            rs.getDate("date").toLocalDate(),
                            rs.getBoolean("did_attend")
                    ));
                }
                return list;
            }
        });
    }

    @Transactional
    public String toggleAttendanceSp(Integer idStudent, Integer idCoursePeriod, Integer idTeacher, LocalDate date, String currentState) {
        return jdbcTemplate.execute((ConnectionCallback<String>) connection -> {
            CallableStatement cs = connection.prepareCall("CALL public.sp_toggle_attendance(?, ?, ?, ?, ?, ?)");
            cs.setInt(1, idStudent);
            cs.setInt(2, idCoursePeriod);
            cs.setInt(3, idTeacher);
            cs.setDate(4, Date.valueOf(date));
            cs.setString(5, currentState);
            cs.registerOutParameter(6, Types.VARCHAR);

            cs.execute();
            return cs.getString(6); // Devuelve el nuevo estado ('true', 'false', 'null')
        });
    }
}