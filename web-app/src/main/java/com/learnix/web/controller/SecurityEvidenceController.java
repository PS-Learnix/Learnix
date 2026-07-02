package com.learnix.web.controller;

import com.learnix.web.dto.UserResponse;
import com.learnix.web.security.SecurityEvidence;
import com.learnix.web.security.SecurityEvidenceService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SecurityEvidenceController {

    private final SecurityEvidenceService securityEvidenceService;

    @GetMapping("/security/evidences")
    public String evidences(HttpSession session, Model model) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<SecurityEvidence> evidences = securityEvidenceService.buildEvidenceReport();
        model.addAttribute("user", user);
        model.addAttribute("evidences", evidences);
        model.addAttribute("approvedCount", evidences.stream().filter(e -> "APROBADO".equals(e.status())).count());
        return "security_evidences";
    }

    @GetMapping("/security/evidences.json")
    @ResponseBody
    public ResponseEntity<List<SecurityEvidence>> evidencesJson(HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(securityEvidenceService.buildEvidenceReport());
    }
}
