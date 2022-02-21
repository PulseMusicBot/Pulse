package dev.westernpine.pulse.events.system.manager;

import dev.westernpine.eventapi.objects.Event;
import dev.westernpine.pipeline.message.Message;
import dev.westernpine.pulse.manager.ManagerWebsocket;

public class ManagerMessageEvent extends Event {

    private final ManagerWebsocket websocket;
    private final Message message;

    public ManagerMessageEvent(ManagerWebsocket websocket, Message message) {
        this.websocket = websocket;
        this.message = message;
    }

    public ManagerWebsocket getWebsocket() {
        return websocket;
    }

    public Message getMessage() {
        return message;
    }
}
