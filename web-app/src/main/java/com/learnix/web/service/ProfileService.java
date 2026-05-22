package com.learnix.web.service;

import com.learnix.web.dto.TeacherProfileCourse;
import com.learnix.web.dto.TeacherStatsResponse;
import lombok.RequiredArgsConstructor;
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
public class ProfileService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public TeacherStatsResponse getTeacherStatsSp(Integer idTeacher) {
        return jdbcTemplate.execute((ConnectionCallback<TeacherStatsResponse>) connection -> {
            CallableStatement cs = connection.prepareCall("CALL public.sp_get_teacher_stats(?, ?, ?)");
            cs.setInt(1, idTeacher);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();

            return new TeacherStatsResponse(cs.getInt(2), cs.getInt(3));
        });
    }

    @Transactional(readOnly = true)
    public List<TeacherProfileCourse> getTeacherProfileCoursesSp(Integer idTeacher) {
        return jdbcTemplate.execute((ConnectionCallback<List<TeacherProfileCourse>>) connection -> {
            CallableStatement cs = connection.prepareCall("CALL public.sp_get_teacher_profile_courses(?, ?)");
            cs.setInt(1, idTeacher);
            cs.registerOutParameter(2, Types.OTHER);
            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                List<TeacherProfileCourse> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new TeacherProfileCourse(
                            rs.getInt("id_course"),
                            rs.getString("name"),
                            rs.getInt("total_sections")
                    ));
                }
                return list;
            }
        });
    }
}