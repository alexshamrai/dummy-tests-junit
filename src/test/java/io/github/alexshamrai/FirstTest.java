package io.github.alexshamrai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("first")
@Tag("success")
@DisplayName("First Test Suite")
public class FirstTest extends BaseFakeTest {

    @Test
    @DisplayName("Verify user login with valid credentials")
    void firstTestOne() {
        System.out.println("firstTestOne");
    }

    @Test
    @DisplayName("Verify user logout functionality")
    void firstTestTwo() {
        System.out.println("firstTestTwo");
    }

}