package com.learnix.web.controller;

import com.learnix.web.dto.*;
import com.learnix.web.service.GradeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    // Simula la carga de la ruta [idActivity] de SvelteKit
    @GetMapping("/actividades/{idActivity}")
    public String showActivityDetailPage(@PathVariable("idActivity") Integer idActivity, HttpSession session, Model model) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);

        model.addAttribute("actividad", gradeService.getActivityDetailSp(idActivity));
        model.addAttribute("listaNotas", gradeService.getActivityGradesSp(idActivity));
        model.addAttribute("stats", gradeService.getActivityStatsSp(idActivity));

        return "actividad_detalle";
    }

    // Endpoint atómico procesado por HTMX
    @PostMapping("/actividades/guardar-nota")
    public String saveGrade(
            @RequestParam("idStudent") Integer idStudent,
            @RequestParam("idActivity") Integer idActivity,
            @RequestParam("value") String value,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            HttpServletResponse response, Model model) {

        // 1. Persistencia estricta en base de datos
        gradeService.saveStudentGradeSp(idStudent, idActivity, value);

        // 2. Preparamos el objeto para devolver de forma aislada la fila de la tabla modificada
        StudentGradeResponse updatedRow = new StudentGradeResponse(idStudent, firstName, lastName, value);
        model.addAttribute("item", updatedRow);

        // 3. Despachamos un evento para obligar a la barra lateral de estadísticas a recalcularse
        response.setHeader("HX-Trigger", "refreshStatsEvent");

        return "actividad_detalle :: grade-row";
    }

    @GetMapping("/actividades/{idActivity}/stats")
    public String refreshStats(@PathVariable("idActivity") Integer idActivity, Model model) {
        model.addAttribute("idActivity", idActivity); // Variable de respaldo para el hx-get del fragmento
        model.addAttribute("stats", gradeService.getActivityStatsSp(idActivity));
        return "actividad_detalle :: stats-aside";
    }
}