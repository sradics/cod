package net.ontheagilepath;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan(excludeFilters={
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value=FeatureSequenceGUI.class),
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value=App.class)})
public class SequenceUnitTest {

    @Autowired
    private Sequencer sequencer = new SequencerImpl();

    @Autowired
    private TotalCostOfDelayCalculator totalCostOfDelayCalculator;


    @Test
    public void testCalculateSequence(){
        assertNotNull(sequencer.calculateSequence(new ArrayList<Feature>(), null));
    }

    @Test
    public void testCalculateSequenceOneFeature(){
        Feature feature = new Feature(BigDecimal.ONE);

        assertEquals(feature,sequencer.calculateSequence(Arrays.asList(new Feature[]{feature}), null)[0]);
    }

    @Test
    public void testCalculateSequenceTwoFeaturesDifferentCoDWithSameDuration(){
        Feature feature1 =
                new FeatureBuilder()
                        .withDurationInWeeks(BigDecimal.ONE)
                        .withCostOfDelayPerWeek(new Feature.CostOfDelayPerWeek(BigDecimal.TEN))
                .build();
        Feature feature2 =
                new FeatureBuilder()
                        .withDurationInWeeks(BigDecimal.ONE)
                        .withCostOfDelayPerWeek(new Feature.CostOfDelayPerWeek(BigDecimal.valueOf(200)))
                        .build();

        Feature[] result = sequencer.calculateSequence(Arrays.asList(new Feature[]{
            feature1, feature2
        }), null);
        assertEquals(feature2,result[0]);
        assertEquals(feature1,result[1]);
    }

    @Test
    public void testCalculateSequenceNFeaturesDifferentCoDWithSameDuration(){
        Feature feature1 =
                new FeatureBuilder()
                        .withDurationInWeeks(BigDecimal.ONE)
                        .withCostOfDelayPerWeek(new Feature.CostOfDelayPerWeek(BigDecimal.TEN))
                        .build();
        Feature feature2 =
                new FeatureBuilder()
                        .withDurationInWeeks(BigDecimal.ONE)
                        .withCostOfDelayPerWeek(new Feature.CostOfDelayPerWeek(BigDecimal.valueOf(200)))
                        .build();

        Feature feature3 =
                new FeatureBuilder()
                        .withDurationInWeeks(BigDecimal.ONE)
                        .withCostOfDelayPerWeek(new Feature.CostOfDelayPerWeek(BigDecimal.valueOf(150)))
                        .build();

        Feature feature4 =
                new FeatureBuilder()
                        .withDurationInWeeks(BigDecimal.ONE)
                        .withCostOfDelayPerWeek(new Feature.CostOfDelayPerWeek(BigDecimal.valueOf(8)))
                        .build();

        Feature[] result = sequencer.calculateSequence(Arrays.asList(new Feature[]{
                feature1, feature2,feature3, feature4
        }), null);
        assertEquals(feature2,result[0]);
        assertEquals(feature3,result[1]);
        assertEquals(feature1,result[2]);
        assertEquals(feature4,result[3]);
    }

    @Test
    public void testCalculateCoD(){
        DateTime dt = DateTime.now();
        Feature feature1 =
                new FeatureBuilder()
                        .withDurationInWeeks(BigDecimal.ONE)
                        .withName("A")
                        .withCostOfDelayPerWeek(new Feature.CostOfDelayPerWeek(BigDecimal.valueOf(20)))
                        .withDurationAndCostOfDelayPeriod(BigDecimal.ONE,dt.plusWeeks(3),dt.plusWeeks(6))
                        .build();
        Feature feature2 =
                new FeatureBuilder()
                        .withDurationInWeeks(BigDecimal.ONE)
                        .withName("B")
                        .withCostOfDelayPerWeek(new Feature.CostOfDelayPerWeek(BigDecimal.valueOf(12)))
                        .build();
        BigDecimal result = totalCostOfDelayCalculator.calculateTotalCostOfDelayForSequence(new Feature[]{feature1},dt);
        Feature[] resultSeq = sequencer.calculateSequence(Arrays.asList(new Feature[]{
                feature1, feature2
        }), dt);

        System.out.println(resultSeq[0].toString()+" "+resultSeq[1].toString());
        assertEquals(BigDecimal.ZERO,result);

    }


}
