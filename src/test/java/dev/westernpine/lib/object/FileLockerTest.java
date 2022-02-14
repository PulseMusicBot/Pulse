package dev.westernpine.lib.object;

import dev.westernpine.bettertry.Try;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.File;

class FileLockerTest {

    public static void main(String[] args) {
        String filePath = "locker.tmp";
        FileLocker locker = new FileLocker(filePath);
        assert !FileLocker.isLocked(new File(filePath));
        assert locker.lock().isLocked();
        Try.to(() -> Thread.sleep(5000));
        assert !locker.unlock().isLocked();
        System.out.println("Locker test completed.");
    }
}