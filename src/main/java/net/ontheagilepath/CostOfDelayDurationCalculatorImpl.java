package net.ontheagilepath;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CostOfDelayDurationCalculatorImpl implements CostOfDelayDurationCalculator {
    @Override
    public BigDecimal calculateDurationOverlap(DateTime startDate, BigDecimal overlapDuration, Feature feature){
        if (feature.getCostOfDelayStartDate()==null && feature.getCostOfDelayEndDate()==null)
            return overlapDuration;
        DateTime intervalStartDate = startDate;
        Interval intervalForFeature = new Interval(intervalStartDate,startDate.plusWeeks(overlapDuration.intValue()));


        Interval codInterval = new Interval(feature.getCostOfDelayStartDate()!=null?feature.getCostOfDelayStartDate():startDate,
                feature.getCostOfDelayEndDate()!=null?feature.getCostOfDelayEndDate():startDate.plusWeeks(feature.getDurationInWeeks().intValue()));
        Interval overlap = intervalForFeature.overlap(codInterval);

        if (overlap==null) {
            return BigDecimal.ZERO;
        }

        if (overlap.toDuration().getStandardDays()==0) //Same day start/stop of cost of delay
            return BigDecimal.ONE;

        return BigDecimal.valueOf(overlap.toDuration().getStandardDays()).
                divide(BigDecimal.valueOf(7),BigDecimal.ROUND_UP);
    }
}
