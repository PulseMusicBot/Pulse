package dev.westernpine.lib.object;

import dev.westernpine.bettertry.Try;

import java.io.File;

class FileLockerTest {

    public static void main(String[] args) {
        String filePath = "locker.tmp";
        FileLocker locker = new FileLocker(filePath);
        Runtime.getRuntime().addShutdownHook(new Thread(locker::unlock));
        System.out.println("Is Locked: " + FileLocker.isLocked(new File(filePath)));
        if (locker.isLocked()) {
            locker.lockBlocking();
            System.out.println("Lock aquired!");
        } else {
            locker.lockBlocking();
        }

        new Thread(() -> {
            while (true) {
                Try.to(() -> Thread.sleep(1000));
            }
        }).start();

    }
}