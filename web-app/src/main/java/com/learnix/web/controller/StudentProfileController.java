package com.learnix.web.controller;

import com.learnix.web.dto.StudentDetailResponse;
import com.learnix.web.dto.StudentProfileCourseDto;
import com.learnix.web.dto.UserResponse;
import com.learnix.web.service.StudentProfileService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @GetMapping("/perfil/{idStudent}")
    public String showStudentProfile(@PathVariable("idStudent") Integer idStudent, HttpSession session, Model model) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);

        StudentDetailResponse student = studentProfileService.getStudentDetailSp(idStudent);
        if (student == null) return "redirect:/actividades";

        model.addAttribute("student", student);
        model.addAttribute("cursos", studentProfileService.getStudentCoursesSp(idStudent));
        model.addAttribute("selectedCourse", null); // Inicialmente vacío en la carga

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
}