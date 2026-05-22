package com.learnix.web.service;

import com.learnix.web.dto.ActivityResponse;
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
public class ActivityService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public List<ActivityResponse> getActivitiesByPeriodSp(Integer idCoursePeriod) {
        return jdbcTemplate.execute((ConnectionCallback<List<ActivityResponse>>) connection -> {
            CallableStatement cs = connection.prepareCall("CALL public.sp_get_activities_by_period(?, ?)");
            cs.setInt(1, idCoursePeriod);
            cs.registerOutParameter(2, Types.OTHER);
            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                List<ActivityResponse> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new ActivityResponse(
                            rs.getInt("id_activity"),
                            rs.getInt("id_course_period"),
                            rs.getString("name"),
                            rs.getDouble("weight"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
                return list;
            }
        });
    }

    @Transactional
    public Integer createActivitySp(Integer idCoursePeriod, String name, Double weight) {
        return jdbcTemplate.execute((ConnectionCallback<Integer>) connection -> {
            CallableStatement cs = connection.prepareCall("CALL public.sp_create_activity(?, ?, ?, ?)");

            // 1. IN Params
            cs.setInt(1, idCoursePeriod);
            cs.setString(2, name.trim());

            // Convertimos de Double a BigDecimal de forma imperativa para satisfacer al tipo DECIMAL de Postgres
            cs.setBigDecimal(3, BigDecimal.valueOf(weight));

            // 2. OUT Param
            cs.registerOutParameter(4, Types.INTEGER);

            cs.execute();
            return cs.getInt(4);
        });
    }
}