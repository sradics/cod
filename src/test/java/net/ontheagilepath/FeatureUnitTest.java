package net.ontheagilepath;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;


public class FeatureUnitTest {
    @Test
    public void testCD3Calculation(){
        Feature feature = new FeatureBuilder().withDurationInWeeks(BigDecimal.TEN).withCostOfDelayPerWeek(
                BigDecimal.valueOf(1000)).build();

        assertEquals(BigDecimal.valueOf(100),feature.calculateCD3());

    }

    @Test
    public void testCD3CalculationZero(){
        Feature feature = new FeatureBuilder().withDurationInWeeks(BigDecimal.ZERO).withCostOfDelayPerWeek(
                BigDecimal.valueOf(1000)).build();

        assertEquals(BigDecimal.valueOf(0),feature.calculateCD3());

        feature = new FeatureBuilder().withDurationInWeeks(BigDecimal.ZERO).withCostOfDelayPerWeek(
                BigDecimal.valueOf(0)).build();
        assertEquals(BigDecimal.valueOf(0),feature.calculateCD3());

    }

}
