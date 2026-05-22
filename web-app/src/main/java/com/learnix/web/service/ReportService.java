package com.learnix.web.service;

import com.learnix.web.dto.ReportCardItem;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public List<ReportCardItem> getStudentReportCardSp(Integer idStudent, Integer idCoursePeriod) {
        return jdbcTemplate.execute((ConnectionCallback<List<ReportCardItem>>) connection -> {
            // Usamos la sintaxis estándar de llamada de funciones/procedimientos con retorno de cursor
            CallableStatement cs = connection.prepareCall("CALL public.sp_get_student_report_card(?, ?, ?)");
            cs.setInt(1, idStudent);
            cs.setInt(2, idCoursePeriod);

            // CORRECCIÓN: Registramos el parámetro explícitamente como REF_CURSOR para Postgres
            cs.registerOutParameter(3, Types.REF_CURSOR);
            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(3)) {
                List<ReportCardItem> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new ReportCardItem(
                            rs.getString("activity_name"),
                            rs.getDouble("activity_weight"),
                            rs.getString("grade_value")
                    ));
                }
                return list;
            }
        });
    }
}