package net.ontheagilepath;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Created by sebastianradics on 24.02.17.
 */
@Component
public class TotalCostOfDelayCalculatorImpl implements TotalCostOfDelayCalculator {
    @Autowired
    private CostOfDelayDurationCalculator costOfDelayDurationCalculator;

    @Override
    public BigDecimal calculateTotalCostOfDelayForSequence(Feature[] featuresForSequenceCurrentTrial, DateTime startDate){
        if (startDate==null)
            startDate = new DateTime();
        BigDecimal totalCoD = BigDecimal.ZERO;
        for (int i = 0; i < featuresForSequenceCurrentTrial.length; i++) {
            totalCoD = totalCoD.add(calculateCostOfDelayForDuration(featuresForSequenceCurrentTrial,i,i,startDate));

            for (int j=i+1;j<featuresForSequenceCurrentTrial.length;j++){
                totalCoD = totalCoD.add(calculateCostOfDelayForDuration(featuresForSequenceCurrentTrial,j,i,startDate));
            }
            startDate=startDate.plusWeeks(featuresForSequenceCurrentTrial[i].getDurationInWeeks().intValue());
        }
        return totalCoD;
    }

    private BigDecimal calculateCostOfDelayForDuration(Feature[] featuresForSequenceCurrentTrial,int indexCod, int indexDuration, DateTime startDate){
        Feature codFeature = featuresForSequenceCurrentTrial[indexCod];
        BigDecimal duration = featuresForSequenceCurrentTrial[indexDuration].getDurationInWeeks();
        long codDuration = costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration,codFeature).longValue();

        return codFeature.getCostOfDelayPerWeek().multiply(BigDecimal.valueOf(codDuration));
    }
}
