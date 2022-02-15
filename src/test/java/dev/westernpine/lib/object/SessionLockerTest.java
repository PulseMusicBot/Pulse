package dev.westernpine.lib.object;

import dev.westernpine.bettertry.Try;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SessionLockerTest {


    public static void main(String[] args) {
        int port = 9999;
        SessionLocker locker = new SessionLocker(port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Try.to(() -> Thread.sleep(5000));
            locker.unlock();
        }));
        System.out.println("Is Locked: " + locker.lockExists());

        if(locker.lockExists()) {
            locker.lockBlocking(1, TimeUnit.SECONDS);
            System.out.println("Lock aquired!");
        } else {
            locker.lockBlocking(1, TimeUnit.SECONDS);
        }

        new Thread(() -> {
            while(true) {
                Try.to(() -> Thread.sleep(1000));
            }
        }).start();

    }


}