package net.ontheagilepath;

import net.ontheagilepath.graph.GraphDataBean;
import net.ontheagilepath.graph.GraphDataBeanContainer;
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

    @Override
    public GraphDataBeanContainer calculateWeeklyCostOfDelayForSequence(Feature[] featuresForSequenceCurrentTrial, DateTime startDate){
        if (startDate==null)
            startDate = new DateTime();
        GraphDataBeanContainer graphDataBeanContainer = new GraphDataBeanContainer();
        if (featuresForSequenceCurrentTrial==null)
            return graphDataBeanContainer;
        int weeks = 0;
        for (int i = 0; i < featuresForSequenceCurrentTrial.length; i++) {
            addCalculateCostOfDelayForDuration(weeks,graphDataBeanContainer,featuresForSequenceCurrentTrial,i,i,startDate);

            for (int j=i+1;j<featuresForSequenceCurrentTrial.length;j++){
                addCalculateCostOfDelayForDuration(weeks,graphDataBeanContainer,featuresForSequenceCurrentTrial,j,i,startDate);
            }
            startDate=startDate.plusWeeks(featuresForSequenceCurrentTrial[i].getDurationInWeeks().intValue());
            weeks+=featuresForSequenceCurrentTrial[i].getDurationInWeeks().intValue();
        }
        return graphDataBeanContainer;
    }

    private void addCalculateCostOfDelayForDuration(int weeks,GraphDataBeanContainer graphDataBeanContainer,Feature[] featuresForSequenceCurrentTrial,int indexCod, int indexDuration, DateTime startDate){
        Feature codFeature = featuresForSequenceCurrentTrial[indexCod];
        BigDecimal duration = featuresForSequenceCurrentTrial[indexDuration].getDurationInWeeks();
        int codDuration = costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration,codFeature).intValue();
        for (int i=0;i<codDuration;i++){
            graphDataBeanContainer.addDataBean(new GraphDataBean(
                    codFeature.getCostOfDelayPerWeek().toString(),
                    codFeature.getName(),
                    String.valueOf(weeks+i+1)));
        }
    }

    private BigDecimal calculateCostOfDelayForDuration(Feature[] featuresForSequenceCurrentTrial,int indexCod, int indexDuration, DateTime startDate){
        Feature codFeature = featuresForSequenceCurrentTrial[indexCod];
        BigDecimal duration = featuresForSequenceCurrentTrial[indexDuration].getDurationInWeeks();
        long codDuration = costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration,codFeature).longValue();

        return codFeature.getCostOfDelayPerWeek().multiply(BigDecimal.valueOf(codDuration));
    }
}
