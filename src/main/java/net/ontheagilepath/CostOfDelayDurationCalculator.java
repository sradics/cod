package net.ontheagilepath;

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * Created by sebastianradics on 26.02.17.
 */
public interface CostOfDelayDurationCalculator {
    BigDecimal calculateDurationOverlap(DateTime startDate, BigDecimal overlapDuration, Feature feature);
}
