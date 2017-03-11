package net.ontheagilepath;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.ontheagilepath.util.DateTimeStringConverter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.text.SimpleDateFormat;
import java.util.Date;

import static net.ontheagilepath.util.DateTimeStringConverter.PATTERN;

/**
 * Created by sebastianradics on 27.02.17.
 */
public class FeatureSequenceModel {
    private ObservableList<Feature> features = FXCollections.observableArrayList();
    private String projectStartDate = new SimpleDateFormat(PATTERN).format(new Date());

    public String getProjectStartDate() {
        return projectStartDate;
    }

    public void setProjectStartDate(String projectStartDate) {
        this.projectStartDate = projectStartDate;
    }

    public void addFeature(String name, String costOfDelayPerWeek, String durationInWeeks, String startWeek, String endWeek, String startDate, String endDate) {
        DateTime projectStartDate = new DateTimeStringConverter().fromString(getProjectStartDate());
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
            feature.setCostOfDelayStartWeek(new DateTimeStringConverter().fromString(startDate));
        }
        if (endDate!=null && !endDate.isEmpty()){
            feature.setCostOfDelayEndWeek(new DateTimeStringConverter().fromString(endDate));
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
