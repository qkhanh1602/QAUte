package com.example.TuVanSinhVien.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(-200)
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="app.workflow.policy-migration.enabled",havingValue="true",matchIfMissing=true)
public class WorkflowPolicyInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    public WorkflowPolicyInitializer(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    @Override public void run(ApplicationArguments arguments) throws Exception {
        String sql=new ClassPathResource("db/workflow-policy-20261005.sql").getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        jdbc.execute(sql);
        jdbc.execute(new ClassPathResource("db/forum-20261006.sql").getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
    }
}
