package net.ontheagilepath.util;

import net.ontheagilepath.Feature;
import net.ontheagilepath.FeatureBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by sebastianradics on 06.03.17.
 */
public class FeatureSequenceUtilUnitTest {
    private Feature[] features = {
            new FeatureBuilder().withName("B").build(),
            new FeatureBuilder().withName("C").build(),
            new FeatureBuilder().withName("A").build(),

    };

    @Test
    public void getSequenceFromLabels() throws Exception {
        String sequence = "A,B,C";

        Feature[] result = FeatureSequenceUtil.getSequenceFromLabels(sequence,features);
        assertEquals("A",result[0].getName());
        assertEquals("B",result[1].getName());
        assertEquals("C",result[2].getName());
    }

    @Test
    public void getSequenceFromLabels_MissingParts() throws Exception {
        String sequence = "A,B";

        Feature[] result = FeatureSequenceUtil.getSequenceFromLabels(sequence,features);
        assertEquals("A",result[0].getName());
        assertEquals("B",result[1].getName());
        assertEquals(2,result.length);
    }

    @Test
    public void getSequenceFromLabels_TooManyParts() throws Exception {
        String sequence = "A,B,X,C";

        Feature[] result = FeatureSequenceUtil.getSequenceFromLabels(sequence,features);
        assertEquals("A",result[0].getName());
        assertEquals("B",result[1].getName());
        assertEquals("C",result[2].getName());
        assertEquals(3,result.length);
    }

}