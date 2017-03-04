package net.ontheagilepath;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Created by sebastianradics on 27.02.17.
 */
public class FeatureSequenceModel {
    private ObservableList<Feature> features = FXCollections.observableArrayList();

    public void addFeature(String name, String costOfDelayPerWeek, String durationInWeeks, String startWeek, String endWeek, String startDate, String endDate, String projectStart) {
        DateTime projectStartDate = DateTime.parse(projectStart, DateTimeFormat.forPattern("dd.MM.yyyy"));
        Feature feature = new FeatureBuilder()
                .withName(name)
                .build();
        feature.setDurationInWeeks(durationInWeeks);
        feature.setCostOfDelayPerWeek(costOfDelayPerWeek);
        if (startWeek!=null && !startWeek.isEmpty()){
            feature.setCostOfDelayStartWeek(projectStartDate,startWeek);
        }
        if (endWeek!=null && !endWeek.isEmpty()){
            feature.setCostOfDelayEndWeek(projectStartDate,endWeek);
        }

        if (startDate!=null && !startDate.isEmpty()){
            feature.setCostOfDelayStartWeek(DateTime.parse(startDate, DateTimeFormat.forPattern("dd.MM.yyyy")));
        }
        if (endDate!=null && !endDate.isEmpty()){
            feature.setCostOfDelayEndWeek(DateTime.parse(endDate, DateTimeFormat.forPattern("dd.MM.yyyy")));
        }

        features.add(feature);
    }

    public ObservableList<Feature> getFeatures(){
        return features;
    }

    public void clear(){
        features.clear();
    }

}
