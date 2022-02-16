package dev.westernpine.pulse.controller;

import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.object.Scheduler;
import dev.westernpine.pulse.Pulse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ControllerFactoryTest {

    public static void main(String[] args) {
        Pulse.main(args);

        new Thread(() -> {

            while(ControllerFactory.getControllers().isEmpty())
                Try.to(() -> Thread.sleep(10000));

            Pulse.scheduler.run(() -> {
                Controller controller = ControllerFactory.getControllers().values().stream().findAny().get();
                String json = ControllerFactory.toJson(controller);

                System.out.println(json);

                controller.destroy(EndCase.BOT_RESTART);
                controller = ControllerFactory.fromJson(json);

                System.out.println(ControllerFactory.toJson(controller));

                System.exit(0);
            });

        }).start();



    }

}