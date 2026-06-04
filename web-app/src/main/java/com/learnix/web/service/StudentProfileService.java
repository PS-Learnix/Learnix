package com.learnix.web.service;

import com.learnix.web.dto.ObservationResponse;
import com.learnix.web.dto.StudentDetailResponse;
import com.learnix.web.dto.StudentProfileCourseDto;
import com.learnix.web.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final JdbcTemplate jdbcTemplate;
    private final StudentProfileRepository studentProfileRepository;

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

    public List<StudentProfileCourseDto> getStudentCoursesSp(Integer idStudent) {
        return studentProfileRepository.getStudentCourses(idStudent);
    }

    public List<ObservationResponse> getStudentObservationsSp(Integer idStudent) {
        return studentProfileRepository.getStudentObservations(idStudent);
    }

    @Transactional
    public Integer addStudentObservationSp(Integer idStudent, Integer idTeacher, String comment) {
        return studentProfileRepository.addStudentObservation(idStudent, idTeacher, comment);
    }
}