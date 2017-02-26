package net.ontheagilepath;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sebastianradics on 24.02.17.
 */
@Component
public class SequenceSummarizerImpl implements SequenceSummarizer {
    private List<SequenceSummaryData> summaryDataList = new ArrayList<SequenceSummaryData>();

    @Override
    public void addSummary(SequenceSummaryData summary){
        summaryDataList.add(summary);
    }

    @Override
    public void printSummary(){
        summaryDataList.sort( new Comparator<SequenceSummaryData>() {
            @Override
            public int compare(SequenceSummaryData o1, SequenceSummaryData o2) {
                return o1.getTotalCostOfDelay().compareTo(o2.getTotalCostOfDelay());
            }
        });
        for (SequenceSummaryData sequenceSummaryData : summaryDataList) {
            System.out.println(sequenceSummaryData);
        }
    }
}
