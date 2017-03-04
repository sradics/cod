package net.ontheagilepath;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.math.BigDecimal;

/**
 * Created by sebastianradics on 21.02.17.
 */
public class Feature {
    private BigDecimal durationInWeeks = BigDecimal.ZERO;
    private BigDecimal costOfDelayPerWeek;
    private String name="";
    private DateTime costOfDelayStartDate;
    private DateTime costOfDelayEndDate;

    public Feature(BigDecimal durationInWeeks) {
        this.durationInWeeks = durationInWeeks;
    }



    public void setDurationInWeeks(String weeks){
        durationInWeeks = BigDecimal.valueOf(Long.valueOf((String)weeks));
    }
    public void setCostOfDelayStartWeek(DateTime projectStartDate, String startWeek){
        costOfDelayStartDate = projectStartDate.plusWeeks(Integer.valueOf(startWeek));
    }
    public void setCostOfDelayEndWeek(DateTime projectStartDate, String endWeek){
        costOfDelayEndDate = projectStartDate.plusWeeks(Integer.valueOf(endWeek));
    }
    public void setCostOfDelayStartDate(String startDate){
        costOfDelayStartDate = DateTime.parse(startDate, DateTimeFormat.forPattern("dd.MM.yyyy"));
    }

    public void setCostOfDelayStartDate(DateTime startDate){
        costOfDelayStartDate = startDate;
    }


    public void setCostOfDelayEndDate( DateTime endDate){
        costOfDelayEndDate = endDate;
    }

    public void setCostOfDelayEndDate( String endDate){
        costOfDelayEndDate = DateTime.parse(endDate, DateTimeFormat.forPattern("dd.MM.yyyy"));
    }
    public void setCostOfDelayPerWeek(String cod){
        costOfDelayPerWeek = BigDecimal.valueOf(Long.valueOf((String)cod));
    }

    public BigDecimal getDurationInWeeks() {
        return durationInWeeks;
    }

    public void setDurationInWeeks(BigDecimal durationInWeeks) {
        this.durationInWeeks = durationInWeeks;
    }

    public BigDecimal getCostOfDelayPerWeek() {
        return costOfDelayPerWeek;
    }

    public void setCostOfDelayPerWeek(BigDecimal costOfDelayPerWeek) {
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

    public void setCostOfDelayStartWeek(DateTime costOfDelayStartDate) {
        this.costOfDelayStartDate = costOfDelayStartDate;
    }

    public DateTime getCostOfDelayEndDate() {
        return costOfDelayEndDate;
    }

    public void setCostOfDelayEndWeek(DateTime costOfDelayEndDate) {
        this.costOfDelayEndDate = costOfDelayEndDate;
    }

    public BigDecimal calculateCD3(){
        if (durationInWeeks.compareTo(BigDecimal.ZERO)==0)
            return BigDecimal.ZERO;
        return costOfDelayPerWeek.divide(durationInWeeks,BigDecimal.ROUND_HALF_UP);
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
