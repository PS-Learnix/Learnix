package com.learnix.web.dto;

public record CourseStatsResponse(
        String promedioGeneral,
        String promedioAsistencia,
        String aprobadosPct,
        String riesgoPct
) {
    // Calculamos el desfase del SVG directamente en el backend de forma segura
    public double getSvgDashOffset() {
        try {
            double promedio = Double.parseDouble(promedioGeneral);
            // Aplicamos tu fórmula exacta de Svelte: 471.2 - (471.2 * (promedio / 4.0))
            return 471.2 - (471.2 * (promedio / 4.0));
        } catch (NumberFormatException e) {
            return 471.2; // Fallback si no es un número válido
        }
    }
}