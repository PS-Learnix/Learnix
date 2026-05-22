package com.learnix.web.dto;

public record StudentProfileCourseDto(
        Integer idCoursePeriod,
        String courseName,
        Double promedio
) {
    // Convierte la escala 4.0 a base 20 o la que prefieras para la UI
    public int getNotaBaseVente() {
        return (int) Math.round(promedio * 5);
    }

    // Obtiene el porcentaje de avance para el width de la barra de progreso
    public int getPercentageWidth() {
        return (int) Math.round((promedio / 4.0) * 100);
    }
}