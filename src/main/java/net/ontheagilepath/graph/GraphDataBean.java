package net.ontheagilepath.graph;

/**
 * Created by sebastianradics on 04.03.17.
 */
public class GraphDataBean {
    private String costOfDelay;
    private String name;
    private String week;
    private String currentFeatureName;

    public GraphDataBean(String costOfDelay, String name, String week, String currentFeatureName) {
        this.costOfDelay = costOfDelay;
        this.name = name;
        this.week = week;
        this.currentFeatureName = currentFeatureName;
    }

    public GraphDataBean() {
    }

    public String getCostOfDelay() {
        return costOfDelay;
    }

    public void setCostOfDelay(String costOfDelay) {
        this.costOfDelay = costOfDelay;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getCurrentFeatureName() {
        return currentFeatureName;
    }

    public void setCurrentFeatureName(String currentFeatureName) {
        this.currentFeatureName = currentFeatureName;
    }
}
