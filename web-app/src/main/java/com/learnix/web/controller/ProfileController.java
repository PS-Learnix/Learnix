package com.learnix.web.controller;

import com.learnix.web.dto.UserResponse;
import com.learnix.web.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/perfil")
    public String showTeacherProfile(HttpSession session, Model model) {
        // Validación de sesión
        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);

        // Carga imperativa de Stored Procedures
        model.addAttribute("stats", profileService.getTeacherStatsSp(user.idUser()));
        model.addAttribute("myCourses", profileService.getTeacherProfileCoursesSp(user.idUser()));

        return "perfil";
    }
}