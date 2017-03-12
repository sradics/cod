package net.ontheagilepath.aspects;

import org.springframework.context.ApplicationEvent;

/**
 * Created by sebastianradics on 12.03.17.
 */
public class MessageEvent extends ApplicationEvent {
    private String message;
    public MessageEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
