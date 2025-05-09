package io.github.alexshamrai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("success")
@Tag("long")
@DisplayName("First Long Running Tests")
public class FirstLongTest extends BaseFakeTest {

    @Test
    @DisplayName("Verify data export to CSV format")
    void firstLongOneSecondTest() throws InterruptedException {
        System.out.println("OneSecondTest");
        Thread.sleep(1000);
    }

    @Test
    @DisplayName("Verify email notification delivery")
    void firstLongHalfSecondTest() throws InterruptedException {
        System.out.println("halfSecondTest");
        Thread.sleep(500);
    }

    @Test
    @DisplayName("Verify batch processing completion")
    void firstLongTwoSecondTest() throws InterruptedException {
        System.out.println("TwoSecondTest");
        Thread.sleep(2000);
    }
}