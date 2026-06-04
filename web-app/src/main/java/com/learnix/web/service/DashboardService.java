package com.learnix.web.service;

import com.learnix.web.dto.DashboardCourseResponse;
import com.learnix.web.dto.DashboardStatsResponse;
import com.learnix.web.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public List<DashboardCourseResponse> getTeacherCourseList(Integer idTeacher) {
        return dashboardRepository.getTeacherCoursesList(idTeacher);
    }

    public DashboardStatsResponse getDashboardStats(Integer idCoursePeriod) {
        return dashboardRepository.getDashboardStats(idCoursePeriod);
    }


    public DashboardCourseResponse determineSelectedCourse(
            List<DashboardCourseResponse> courseList,
            Integer idCoursePeriod, Integer lastPeriodCookie
    ) {
        if (courseList.isEmpty()) return null;

        if (idCoursePeriod != null) {
            return courseList.stream()
                    .filter(c -> c.idCoursePeriod().equals(idCoursePeriod))
                    .findFirst()
                    .orElse(courseList.getFirst());
        }

        if (lastPeriodCookie != null) {
            return courseList.stream()
                    .filter(c -> c.idCoursePeriod().equals(lastPeriodCookie))
                    .findFirst()
                    .orElse(courseList.getFirst());
        }

        return courseList.getFirst();
    }
}