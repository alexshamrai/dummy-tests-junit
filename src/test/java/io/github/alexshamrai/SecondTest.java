package io.github.alexshamrai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("second")
@Tag("success")
@DisplayName("Second Test Suite")
public class SecondTest extends BaseFakeTest {

    @Test
    @DisplayName("Verify user profile update")
    void secondTestOne() {
        System.out.println("secondTestOne");
    }

    @Test
    @DisplayName("Verify user password change")
    void secondTestTwo() {
        System.out.println("secondTestTwo");
    }

}