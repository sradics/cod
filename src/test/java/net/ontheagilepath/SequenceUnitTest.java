package net.ontheagilepath;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SequenceUnitTest {

    @Autowired
    private Sequencer sequencer = new SequencerImpl();


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


}
