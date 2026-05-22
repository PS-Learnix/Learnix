package com.learnix.web.service;

import com.learnix.web.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.sql.Types;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public UserResponse authenticateSp(String email, String rawPassword) {

        return jdbcTemplate.execute(connection -> {
            CallableStatement cs = connection.prepareCall(
                    "CALL public.sp_authenticate_user(?, ?, ?, ?, ?, ?)"
            );

            // IN params
            cs.setString(1, email.trim());
            cs.setString(2, rawPassword.trim());

            // OUT params
            cs.registerOutParameter(3, Types.INTEGER);
            cs.registerOutParameter(4, Types.VARCHAR);
            cs.registerOutParameter(5, Types.VARCHAR);
            cs.registerOutParameter(6, Types.VARCHAR);

            return cs;

        }, (CallableStatement cs) -> {

            cs.execute();

            Integer idUser = cs.getInt(3);

            // Cuando no encuentra usuario
            if (cs.wasNull()) {
                return null;
            }

            return new UserResponse(
                    idUser,
                    cs.getString(4),
                    cs.getString(5),
                    cs.getString(6)
            );
        });
    }
}