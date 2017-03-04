package net.ontheagilepath;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by sebastianradics on 26.02.17.
 */
public class CostOfDelayDurationCalculatorImplUnitTest {
    private CostOfDelayDurationCalculatorImpl costOfDelayDurationCalculator;
    private Feature feature;
    private DateTime startDate;
    private BigDecimal duration10Weeks;

    @Before
    public void setUp() throws Exception {
        duration10Weeks = BigDecimal.TEN;
        costOfDelayDurationCalculator = new CostOfDelayDurationCalculatorImpl();
        feature = new FeatureBuilder().withCostOfDelayPerWeek(BigDecimal.valueOf(100)).withDurationInWeeks(
                duration10Weeks
        ).build();

        startDate = new DateTime(2017,1,1,12,00);
    }

    @Test
    public void calculateDurationOverlap_FullTime() throws Exception {
        Assert.assertEquals(duration10Weeks,costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration10Weeks,feature));
    }

    @Test
    public void calculateDurationOverlap_ShiftedStartTime() throws Exception {
        DateTime codStartDate = startDate.plusWeeks(2);
        feature.setCostOfDelayStartWeek(codStartDate);
        Assert.assertEquals(BigDecimal.valueOf(8),costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration10Weeks,feature));
    }

    @Test
    public void calculateDurationOverlap_EarlierEnding() throws Exception {
        DateTime codEndDate = startDate.plusWeeks(6);
        feature.setCostOfDelayEndWeek(codEndDate);
        Assert.assertEquals(BigDecimal.valueOf(6),costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration10Weeks,feature));
    }

    @Test
    public void calculateDurationOverlap_EarlierEnding_SlightWeekOverlap() throws Exception {
        DateTime codEndDate = startDate.plusWeeks(6).plusDays(1);
        feature.setCostOfDelayEndWeek(codEndDate);
        Assert.assertEquals(BigDecimal.valueOf(7),costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration10Weeks,feature));
    }

    @Test
    public void calculateDurationOverlap_EarlierEnding_SlightWeekMinusOverlap() throws Exception {
        DateTime codEndDate = startDate.plusWeeks(6).minusDays(4);
        feature.setCostOfDelayEndWeek(codEndDate);
        Assert.assertEquals(BigDecimal.valueOf(6),costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration10Weeks,feature));
    }

    @Test
    public void calculateDurationOverlap_SubInterval() throws Exception {
        DateTime codStartDate = startDate.plusWeeks(2);
        DateTime codEndDate = startDate.plusWeeks(9);
        feature.setCostOfDelayStartWeek(codStartDate);
        feature.setCostOfDelayEndWeek(codEndDate);
        Assert.assertEquals(BigDecimal.valueOf(7),costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration10Weeks,feature));
    }

    @Test
    public void calculateDurationOverlap_SubIntervalOutOfRange() throws Exception {
        DateTime codStartDate = startDate.plusWeeks(duration10Weeks.intValue()+1);
        DateTime codEndDate = startDate.plusWeeks(duration10Weeks.intValue()+5);
        feature.setCostOfDelayStartWeek(codStartDate);
        feature.setCostOfDelayEndWeek(codEndDate);
        Assert.assertEquals(BigDecimal.valueOf(0),costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration10Weeks,feature));
    }

    @Test
    public void calculateDurationOverlap_SubIntervalStartOutOfRange() throws Exception {
        DateTime codStartDate = startDate.minusWeeks(2);
        DateTime codEndDate = startDate.plusWeeks(duration10Weeks.intValue()-5);
        feature.setCostOfDelayStartWeek(codStartDate);
        feature.setCostOfDelayEndWeek(codEndDate);
        Assert.assertEquals(BigDecimal.valueOf(5),costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration10Weeks,feature));
    }

    @Test
    public void calculateDurationOverlap_SubIntervalStartAndEndOutOfRange() throws Exception {
        DateTime codStartDate = startDate.minusWeeks(2);
        DateTime codEndDate = startDate.plusWeeks(duration10Weeks.intValue()+2);
        feature.setCostOfDelayStartWeek(codStartDate);
        feature.setCostOfDelayEndWeek(codEndDate);
        Assert.assertEquals(duration10Weeks.add(BigDecimal.valueOf(0)),costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration10Weeks,feature));
    }

    @Test
    public void calculateDurationOverlap_SubInterval_SameDays() throws Exception {
        DateTime codStartDate = startDate.plusWeeks(9);
        DateTime codEndDate = startDate.plusWeeks(9);
        feature.setCostOfDelayStartWeek(codStartDate);
        feature.setCostOfDelayEndWeek(codEndDate);
        Assert.assertEquals(BigDecimal.valueOf(1),costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration10Weeks,feature));
    }

    @Test
    public void calculateDurationOverlap_ShiftedStartTime_MinusWeeks() throws Exception {
        DateTime codStartDate = startDate.minusWeeks(1);
        feature.setCostOfDelayStartWeek(codStartDate);
        Assert.assertEquals(duration10Weeks.add(BigDecimal.ZERO),costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration10Weeks,feature));
    }

    @Test
    public void calculateDurationOverlap_ShiftedStartTime_OneDayShift() throws Exception {
        DateTime codStartDate = startDate.plusDays(1);
        feature.setCostOfDelayStartWeek(codStartDate);
        Assert.assertEquals(duration10Weeks,costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration10Weeks,feature));
    }

    @Test
    public void calculateDurationOverlap_ShiftedStartTime_6DayShift() throws Exception {
        DateTime codStartDate = startDate.plusDays(6);
        feature.setCostOfDelayStartWeek(codStartDate);
        Assert.assertEquals(duration10Weeks,costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration10Weeks,feature));
    }

    @Test
    public void calculateDurationOverlap_ShiftedStartTime_7DayShift() throws Exception {
        DateTime codStartDate = startDate.plusDays(7);
        feature.setCostOfDelayStartWeek(codStartDate);
        Assert.assertEquals(BigDecimal.valueOf(9),costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration10Weeks,feature));
    }

    @Test
    public void calculateDurationOverlap_ShiftedStartTime_OuterBounds() throws Exception {
        DateTime codStartDate = startDate.plusWeeks(duration10Weeks.intValue());
        feature.setCostOfDelayStartWeek(codStartDate);
        Assert.assertEquals(BigDecimal.valueOf(0),costOfDelayDurationCalculator.calculateDurationOverlap(startDate,duration10Weeks,feature));
    }
}