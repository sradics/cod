package net.ontheagilepath;

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * Created by sebastianradics on 21.02.17.
 */
public class FeatureBuilder {
    private BigDecimal durationInWeeks = BigDecimal.ZERO;
    private BigDecimal costOfDelayPerWeek;
    private String name = "";
    private DateTime costOfDelayStartDate;
    private DateTime costOfDelayEndDate;

    public FeatureBuilder withDurationInWeeks(BigDecimal duration){
        this.durationInWeeks = duration;
        return this;
    }

    public FeatureBuilder withDurationAndCostOfDelayPeriod(BigDecimal durationInWeeks, DateTime costOfDelayStartDate, DateTime costOfDelayEndDate){
        this.durationInWeeks = durationInWeeks;
        this.costOfDelayStartDate = costOfDelayStartDate;
        this.costOfDelayEndDate = costOfDelayEndDate;
        return this;
    }

    public FeatureBuilder withDurationAndCostOfDelayStartDate(BigDecimal durationInWeeks, DateTime costOfDelayStartDate){
        this.durationInWeeks = durationInWeeks;
        this.costOfDelayStartDate = costOfDelayStartDate;
        return this;
    }


    public FeatureBuilder withCostOfDelayPerWeek(BigDecimal costOfDelayPerWeek){
        this.costOfDelayPerWeek = costOfDelayPerWeek;
        return this;
    }

    public FeatureBuilder withName (String name){
        this.name = name;
        return this;
    }

    public Feature build(){
        Feature feature = new Feature(durationInWeeks);
        feature.setCostOfDelayPerWeek(costOfDelayPerWeek);
        feature.setName(name);
        feature.setCostOfDelayStartWeek(costOfDelayStartDate);
        feature.setCostOfDelayEndWeek(costOfDelayEndDate);
        return feature;
    }
}
