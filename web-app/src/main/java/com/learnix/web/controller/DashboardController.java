package com.learnix.web.controller;

import com.learnix.web.dto.CourseDashboardResponse;
import com.learnix.web.dto.CourseStatsResponse;
import com.learnix.web.dto.UserResponse;
import com.learnix.web.service.DashboardService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String showDashboard(
            @RequestParam(value = "courseId", required = false) Integer courseId,
            @RequestParam(value = "coursePeriodId", required = false) Integer coursePeriodId,
            @CookieValue(value = "last_course_period_id", required = false) Integer lastPeriodCookie,
            HttpSession session,
            HttpServletResponse response,
            Model model) {

        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);

        List<CourseDashboardResponse> courses = dashboardService.getTeacherCoursesSp(user.idUser());
        model.addAttribute("teacherCourses", courses);

        if (courses.isEmpty()) return "dashboard";

        // Determinamos la sección exacta usando el ID del Periodo del Curso
        CourseDashboardResponse selectedCourse = null;

        if (coursePeriodId != null) {
            Integer finalPeriodId = coursePeriodId;
            selectedCourse = courses.stream()
                    .filter(c -> c.idCoursePeriod().equals(finalPeriodId))
                    .findFirst()
                    .orElse(null);
        } else if (lastPeriodCookie != null) {
            selectedCourse = courses.stream()
                    .filter(c -> c.idCoursePeriod().equals(lastPeriodCookie))
                    .findFirst()
                    .orElse(null);
        }

        // Fallback: Si no hay selección previa, tomamos el primer elemento de la lista
        if (selectedCourse == null) {
            selectedCourse = courses.get(0);
        }

        model.addAttribute("selectedCourse", selectedCourse);

        // Guardamos el ID del periodo del curso en la cookie para persistencia
        Cookie cookie = new Cookie("last_course_period_id", String.valueOf(selectedCourse.idCoursePeriod()));
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 7);
        response.addCookie(cookie);

        // Carga de estadísticas del SP usando el ID de la sección actual
        CourseStatsResponse stats = dashboardService.getCourseStatsSp(selectedCourse.idCoursePeriod());
        model.addAttribute("stats", stats);

        return "dashboard";
    }
}