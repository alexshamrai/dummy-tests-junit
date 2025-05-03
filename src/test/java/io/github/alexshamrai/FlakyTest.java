package io.github.alexshamrai;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@Tag("flaky")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FlakyTest extends BaseFakeTest {

    @Test
    public void flakyTestPassesOnSecondRun() {
        File counterFile = new File("build/tmp/flakyTestCounter.txt");
        int runCounter = 1;

        try {
            counterFile.getParentFile().mkdirs();

            if (counterFile.exists()) {
                Scanner scanner = new Scanner(counterFile);
                if (scanner.hasNextInt()) {
                    runCounter = scanner.nextInt() + 1;
                }
                scanner.close();
            }

            try (FileWriter writer = new FileWriter(counterFile)) {
                writer.write(String.valueOf(runCounter));
            }

            System.out.println("Running test attempt #" + runCounter);

            if (runCounter == 1) {
                fail("This test is designed to fail on the first run");
            } else {
                assertTrue(true, "Test passed on attempt #" + runCounter);
            }
        } catch (Exception e) {
            System.out.println("Error handling counter file: " + e.getMessage());
        }
    }
}