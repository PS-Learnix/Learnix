package com.learnix.web.service;

import com.learnix.web.dto.AttendanceRecord;
import com.learnix.web.dto.StudentResponse;
import com.learnix.web.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public List<StudentResponse> getStudentsByPeriodSp(Integer idCoursePeriod) {
        return attendanceRepository.getCourseStudents(idCoursePeriod);
    }

    public List<AttendanceRecord> getAttendancesByPeriodSp(Integer idCoursePeriod) {
        return attendanceRepository.getCourseAttendances(idCoursePeriod);
    }

    @Transactional
    public String toggleAttendanceSp(Integer idStudent, Integer idCoursePeriod, Integer idTeacher, LocalDate date, String currentState) {
        return attendanceRepository.toggleAttendance(idStudent, idCoursePeriod, idTeacher, date, currentState);
    }
}