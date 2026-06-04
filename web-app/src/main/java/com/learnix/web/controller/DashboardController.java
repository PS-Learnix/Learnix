package com.learnix.web.controller;

import com.learnix.web.dto.DashboardCourseResponse;
import com.learnix.web.dto.DashboardStatsResponse;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String showDashboard(
            @RequestParam(value = "idCoursePeriod", required = false) Integer idCoursePeriod,
            @CookieValue(value = "last_course_period_id", required = false) Integer lastPeriodCookie,
            @RequestHeader(value = "HX-Request", required = false) String hxRequest,
            HttpSession session,
            HttpServletResponse response,
            Model model
    ) {
        UserResponse user = (UserResponse) session.getAttribute("user");

        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);

        List<DashboardCourseResponse> courseList = dashboardService.getTeacherCourseList(user.idUser());
        model.addAttribute("teacherCourse", courseList);

        if (courseList.isEmpty()) return "views/dashboard/index";

        DashboardCourseResponse selectedCourse = dashboardService.determineSelectedCourse(
                courseList, idCoursePeriod, lastPeriodCookie
        );

        Cookie cookie = new Cookie("last_course_period_id", String.valueOf(selectedCourse.idCoursePeriod()));
        cookie.setPath("/");
        cookie.setMaxAge(60*60*24*7);
        response.addCookie(cookie);

        DashboardStatsResponse stats = dashboardService.getDashboardStats(selectedCourse.idCoursePeriod());
        model.addAttribute("stats", stats);

        if(hxRequest != null) return "views/dashboard/index :: #dashboard-content";

        return "views/dashboard/index";
    }
}