package com.example.TuVanSinhVien.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Temporary, opt-in diagnostics. Never logs SQL, request data or credentials. */
@Component
@ConditionalOnProperty(name = "app.diagnostics.pool-stalls", havingValue = "true")
public class PoolStallDiagnostics {
    private static final Logger log = LoggerFactory.getLogger(PoolStallDiagnostics.class);
    private final DataSource dataSource;
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "pool-stall-diagnostics");
        thread.setDaemon(true);
        return thread;
    });

    public PoolStallDiagnostics(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void start() {
        timer.scheduleWithFixedDelay(this::inspect, 30, 60, TimeUnit.SECONDS);
    }

    private void inspect() {
        try {
            if (!(dataSource instanceof HikariDataSource hikari)) return;
            var pool = hikari.getHikariPoolMXBean();
            if (pool == null || pool.getThreadsAwaitingConnection() == 0) return;
            log.warn("[POOL-STALL] active={}, idle={}, waiting={}", pool.getActiveConnections(),
                    pool.getIdleConnections(), pool.getThreadsAwaitingConnection());
            for (var thread : ManagementFactory.getThreadMXBean().dumpAllThreads(true, true)) {
                var stack = thread.getStackTrace();
                boolean relevant = Arrays.stream(stack).anyMatch(frame ->
                        frame.getClassName().startsWith("com.microsoft.sqlserver.") ||
                        frame.getClassName().startsWith("org.apache.jasper.") ||
                        frame.getClassName().startsWith("com.example.TuVanSinhVien."));
                if (!relevant) continue;
                log.warn("[POOL-STALL] thread={} state={} lock={} owner={} stack={}",
                        thread.getThreadName(), thread.getThreadState(), thread.getLockName(),
                        thread.getLockOwnerName(), Arrays.toString(stack));
            }
        } catch (Exception ex) {
            log.warn("[POOL-STALL] Diagnostic unavailable ({})", ex.getClass().getSimpleName());
        }
    }

    @PreDestroy
    public void stop() {
        timer.shutdownNow();
    }
}
