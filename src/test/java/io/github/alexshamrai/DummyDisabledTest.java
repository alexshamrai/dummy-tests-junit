package io.github.alexshamrai;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("dummy")
@Tag("disabled")
public class DummyDisabledTest extends BaseFakeTest {

    @Test
    @Disabled
    void firstDisabledTest() {
        System.out.println("DummyDisabledTest firstFailedTest()");
    }

    @Test
    @Disabled
    void secondDisabledTest() {
        System.out.println("DummyDisabledTest secondDisabledTest()");
    }
}