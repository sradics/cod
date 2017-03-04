package net.ontheagilepath;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by sebastianradics on 24.02.17.
 */
public class TotalCostOfDelayCalculatorUnitTest {
    @InjectMocks
    private TotalCostOfDelayCalculatorImpl totalCostOfDelayCalculator;

    @Mock
    private CostOfDelayDurationCalculator costOfDelayDurationCalculator;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCalculateTotalCostOfDelayForSequence(){
        Feature feature1 =
                new FeatureBuilder()
                        .withDurationInWeeks(BigDecimal.valueOf(8))
                        .withCostOfDelayPerWeek(BigDecimal.TEN)
                        .build();
        Feature feature2 =
                new FeatureBuilder()
                        .withDurationInWeeks(BigDecimal.TEN)
                        .withCostOfDelayPerWeek(BigDecimal.valueOf(200))
                        .build();
        when(costOfDelayDurationCalculator.calculateDurationOverlap(any(), eq(BigDecimal.valueOf(8)), any())).thenReturn(BigDecimal.valueOf(8));
        when(costOfDelayDurationCalculator.calculateDurationOverlap(any(),eq(BigDecimal.valueOf(10)),any())).thenReturn(BigDecimal.valueOf(10));
        assertEquals(BigDecimal.valueOf(10*8+200*8+200*10),totalCostOfDelayCalculator.calculateTotalCostOfDelayForSequence(new Feature[]{feature1,feature2}, null));
    }

    @Test
    public void testCalculateTotalCostOfDelayForSequenceOneEntry(){
        Feature feature1 =
                new FeatureBuilder()
                        .withDurationInWeeks(BigDecimal.valueOf(8))
                        .withCostOfDelayPerWeek(BigDecimal.TEN)
                        .build();
        when(costOfDelayDurationCalculator.calculateDurationOverlap(any(), eq(BigDecimal.valueOf(8)), any())).thenReturn(BigDecimal.valueOf(8));

        assertEquals(BigDecimal.valueOf(10*8),totalCostOfDelayCalculator.calculateTotalCostOfDelayForSequence(new Feature[]{feature1}, null));
    }

    @Test
    public void testCalculateTotalCostOfDelayForSequence3Entries(){
        Feature feature1 =
                new FeatureBuilder()
                        .withDurationInWeeks(BigDecimal.valueOf(8))
                        .withCostOfDelayPerWeek(BigDecimal.TEN)
                        .build();
        Feature feature2 =
                new FeatureBuilder()
                        .withDurationInWeeks(BigDecimal.TEN)
                        .withCostOfDelayPerWeek(BigDecimal.valueOf(200))
                        .build();
        Feature feature3 =
                new FeatureBuilder()
                        .withDurationInWeeks(BigDecimal.valueOf(2))
                        .withCostOfDelayPerWeek(BigDecimal.valueOf(800))
                        .build();
        when(costOfDelayDurationCalculator.calculateDurationOverlap(any(), eq(BigDecimal.valueOf(8)), any())).thenReturn(BigDecimal.valueOf(8));
        when(costOfDelayDurationCalculator.calculateDurationOverlap(any(), eq(BigDecimal.valueOf(10)), any())).thenReturn(BigDecimal.valueOf(10));
        when(costOfDelayDurationCalculator.calculateDurationOverlap(any(), eq(BigDecimal.valueOf(2)), any())).thenReturn(BigDecimal.valueOf(2));

        assertEquals(BigDecimal.valueOf(10*8+200*8+800*8+200*10+800*10+800*2),
                totalCostOfDelayCalculator.calculateTotalCostOfDelayForSequence(new Feature[]{feature1,feature2,feature3}, null));
    }
}
