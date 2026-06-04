package com.learnix.web.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class BaseProcedureRepository {

    protected final JdbcTemplate jdbcTemplate;
    protected final ObjectMapper objectMapper;

    protected  <T> T executeAndConvert(
            SimpleJdbcCall call, Class<T> targetClass,
            String paramName, Object paramValue
    ) {
        Map<String, Object> inParams = Collections.singletonMap(paramName, paramValue);
        Map<String, Object> outParams = call.execute(inParams);

        return objectMapper.convertValue(outParams, targetClass);
    }

    protected  <T> T executeAndConvert(
            SimpleJdbcCall call, Class<T> targetClass,
            Map<String, Object> inParams
    ) {
        Map<String, Object> outParams = call.execute(inParams);

        return objectMapper.convertValue(outParams, targetClass);
    }

    protected  <T> List<T> executeAndConvertList(
            SimpleJdbcCall call, Class<T> targetClass,
            String paramName, Object paramValue, String resultSetKey
    ) {
        Map<String, Object> inParams = Collections.singletonMap(paramName, paramValue);
        Map<String, Object> outParams = call.execute(inParams);

        return objectMapper.convertValue(
                outParams.get(resultSetKey),
                objectMapper.getTypeFactory().constructCollectionType(
                        List.class, targetClass
                )
        );
    }

    protected  <T> List<T> executeAndConvertList(
            SimpleJdbcCall call, Class<T> targetClass,
            Map<String, Object> inParams, String resultSetKey
    ) {
        Map<String, Object> outParams = call.execute(inParams);

        return objectMapper.convertValue(
                outParams.get(resultSetKey),
                objectMapper.getTypeFactory().constructCollectionType(
                        List.class, targetClass
                )
        );
    }
}
