package net.ontheagilepath;

import java.io.File;

/**
 * Created by sebastianradics on 24.02.17.
 */
public interface SequenceSummarizer {
    void addSummary(SequenceSummaryData summary);

    File printSummary();

    void clear();
}
