package io.github.alexshamrai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("dummy")
@Tag("failed")
public class DummyFailedTest extends BaseFakeTest {

    @Test
    void firstFailedTest() {
        System.out.println("DummyFailedTest firstFailedTest()");
        assert false;
    }

    @Test
    @DisplayName("Second failed test")
    void secondFailedTest() {
        System.out.println("DummyFailedTest secondFailedTest()");
        assert false;
    }
}