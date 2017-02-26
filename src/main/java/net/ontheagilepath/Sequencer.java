package net.ontheagilepath;

import org.joda.time.DateTime;

import java.util.Collection;

/**
 * Created by sebastianradics on 24.02.17.
 */
public interface Sequencer {
    Feature[] calculateSequence(Collection<Feature> features, DateTime startDate);
}
