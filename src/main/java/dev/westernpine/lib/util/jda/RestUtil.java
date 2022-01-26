package dev.westernpine.lib.util.jda;

import dev.westernpine.bettertry.Try;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.concurrent.CompletableFuture;

import static dev.westernpine.pulse.logging.Logger.logger;

public class RestUtil {

    public static <T> T waitFor(RestAction<T> action) {
        return tryToWaitFor(action)
                .orElse(null);
    }

    public static <T> Try<T> tryToWaitFor(RestAction<T> action) {
        CompletableFuture<T> future = action.submit();
        return Try.to(() -> action.submit().get())
                .onFailure(throwable -> logger.warning("Unable to wait for future object! (%s)".formatted(throwable.getMessage())));
    }

}
