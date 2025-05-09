package io.github.alexshamrai;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("dummy")
@Tag("disabled")
@DisplayName("Dummy Disabled Tests")
public class DummyDisabledTest extends BaseFakeTest {

    @Test
    @Disabled("Feature not implemented yet")
    @DisplayName("Verify legacy system integration")
    void firstDisabledTest() {
        System.out.println("DummyDisabledTest firstFailedTest()");
    }

    @Test
    @Disabled("Pending infrastructure setup")
    @DisplayName("Verify third-party service callback")
    void secondDisabledTest() {
        System.out.println("DummyDisabledTest secondDisabledTest()");
    }
}