package net.ontheagilepath;

import java.io.File;

/**
 * Created by sebastianradics on 24.02.17.
 */
public interface SequenceSummarizer{
    void addSummary(SequenceSummaryData summary);
    SequenceSummaryData getTotalCostOfDelayMax();
    SequenceSummaryData getTotalCostOfDelayMin();

    File printSummary();

    void clear();

    File getCurrentSummary();
}
