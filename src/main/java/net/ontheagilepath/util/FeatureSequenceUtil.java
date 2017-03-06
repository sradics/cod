package net.ontheagilepath.util;

import net.ontheagilepath.Feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by sebastianradics on 06.03.17.
 */
public class FeatureSequenceUtil {
    public static Feature[] getSequenceFromLabels(String labels, Feature[] features){
        HashMap<String,Feature> featureMap = new HashMap<String,Feature>();
        for (Feature feature : features) {
            featureMap.put(feature.getName(),feature);
        }
        StringTokenizer stok = new StringTokenizer(labels,",");
        ArrayList<Feature> featureSequence = new ArrayList<Feature>();
        while(stok.hasMoreTokens()){
            Feature feature = featureMap.get(stok.nextToken().trim());
            if (feature!=null)
                featureSequence.add(feature);
        }
        return featureSequence.toArray(new Feature[]{});
    }
}
