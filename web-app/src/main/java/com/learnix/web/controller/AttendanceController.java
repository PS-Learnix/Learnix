package com.learnix.web.controller;

import com.learnix.web.dto.*;
import com.learnix.web.service.AttendanceService;
import com.learnix.web.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final DashboardService dashboardService; // Reutilizamos para el Layout Lateral

    @GetMapping("/asistencia")
    public String showAttendancePage(
            @RequestParam(value = "id", required = false) Integer idCoursePeriod,
            HttpSession session, Model model) {

        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);

        // Barra lateral: obtener cursos
        List<DashboardCourseResponse> teacherCourses = dashboardService.getTeacherCourseList(user.idUser());
        model.addAttribute("teacherCourses", teacherCourses);

        // Fallback: Si no viene ID de curso, seleccionamos el primero disponible
        Integer targetPeriodId = idCoursePeriod;
        if (targetPeriodId == null && !teacherCourses.isEmpty()) {
            targetPeriodId = teacherCourses.get(0).idCoursePeriod();
        }

        if (targetPeriodId == null) {
            return "asistencia";
        }

        // Obtener curso seleccionado para el título
        Integer finalTargetPeriodId = targetPeriodId;
        DashboardCourseResponse selectedCourse = teacherCourses.stream()
                .filter(c -> c.idCoursePeriod().equals(finalTargetPeriodId))
                .findFirst().orElse(teacherCourses.get(0));
        model.addAttribute("selectedCourse", selectedCourse);
        List<StudentResponse> students = attendanceService.getStudentsByPeriodSp(targetPeriodId);
        List<AttendanceRecord> attendances = attendanceService.getAttendancesByPeriodSp(targetPeriodId);

// Generamos los días de la semana de forma dinámica basándonos en la fecha actual
        LocalDate today = LocalDate.now();
        List<DayOfWeekDto> days = new ArrayList<>();
        String[] dayNames = {"Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};

        int[] offsets = {-2, -1, 0, 1, 2};
        for (int offset : offsets) {
            LocalDate currentDay = today;
            if (offset != 0) {
                int step = offset > 0 ? 1 : -1;
                int absoluteOffset = Math.abs(offset);
                int count = 0;
                while (count < absoluteOffset) {
                    currentDay = currentDay.plusDays(step);
                    if (currentDay.getDayOfWeek().getValue() < 6) { // Lunes a Viernes
                        count++;
                    }
                }
            }
            int dayOfWeekValue = currentDay.getDayOfWeek().getValue() % 7;
            String name = dayNames[dayOfWeekValue];
            days.add(new DayOfWeekDto(name, currentDay, currentDay.equals(today)));
        }
        model.addAttribute("diasSemana", days);

        // Formateamos el mes de inicio y fin para mostrarlo de forma destacada en la cabecera
        LocalDate firstDay = days.get(0).date();
        LocalDate lastDay = days.get(4).date();
        String mesInicio = firstDay.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("es"));
        String mesFin = lastDay.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("es"));
        String año = String.valueOf(lastDay.getYear());
        String mesLabel = mesInicio.equalsIgnoreCase(mesFin)
                ? mesInicio.substring(0, 1).toUpperCase() + mesInicio.substring(1) + " " + año
                : mesInicio.substring(0, 1).toUpperCase() + mesInicio.substring(1) + " / " + mesFin.substring(0, 1).toUpperCase() + mesFin.substring(1) + " " + año;
        model.addAttribute("mesLabel", mesLabel);

// Construimos las filas procesadas cruzando los datos en el Backend
        List<StudentAttendanceRow> matrixRows = new ArrayList<>();

        for (StudentResponse est : students) {
            List<String> states = new ArrayList<>();

            // Para cada día de la semana, buscamos si hay asistencia registrada
            for (DayOfWeekDto day : days) {
                AttendanceRecord match = attendances.stream()
                        .filter(a -> a.idStudent().equals(est.idStudent()) && a.date().equals(day.date()))
                        .findFirst()
                        .orElse(null);

                if (match == null) {
                    states.add("null");
                } else {
                    states.add(match.didAttend() ? "true" : "false");
                }
            }

            matrixRows.add(new StudentAttendanceRow(est.idStudent(), est.firstName(), est.lastName(), states));
        }

        // Pasamos la matriz ya digerida a la vista
        model.addAttribute("matrixRows", matrixRows);

        // Cargar datos de los Stored Procedures
        model.addAttribute("estudiantes", attendanceService.getStudentsByPeriodSp(targetPeriodId));
        model.addAttribute("asistencias", attendanceService.getAttendancesByPeriodSp(targetPeriodId));


        return "asistencia";
    }

    // Acción POST invocada asíncronamente por HTMX para un solo botón
    @PostMapping("/asistencia/toggle")
    public String toggleAttendance(
            @RequestParam("idStudent") Integer idStudent,
            @RequestParam("idCoursePeriod") Integer idCoursePeriod,
            @RequestParam("date") String dateStr,
            @RequestParam("currentState") String currentState,
            HttpSession session, Model model) {

        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null) return "auth/login";

        LocalDate date = LocalDate.parse(dateStr);

        // Invocamos el SP que conmuta los estados
        String newState = attendanceService.toggleAttendanceSp(idStudent, idCoursePeriod, user.idUser(), date, currentState);

        // Pasamos variables parciales al modelo para renderizar EXCLUSIVAMENTE el botón modificado
        model.addAttribute("idStudent", idStudent);
        model.addAttribute("idCoursePeriod", idCoursePeriod);
        model.addAttribute("date", date);
        model.addAttribute("newState", newState);
        model.addAttribute("isToday", date.equals(LocalDate.now()));

        return "asistencia :: attendance-btn";
    }
}