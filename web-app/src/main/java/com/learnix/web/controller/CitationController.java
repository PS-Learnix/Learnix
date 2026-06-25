package com.learnix.web.controller;

import com.learnix.web.dto.MobileDtos.CitationDto;
import com.learnix.web.dto.UserResponse;
import com.learnix.web.service.WebCitationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/citations")
@RequiredArgsConstructor
public class CitationController {

    private final WebCitationService webCitationService;

    private UserResponse getAuthenticatedUser(HttpSession session) {
        return (UserResponse) session.getAttribute("user");
    }

    @GetMapping
    public String listCitations(HttpSession session, Model model) {
        UserResponse user = getAuthenticatedUser(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        List<Map<String, Object>> citations = webCitationService.getTeacherCitations(user.idUser());
        model.addAttribute("citations", citations);

        return "citations_list";
    }

    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model) {
        UserResponse user = getAuthenticatedUser(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("students", webCitationService.getActiveStudents());
        model.addAttribute("sections", webCitationService.getActiveSections());

        return "citations_create";
    }

    @PostMapping("/create")
    public String createCitation(
            @RequestParam(value = "studentId", required = false) Integer studentId,
            @RequestParam(value = "coursePeriodId", required = false) Integer coursePeriodId,
            @RequestParam("title") String title,
            @RequestParam("detail") String detail,
            @RequestParam("scheduledAt") String scheduledAt,
            @RequestParam("mode") String mode,
            @RequestParam(value = "meetingUrl", required = false) String meetingUrl,
            @RequestParam("scope") String scope,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        UserResponse user = getAuthenticatedUser(session);
        if (user == null) return "redirect:/login";

        if (title == null || title.trim().isEmpty() || detail == null || detail.trim().isEmpty() || scheduledAt == null || scheduledAt.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Los campos Título, Detalle y Fecha/Hora son obligatorios.");
            return "redirect:/citations/create";
        }

        try {
            webCitationService.createCitation(
                    studentId, coursePeriodId, user.idUser(), title, detail, scheduledAt, mode, meetingUrl, scope
            );
            redirectAttributes.addFlashAttribute("success", "Citación programada exitosamente.");
            return "redirect:/citations";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al programar la citación: " + e.getMessage());
            return "redirect:/citations/create";
        }
    }

    @GetMapping("/{id}")
    public String viewCitationDetail(
            @PathVariable("id") Integer citationId,
            @RequestParam(value = "parentId", required = false) Integer parentId,
            HttpSession session,
            Model model) {
        UserResponse user = getAuthenticatedUser(session);
        if (user == null) return "redirect:/login";

        CitationDto citation = webCitationService.getCitationDetail(citationId, user.idUser());
        if (citation == null) {
            return "redirect:/citations";
        }

        model.addAttribute("user", user);
        model.addAttribute("citation", citation);
        List<Map<String, Object>> recipients = webCitationService.getCitationRecipients(citationId);
        List<Map<String, Object>> contacts = webCitationService.getCommunicationContacts(citationId);
        Integer selectedParentId = parentId;
        if (selectedParentId == null && !contacts.isEmpty()) {
            Object firstParentId = contacts.get(0).get("parentId");
            if (firstParentId instanceof Number number) {
                selectedParentId = number.intValue();
            }
        }

        model.addAttribute("recipients", recipients);
        model.addAttribute("responseSummary", webCitationService.getCitationResponseSummary(citationId));
        model.addAttribute("contacts", contacts);
        model.addAttribute("selectedParentId", selectedParentId);
        model.addAttribute("messages", selectedParentId == null
                ? List.of()
                : webCitationService.getCitationMessages(citationId, selectedParentId, null));

        return "citations_detail";
    }

    @PostMapping("/{id}/messages")
    public String sendMessage(
            @PathVariable("id") Integer citationId,
            @RequestParam("body") String body,
            @RequestParam("parentId") Integer parentId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        UserResponse user = getAuthenticatedUser(session);
        if (user == null) return "redirect:/login";

        if (body == null || body.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El mensaje no puede estar vacío.");
            return "redirect:/citations/" + citationId + "?parentId=" + parentId;
        }

        try {
            webCitationService.sendTeacherMessage(citationId, body.trim(), user.idUser(), parentId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al enviar mensaje: " + e.getMessage());
        }

        return "redirect:/citations/" + citationId + "?parentId=" + parentId;
    }

    @PostMapping("/{id}/cancel")
    public String cancelCitation(
            @PathVariable("id") Integer citationId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        UserResponse user = getAuthenticatedUser(session);
        if (user == null) return "redirect:/login";

        try {
            webCitationService.cancelCitation(citationId, user.idUser());
            redirectAttributes.addFlashAttribute("success", "La citación ha sido cancelada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar la citación: " + e.getMessage());
        }

        return "redirect:/citations/" + citationId;
    }
}
