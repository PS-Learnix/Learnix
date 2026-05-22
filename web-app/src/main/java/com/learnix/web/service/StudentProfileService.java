package com.learnix.web.service;

import com.learnix.web.dto.StudentDetailResponse;
import com.learnix.web.dto.StudentProfileCourseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public StudentDetailResponse getStudentDetailSp(Integer idStudent) {
        String sql = "SELECT id_student, first_name, last_name, dni, birth_date FROM students WHERE id_student = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new StudentDetailResponse(
                    rs.getInt("id_student"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("dni"),
                    rs.getDate("birth_date") != null ? rs.getDate("birth_date").toLocalDate() : null
            ), idStudent);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<StudentProfileCourseDto> getStudentCoursesSp(Integer idStudent) {
        return jdbcTemplate.execute((ConnectionCallback<List<StudentProfileCourseDto>>) connection -> {
            CallableStatement cs = connection.prepareCall("CALL public.sp_get_student_dashboard_courses(?, ?)");
            cs.setInt(1, idStudent);
            cs.registerOutParameter(2, Types.OTHER);
            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                List<StudentProfileCourseDto> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new StudentProfileCourseDto(
                            rs.getInt("id_course_period"),
                            rs.getString("course_name"),
                            rs.getDouble("promedio")
                    ));
                }
                return list;
            }
        });
    }
}