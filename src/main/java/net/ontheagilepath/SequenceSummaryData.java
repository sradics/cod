package net.ontheagilepath;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebastianradics on 24.02.17.
 */
public class SequenceSummaryData {
    private BigDecimal totalCostOfDelay;
    private List<String> featureSequence = new ArrayList<String>();

    public BigDecimal getTotalCostOfDelay() {
        return totalCostOfDelay;
    }

    public void setTotalCostOfDelay(BigDecimal totalCostOfDelay) {
        this.totalCostOfDelay = totalCostOfDelay;
    }

    public List<String> getFeatureSequence() {
        return featureSequence;
    }

    public void setFeatureSequence(List<String> featureSequence) {
        this.featureSequence = featureSequence;
    }

    public SequenceSummaryData(BigDecimal totalCostOfDelay, List<String> featureSequence) {
        this.totalCostOfDelay = totalCostOfDelay;
        this.featureSequence = featureSequence;
    }

    public SequenceSummaryData(BigDecimal totalCostOfDelay, Feature[] features) {
        this.totalCostOfDelay = totalCostOfDelay;
        for (Feature feature : features) {
            featureSequence.add(feature.getName());
        }
    }

    @Override
    public String toString() {
        return "totalCostOfDelay=" + totalCostOfDelay +
                ", featureSequence=" + featureSequence;
    }
}
