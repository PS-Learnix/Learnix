package com.learnix.web.controller;

import com.learnix.web.dto.DashboardCourseResponse;
import com.learnix.web.dto.UserResponse;
import com.learnix.web.service.AttendanceService;
import com.learnix.web.service.ActivityService;
import com.learnix.web.service.DashboardService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final AttendanceService attendanceService; // Reutilizamos el SP de estudiantes
    private final DashboardService dashboardService;   // Reutilizamos el SP de cursos para el layout

    @GetMapping("/actividades")
    public String showActivitiesPage(
            @CookieValue(value = "last_course_period_id", required = false) Integer lastPeriodCookie,
            HttpSession session, Model model) {

        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);

        List<DashboardCourseResponse> teacherCourses = dashboardService.getTeacherCourseList(user.idUser());
        model.addAttribute("teacherCourses", teacherCourses);

        // Respetamos la cookie del último curso period seleccionado para mantener la consistencia
        Integer targetPeriodId = (lastPeriodCookie != null) ? lastPeriodCookie :
                (!teacherCourses.isEmpty() ? teacherCourses.get(0).idCoursePeriod() : null);

        if (targetPeriodId == null) return "actividades";

        DashboardCourseResponse selectedCourse = teacherCourses.stream()
                .filter(c -> c.idCoursePeriod().equals(targetPeriodId)).findFirst().orElse(teacherCourses.get(0));
        model.addAttribute("selectedCourse", selectedCourse);

        // Invocamos los Stored Procedures correspondientes
        model.addAttribute("actividades", activityService.getActivitiesByPeriodSp(targetPeriodId));
        model.addAttribute("estudiantes", attendanceService.getStudentsByPeriodSp(targetPeriodId));

        // Calculamos el periodo/trimestre dinámicamente según el mes actual
        java.time.LocalDate today = java.time.LocalDate.now();
        int month = today.getMonthValue();
        String currentPeriodName;
        if (month >= 3 && month <= 5) {
            currentPeriodName = "1er Trimestre";
        } else if (month >= 6 && month <= 8) {
            currentPeriodName = "2do Trimestre";
        } else {
            currentPeriodName = "3er Trimestre";
        }
        model.addAttribute("currentPeriodName", currentPeriodName);

        return "actividades";
    }

    @PostMapping("/actividades/crear")
    public String createActivity(
            @RequestParam("idCoursePeriod") Integer idCoursePeriod,
            @RequestParam("name") String name,
            @RequestParam("weight") Double weight,
            HttpServletResponse response, Model model) {

        // 1. Inserción mediante Stored Procedure
        activityService.createActivitySp(idCoursePeriod, name, weight);

        // 2. Cargamos la lista actualizada para devolvérsela a HTMX
        model.addAttribute("actividades", activityService.getActivitiesByPeriodSp(idCoursePeriod));

        // 3. Cabecera especial para indicarle a HTMX que limpie el formulario y cierre el modal
        response.setHeader("HX-Trigger", "closeModalEvent");

        // Retornamos únicamente el fragmento de las filas de la tabla
        return "actividades :: activities-table";
    }

}