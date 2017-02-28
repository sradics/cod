package net.ontheagilepath;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sebastianradics on 27.02.17.
 */
public class FeatureSequenceModel {
    private ArrayList<Feature> features = new ArrayList<Feature>();

    public void addFeature(String name, String costOfDelayPerWeek, String durationInWeeks, String startWeek, String endWeek, String projectStart) {
        DateTime projectStartDate = DateTime.parse(projectStart, DateTimeFormat.forPattern("dd.MM.yyyy"));
        Feature feature = new FeatureBuilder()
                .withName(name)
                .withDurationInWeeks(BigDecimal.valueOf(Long.valueOf(durationInWeeks)))
                .withCostOfDelayPerWeek(new Feature.CostOfDelayPerWeek(BigDecimal.valueOf(Long.valueOf(costOfDelayPerWeek))))
                .build();

        if (startWeek!=null && !startWeek.isEmpty()){
            feature.setCostOfDelayStartDate(projectStartDate.plusWeeks(Integer.valueOf(startWeek)));
        }
        if (endWeek!=null && !endWeek.isEmpty()){
            feature.setCostOfDelayEndDate(projectStartDate.plusWeeks(Integer.valueOf(endWeek)));
        }

        features.add(feature);
    }

    public List<Feature> getFeatures(){
        return Collections.unmodifiableList(features);
    }

}
