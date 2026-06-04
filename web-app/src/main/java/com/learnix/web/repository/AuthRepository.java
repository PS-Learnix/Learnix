package com.learnix.web.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnix.web.dto.UserResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class AuthRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    public SimpleJdbcCall authenticateUserCall;

    @PostConstruct
    public void init() {
        this.authenticateUserCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_authenticate_user");
    }

    public UserResponse authenticateUserSp(String email, String password) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_email", email);
        inParams.put("p_password", password);

        Map<String, Object> outParams = authenticateUserCall.execute(inParams);

        if(outParams.get("o_id_user") == null) return null;

        return  objectMapper.convertValue(outParams, UserResponse.class);
    }
}
