package io.github.alexshamrai;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag( "first")
@Tag("success")
public class FirstTest extends BaseFakeTest {

    @Test
    void firstTestOne() {
        System.out.println("firstTestOne");
    }

    @Test
    void firstTestTwo() {
        System.out.println("firstTestTwo");
    }

}