package com.cryptopulse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CryptoPulseApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the Spring ApplicationContext initializes without error
    }
}
