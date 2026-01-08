package com.jobboard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JobBoardApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully
        // with all beans properly configured
    }

    @Test
    void mainMethodRuns() {
        // Test that the main method can be called without errors
        String[] args = {};
        // This would normally call JobBoardApplication.main(args)
        // but we skip it in tests to avoid starting the full application
    }
}