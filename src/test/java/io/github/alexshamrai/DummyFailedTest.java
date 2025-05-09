package io.github.alexshamrai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("dummy")
@Tag("failed")
@DisplayName("Dummy Failed Tests")
public class DummyFailedTest extends BaseFakeTest {

    @Test
    @DisplayName("Verify invalid payment processing")
    void firstFailedTest() {
        System.out.println("DummyFailedTest firstFailedTest()");
        assert false;
    }

    @Test
    @DisplayName("Verify expired session handling")
    void secondFailedTest() {
        System.out.println("DummyFailedTest secondFailedTest()");
        assert false;
    }
}