package dev.westernpine.pulse.manager;

import dev.westernpine.bettertry.Try;
import dev.westernpine.pipeline.Pipeline;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.properties.IdentityProperties;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;

import static dev.westernpine.pulse.logging.Logger.logger;

public class ManagerWebsocket extends WebSocketClient {

    private Manager manager;

    private Pipeline pipeline;

    public ManagerWebsocket(Manager manager, URI uri, Map<String, String> headers) {
        super(uri, headers);
        this.manager = manager;
        this.pipeline = new Pipeline(this::send, manager::received);
        if(Boolean.parseBoolean(Pulse.identityProperties.get(IdentityProperties.USE_MANAGER)))
            Try.to(this::connect);
    }

    public Manager getManager() {
        return manager;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.info("Connection to manager established!");
    }

    @Override
    public void onMessage(String s) {
        this.pipeline.received(s);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        logger.warning("Disconnected from manager. (%d) %s [Remote: %b]".formatted(i, s, b));
        if(!Pulse.state.isActive())
            return;
        Pulse.scheduler.runLater(() -> {
            logger.info("Reconnecting to manager...");
            Try.to(this::reconnect);
        }, 5000L);
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
        logger.warning(e.getMessage());
        logger.warning("An error occurred with the connection to manager!");
    }
}
