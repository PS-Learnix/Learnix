package com.learnix.web.controller;

import com.learnix.web.dto.*;
import com.learnix.web.service.DashboardService;
import com.learnix.web.service.ReportService;
import com.learnix.web.service.StudentProfileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService studentProfileService;
    private final ReportService reportService;
    private final DashboardService dashboardService;

    @GetMapping("/perfil/{idStudent}")
    public String showStudentProfile(@PathVariable("idStudent") Integer idStudent, HttpSession session, Model model) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);

        StudentDetailResponse student = studentProfileService.getStudentDetailSp(idStudent);
        if (student == null) return "redirect:/actividades";

        model.addAttribute("student", student);
        model.addAttribute("cursos", studentProfileService.getStudentCoursesSp(idStudent));
        model.addAttribute("selectedCourse", null);
        model.addAttribute("observaciones", studentProfileService.getStudentObservationsSp(idStudent));

        return "estudiante_perfil";
    }

    // Parcial dinámico gatillado asíncronamente por HTMX
    @GetMapping("/perfil/curso-detalle")
    public String getCourseDetail(
            @RequestParam("idCoursePeriod") Integer idCoursePeriod,
            @RequestParam("courseName") String courseName,
            Model model) {

        // Creamos un DTO temporal o pasamos los datos para pintar el panel derecho
        model.addAttribute("courseName", courseName);

        return "estudiante_perfil :: right-panel-detail";
    }

    @PostMapping("/perfil/{idStudent}/observacion")
    public String addObservation(
            @PathVariable("idStudent") Integer idStudent,
            @RequestParam("comment") String comment,
            HttpSession session, HttpServletResponse response, Model model) {

        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null) return "auth/login";

        // 1. Inserción estricta mediante el Procedure
        studentProfileService.addStudentObservationSp(idStudent, user.idUser(), comment);

        // 2. Volvemos a consultar la lista actualizada para refrescar la UI
        model.addAttribute("observaciones", studentProfileService.getStudentObservationsSp(idStudent));

        // 3. Mandamos cabecera para obligar al navegador a limpiar el textarea de forma nativa
        response.setHeader("HX-Trigger", "clearCommentBox");

        // Retornamos únicamente el fragmento que envuelve el listado de comentarios
        return "estudiante_perfil :: observations-timeline";
    }
    @GetMapping("/perfil/{idStudent}/reporte")
    public String generateStudentReport(
            @PathVariable("idStudent") Integer idStudent,
            @RequestParam("idCoursePeriod") Integer idCoursePeriod,
            HttpSession session, Model model) {

        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // Cargamos los datos básicos de la boleta
        StudentDetailResponse student = studentProfileService.getStudentDetailSp(idStudent);
        List<ReportCardItem> grades = reportService.getStudentReportCardSp(idStudent, idCoursePeriod);

        // Obtenemos los cursos generales para extraer el nombre de la materia actual
        List<CourseDashboardResponse> teacherCourses = dashboardService.getTeacherCoursesSp(user.idUser());
        CourseDashboardResponse currentCourse = teacherCourses.stream()
                .filter(c -> c.idCoursePeriod().equals(idCoursePeriod)).findFirst().orElse(null);

        model.addAttribute("student", student);
        model.addAttribute("reportItems", grades);
        model.addAttribute("course", currentCourse);
        model.addAttribute("teacher", user);

        return "reporte_imprimible"; // Nueva plantilla limpia
    }
}