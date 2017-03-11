package net.ontheagilepath;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by sebastianradics on 11.03.17.
 */
public class SequenceSummarizerImplUnitTest {
    private static final Logger log = Logger.getLogger( SequenceSummarizerImplUnitTest.class.getName() );
    private File tempFile;
    private File sortedTempFile;
    private SequenceSummarizerImpl sequenceSummarizer;

    @Before
    public void setUp() throws Exception {
        tempFile = File.createTempFile(getClass().getName(),"_temp_"+System.currentTimeMillis()+"_"+Math.random());
        tempFile.deleteOnExit();

        sortedTempFile = File.createTempFile(getClass().getName(),"_sorted_"+System.currentTimeMillis()+"_"+Math.random());
        sortedTempFile.deleteOnExit();

        log.info("tempfile:"+tempFile.getAbsolutePath());
        log.info("sortedTempFile:"+sortedTempFile.getAbsolutePath());



        sequenceSummarizer = spy(new SequenceSummarizerImpl(){
            void initFiles(){
                setTempFile(tempFile);
                setSorted(sortedTempFile);
            }
        });
        when(sequenceSummarizer.createTempFile()).thenReturn(tempFile);
        when(sequenceSummarizer.createTempFileSorted()).thenReturn(sortedTempFile);
        when(sequenceSummarizer.getFileFlushThreshold()).thenReturn(0);


    }

    private int countLines(File linesToCheck){
        try {
            return FileUtils.readLines(linesToCheck, Charset.defaultCharset()).size();
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }

    @Test
    public void addSummary() throws Exception {
        SequenceSummaryData sequenceSummaryDataMin = new SequenceSummaryData(BigDecimal.ZERO, Arrays.asList(new String[]{"A","B"}));
        sequenceSummarizer.addSummary(sequenceSummaryDataMin);
        checkMinMaxAndLineCount(sequenceSummaryDataMin, sequenceSummaryDataMin,1);

        SequenceSummaryData sequenceSummaryDataMax = new SequenceSummaryData(new BigDecimal(100), Arrays.asList(new String[]{"B","A"}));
        sequenceSummarizer.addSummary(sequenceSummaryDataMax);
        checkMinMaxAndLineCount(sequenceSummaryDataMin, sequenceSummaryDataMax,2);

        sequenceSummarizer.addSummary(new SequenceSummaryData(BigDecimal.ONE, Arrays.asList(new String[]{"B","A"})));
        checkMinMaxAndLineCount(sequenceSummaryDataMin, sequenceSummaryDataMax,3);

        sequenceSummaryDataMin = new SequenceSummaryData(new BigDecimal(-1), Arrays.asList(new String[]{"B","A"}));
        sequenceSummarizer.addSummary(sequenceSummaryDataMin);
        checkMinMaxAndLineCount(sequenceSummaryDataMin, sequenceSummaryDataMax,4);
    }

    private void checkMinMaxAndLineCount(SequenceSummaryData sequenceSummaryDataMin, SequenceSummaryData sequenceSummaryDataMax, int lineCount) {
        assertEquals(sequenceSummaryDataMin,sequenceSummarizer.getTotalCostOfDelayMin());
        assertEquals(sequenceSummaryDataMax,sequenceSummarizer.getTotalCostOfDelayMax());
        assertEquals(lineCount,countLines(tempFile));
    }

    @Test
    public void printSummary() throws Exception {
        SequenceSummaryData sequenceSummaryDataMin = new SequenceSummaryData(BigDecimal.ZERO, Arrays.asList(new String[]{"A","B","C"}));
        sequenceSummarizer.addSummary(sequenceSummaryDataMin);
        SequenceSummaryData sequenceSummaryDataMax = new SequenceSummaryData(new BigDecimal(100), Arrays.asList(new String[]{"B","A","C"}));
        sequenceSummarizer.addSummary(sequenceSummaryDataMax);
        sequenceSummarizer.addSummary(new SequenceSummaryData(BigDecimal.ONE, Arrays.asList(new String[]{"C","B","A"})));

        sequenceSummarizer.printSummary();

        assertEquals(3,countLines(tempFile));
        assertEquals(3,countLines(sortedTempFile));

        List<String> lines = FileUtils.readLines(sortedTempFile,Charset.defaultCharset());
        assertTrue(lines.get(0),lines.get(0).contains("A, B, C"));
        assertTrue(lines.get(1),lines.get(1).contains("C, B, A"));
        assertTrue(lines.get(2),lines.get(2).contains("B, A, C"));

        lines = FileUtils.readLines(tempFile,Charset.defaultCharset());
        assertTrue(lines.get(0),lines.get(0).contains("A, B, C"));
        assertTrue(lines.get(1),lines.get(1).contains("B, A, C"));
        assertTrue(lines.get(2),lines.get(2).contains("C, B, A"));
    }

    @Test
    public void clear() throws Exception {
        sequenceSummarizer.addSummary(new SequenceSummaryData(BigDecimal.ONE, Arrays.asList(new String[]{"B","A"})));
        assertEquals(1,countLines(tempFile));
        assertNotNull(sequenceSummarizer.getTotalCostOfDelayMax());
        assertNotNull(sequenceSummarizer.getTotalCostOfDelayMin());

        sequenceSummarizer.clear();
        assertEquals(0,countLines(tempFile));
        assertEquals(0,countLines(sortedTempFile));
        assertNull(sequenceSummarizer.getTotalCostOfDelayMax());
        assertNull(sequenceSummarizer.getTotalCostOfDelayMin());
    }

}