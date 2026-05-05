package com.creditflow.applications.application;

import com.creditflow.shared.domain.ApplicationNumber;
import java.time.Clock;
import java.time.Year;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ApplicationNumberGenerator {

    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    public ApplicationNumberGenerator(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    public ApplicationNumber next() {
        Long sequenceValue = jdbcTemplate.queryForObject("select nextval('application_number_seq')", Long.class);
        int year = Year.now(clock).getValue();
        return ApplicationNumber.of("CFM-%d-%06d".formatted(year, sequenceValue));
    }
}
