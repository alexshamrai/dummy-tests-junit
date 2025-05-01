package io.github.alexshamrai;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("second")
@Tag("success")
public class SecondTest extends BaseFakeTest {

    @Test
    void secondTestOne() {
        System.out.println("secondTestOne");
    }

    @Test
    void secondTestTwo() {
        System.out.println("secondTestTwo");
    }

}