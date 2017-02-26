package net.ontheagilepath;

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * Created by sebastianradics on 21.02.17.
 */
public class Feature {
    private BigDecimal durationInWeeks = BigDecimal.ZERO;
    private CostOfDelayPerWeek costOfDelayPerWeek;
    private String name="";
    private DateTime costOfDelayStartDate;
    private DateTime costOfDelayEndDate;

    public Feature(BigDecimal durationInWeeks) {
        this.durationInWeeks = durationInWeeks;
    }

    public static class CostOfDelayPerWeek {
        private BigDecimal cost = BigDecimal.ZERO;

        public CostOfDelayPerWeek(BigDecimal cost) {
            this.cost = cost;
        }

        public BigDecimal getCost() {
            return cost;
        }

        public void setCost(BigDecimal cost) {
            this.cost = cost;
        }

        @Override
        public String toString() {
            return "CostOfDelayPerWeek{" +
                    "cost=" + cost +
                    '}';
        }
    }

    public BigDecimal getDurationInWeeks() {
        return durationInWeeks;
    }

    public void setDurationInWeeks(BigDecimal durationInWeeks) {
        this.durationInWeeks = durationInWeeks;
    }

    public CostOfDelayPerWeek getCostOfDelayPerWeek() {
        return costOfDelayPerWeek;
    }

    public void setCostOfDelayPerWeek(CostOfDelayPerWeek costOfDelayPerWeek) {
        this.costOfDelayPerWeek = costOfDelayPerWeek;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getCostOfDelayStartDate() {
        return costOfDelayStartDate;
    }

    public void setCostOfDelayStartDate(DateTime costOfDelayStartDate) {
        this.costOfDelayStartDate = costOfDelayStartDate;
    }

    public DateTime getCostOfDelayEndDate() {
        return costOfDelayEndDate;
    }

    public void setCostOfDelayEndDate(DateTime costOfDelayEndDate) {
        this.costOfDelayEndDate = costOfDelayEndDate;
    }

    public BigDecimal calculateCD3(){
        if (durationInWeeks.compareTo(BigDecimal.ZERO)==0)
            return BigDecimal.ZERO;
        return costOfDelayPerWeek.getCost().divide(durationInWeeks,BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public String toString() {
        return "Feature{" +
                "costOfDelayPerWeek=" + costOfDelayPerWeek +
                ", costOfDelayEndDate=" + costOfDelayEndDate +
                ", costOfDelayStartDate=" + costOfDelayStartDate +
                ", durationInWeeks=" + durationInWeeks +
                ", name='" + name + '\'' +
                '}';
    }
}
