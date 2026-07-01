package com.learnix.web.security;

public record SecurityEvidence(
        String countermeasure,
        String testPerformed,
        String result,
        String status,
        String timestamp,
        String module,
        String observations
) {
}
