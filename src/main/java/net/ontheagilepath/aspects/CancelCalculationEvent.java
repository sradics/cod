package net.ontheagilepath.aspects;

import org.springframework.context.ApplicationEvent;

/**
 * Created by sebastianradics on 12.03.17.
 */
public class CancelCalculationEvent extends ApplicationEvent {
    public CancelCalculationEvent(Object source) {
        super(source);
    }
}
