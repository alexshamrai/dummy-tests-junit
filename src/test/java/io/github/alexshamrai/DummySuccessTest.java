package io.github.alexshamrai;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("dummy")
@Tag("success")
public class DummySuccessTest extends BaseFakeTest {

    @Test
    void firstSuccessTest() {
        System.out.println("DummySuccessTest firstSuccessTest()");
    }

    @Test
    void secondSuccessTest() {
        System.out.println("DummySuccessTest secondSuccessTest()");
    }
}