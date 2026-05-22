package com.learnix.web.service;

import com.learnix.web.dto.CourseDashboardResponse;
import com.learnix.web.dto.CourseStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public List<CourseDashboardResponse> getTeacherCoursesSp(Integer idTeacher) {
        return jdbcTemplate.execute((ConnectionCallback<List<CourseDashboardResponse>>) connection -> {

            // Invocamos el PROCEDURE de forma explícita con la sintaxis estandarizada
            CallableStatement cs = connection.prepareCall("CALL public.sp_get_teacher_courses(?, ?)");

            // IN: ID del profesor
            cs.setInt(1, idTeacher);

            // OUT: El REFCURSOR de Postgres se registra como Types.OTHER
            cs.registerOutParameter(2, Types.OTHER);

            cs.execute();

            // Recuperamos el objeto del parámetro OUT y lo casteamos directamente a ResultSet
            // El driver de Postgres automáticamente convierte el REFCURSOR activo a un ResultSet almacenable
            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                List<CourseDashboardResponse> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new CourseDashboardResponse(
                            rs.getInt("id_course"),
                            rs.getString("name"),
                            rs.getInt("id_course_period"),
                            rs.getString("section")
                    ));
                }
                return list;
            }
        });
    }

    @Transactional(readOnly = true)
    public CourseStatsResponse getCourseStatsSp(Integer idCoursePeriod) {
        return jdbcTemplate.execute((ConnectionCallback<CourseStatsResponse>) connection -> {
            double promedio = 0.0;
            int asistencia = 0;
            int aprobados = 0;
            int riesgo = 0;

            // 1. Promedio General (Uso de BigDecimal obligatorio para Types.NUMERIC)
            try (CallableStatement cs1 = connection.prepareCall("CALL public.sp_promedio_general_curso(?, ?)")) {
                cs1.setInt(1, idCoursePeriod);
                cs1.registerOutParameter(2, Types.NUMERIC);
                cs1.execute();

                BigDecimal bdPromedio = cs1.getBigDecimal(2);
                if (bdPromedio != null) {
                    promedio = bdPromedio.doubleValue(); // Convertimos de forma segura a double
                }
            }

            // 2. Asistencia
            try (CallableStatement cs2 = connection.prepareCall("CALL public.sp_promedio_asistencia_curso(?, ?)")) {
                cs2.setInt(1, idCoursePeriod);
                cs2.registerOutParameter(2, Types.NUMERIC);
                cs2.execute();

                BigDecimal bdAsistencia = cs2.getBigDecimal(2);
                if (bdAsistencia != null) {
                    asistencia = bdAsistencia.intValue(); // Convertimos de forma segura a int
                }
            }

            // 3. Estado Académico (Aprobados y Riesgo)
            try (CallableStatement cs3 = connection.prepareCall("CALL public.sp_estudiantes_riesgo_exito(?, ?, ?)")) {
                cs3.setInt(1, idCoursePeriod);
                cs3.registerOutParameter(2, Types.NUMERIC);
                cs3.registerOutParameter(3, Types.NUMERIC);
                cs3.execute();

                BigDecimal bdAprobados = cs3.getBigDecimal(2);
                BigDecimal bdRiesgo = cs3.getBigDecimal(3);

                if (bdAprobados != null) aprobados = bdAprobados.intValue();
                if (bdRiesgo != null) riesgo = bdRiesgo.intValue();
            }

            return new CourseStatsResponse(
                    String.format("%.2f", promedio),
                    String.valueOf(asistencia),
                    String.valueOf(aprobados),
                    String.valueOf(riesgo)
            );
        });
    }
}