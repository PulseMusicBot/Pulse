package dev.westernpine.pulse.manager;

import dev.westernpine.bettertry.Try;
import dev.westernpine.eventapi.objects.Listener;
import dev.westernpine.pipeline.message.Message;
import dev.westernpine.pipeline.message.MessageType;
import dev.westernpine.pulse.controller.ControllerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Manager implements Listener {

    private ManagerWebsocket websocket;

    public Manager(String url, String managerToken) throws URISyntaxException {
        URI uri = new URI(url);
        Map<String, String> headers = Map.of("Authorization", managerToken);
        websocket = new ManagerWebsocket(this, uri, headers);
    }

    public void received(Message message) {
        if(message.isRequest()) {
            switch (message.read().toString()) {
                default:
                    websocket.getPipeline().send(message.toRespone().write("error").write("Unknown request!"));
                    break;
            }
        } else if (message.isMessage()) {
            switch (message.read().toString()) {
                case "user.premium.update":
                    String userId = message.read().toString();
                    boolean premium = message.read(Boolean.class);
                    ControllerFactory.getControllers()
                            .values()
                            .stream()
                            .filter(controller -> controller.getGuild().getOwnerId().equals(userId))
                            .forEach(controller -> controller.setPremium(premium));
                    break;
                default:
                    StringBuilder builder = new StringBuilder();
                    while(message.hasNext())
                        builder.append(message.read().toString() + "\n");
                    System.out.println("Received Unknown Message: " + builder.toString());
                    break;
            }
        }
    }

    public boolean isPremium(String userId) {
        if(this.websocket.isClosed())
            return false;

        Message response = Try.to(() -> this.websocket.getPipeline().send(new Message().withType(MessageType.REQUEST).write("user.premium").write(userId)).get(5, TimeUnit.SECONDS)).orElse(null);
        if(response == null)
            return false;

        return response.read(Boolean.class);
    }
}
