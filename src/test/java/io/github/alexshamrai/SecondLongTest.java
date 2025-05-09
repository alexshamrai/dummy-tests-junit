package io.github.alexshamrai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("success")
@Tag("long")
@DisplayName("Second Long Running Tests")
public class SecondLongTest extends BaseFakeTest {

    @Test
    @DisplayName("Verify report generation with large dataset")
    void secondLongOneSecondTest() throws InterruptedException {
        System.out.println("OneSecondTest");
        Thread.sleep(1000);
    }

    @Test
    @DisplayName("Verify cache invalidation across nodes")
    void secondLongHalfSecondTest() throws InterruptedException {
        System.out.println("halfSecondTest");
        Thread.sleep(500);
    }

    @Test
    @DisplayName("Verify full database migration")
    void secondLongTwoSecondTest() throws InterruptedException {
        System.out.println("TwoSecondTest");
        Thread.sleep(2000);
    }
}