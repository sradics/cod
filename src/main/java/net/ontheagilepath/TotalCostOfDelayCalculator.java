package net.ontheagilepath;

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * Created by sebastianradics on 24.02.17.
 */
public interface TotalCostOfDelayCalculator {
    BigDecimal calculateTotalCostOfDelayForSequence(Feature[] featuresForSequenceCurrentTrial, DateTime startDate);
}
