package net.ontheagilepath.aspects;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Observable;

/**
 * Created by sebastianradics on 12.03.17.
 */
@Component
public class SummaryFinishedListener extends Observable implements ApplicationListener<MessageEvent> {
    @Override
    public void onApplicationEvent(MessageEvent event) {
        setChanged();
        notifyObservers(event);


    }


}
