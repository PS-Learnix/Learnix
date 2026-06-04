package com.learnix.web.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnix.web.dto.ActivityResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public class ActivityRepository extends BaseProcedureRepository {

    private SimpleJdbcCall getActivitiesByPeriodCall;
    private SimpleJdbcCall createActivityCall;

    public ActivityRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        super(jdbcTemplate, objectMapper);
    }

    @PostConstruct
    public void init() {
        this.getActivitiesByPeriodCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_activities_by_period")
                .returningResultSet("activitiesList",
                        new DataClassRowMapper<>(ActivityResponse.class));

        this.createActivityCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_create_activity");
    }

    public List<ActivityResponse> getActivitiesByPeriod(Integer idCoursePeriod) {
        return executeAndConvertList(getActivitiesByPeriodCall, ActivityResponse.class, "p_id_course_period", idCoursePeriod, "activitiesList");
    }

    public Integer createActivity(Integer idCoursePeriod, String name, Double weight) {
        Map<String, Object> inParams = Map.of(
                "p_id_course_period", idCoursePeriod,
                "p_name", name.trim(),
                "p_weight", BigDecimal.valueOf(weight)
        );
        Map<String, Object> out = createActivityCall.execute(inParams);
        return (Integer) out.get("o_id_activity");
    }
}
