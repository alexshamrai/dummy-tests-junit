package io.github.alexshamrai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("dummy")
@Tag("success")
@DisplayName("Dummy Success Tests")
public class DummySuccessTest extends BaseFakeTest {

    @Test
    @DisplayName("Verify API health check endpoint")
    void firstSuccessTest() {
        System.out.println("DummySuccessTest firstSuccessTest()");
    }

    @Test
    @DisplayName("Verify database connection status")
    void secondSuccessTest() {
        System.out.println("DummySuccessTest secondSuccessTest()");
    }
}