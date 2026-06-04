package com.learnix.web.controller;

import com.learnix.web.dto.LoginRequest;
import com.learnix.web.dto.UserResponse;
import com.learnix.web.repository.AuthRepository;
import com.learnix.web.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private AuthRepository authRepository;

    // Carga inicial de la página de Login
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        // Pasamos una instancia vacía del Record para enlazar el formulario de Thymeleaf
        model.addAttribute("loginRequest", new LoginRequest("", "", false));
        return "login";
    }

    // Acción procesada asíncronamente por HTMX
    @PostMapping("/login")
    public String processLogin(
            @Valid @ModelAttribute("loginRequest") LoginRequest loginRequest,
            BindingResult bindingResult,
            HttpSession session,
            HttpServletResponse response,
            Model model) {

        // 1. Validación de reglas de Jakarta Validation (reemplaza a Zod)
        if (bindingResult.hasErrors()) {
            return "login :: login-form"; // Devuelve solo el fragmento del formulario
        }

        try {
            // 2. Autenticación imperativa usando el Stored Procedure mediante el servicio
            // Nota el uso de la sintaxis de métodos del Record: .email() y .password()


            UserResponse user = authRepository.authenticateUserSp(loginRequest.email(), loginRequest.password());

            // 3. Verificación del resultado del SP
            if (user == null) {
                model.addAttribute("globalError", "Credenciales incorrectas");
                return "login :: login-form";
            }

            // 4. Inyección del Record de usuario en la Sesión Http (Manejo riguroso del estado en Backend)
            session.setAttribute("user", user);

            if (loginRequest.rememberMe()) {
                // Si el docente marcó "Remember me", extendemos la sesión (Ej: 1 semana)
                session.setMaxInactiveInterval(60 * 60 * 24 * 7);
            }

            // 5. Redirección gestionada por HTMX en el cliente hacia el Dashboard
            response.setHeader("HX-Redirect", "/dashboard");
            return null;

        } catch (Exception e) {
            e.printStackTrace();

            model.addAttribute("globalError", e.getMessage());

            return "login :: login-form";
        }

    }

    // Cierre de sesión e invalidación del estado
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}