package io.github.alexshamrai;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("success")
@Tag("long")
public class SecondLongTest extends BaseFakeTest {

    @Test
    void secondLongOneSecondTest() throws InterruptedException {
        System.out.println("OneSecondTest");
        Thread.sleep(1000);
    }

    @Test
    void secondLongHalfSecondTest() throws InterruptedException {
        System.out.println("halfSecondTest");
        Thread.sleep(500);
    }

    @Test
    void secondLongTwoSecondTest() throws InterruptedException {
        System.out.println("TwoSecondTest");
        Thread.sleep(2000);
    }
}