package net.ontheagilepath;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by sebastianradics on 24.02.17.
 */
@Component
public class SequenceSummarizerImpl implements SequenceSummarizer {
    private static final Logger log = Logger.getLogger( SequenceSummarizerImpl.class.getName() );
    private List<SequenceSummaryData> summaryDataList = new ArrayList<SequenceSummaryData>();

    File getTempFile() {
        return _tempFile;
    }

    void setTempFile(File tempFile) {
        this._tempFile = tempFile;
    }

    File getSorted() {
        return _sorted;
    }

    void setSorted(File sorted) {
        this._sorted = sorted;
    }

    private File _tempFile;
    private File _sorted;
    private static final int FILE_USAGE_THRESHOLD = 10000;
    private static final String SEARCHPATTERN = "totalCostOfDelay=";
    private SequenceSummaryData totalCostOfDelayMin = null;
    private SequenceSummaryData totalCostOfDelayMax = null;


    public SequenceSummarizerImpl(){
        initFiles();
    }

    void initFiles(){
        setTempFile(createTempFile());
        setSorted(createTempFileSorted());

        if (getTempFile().exists()) {
            getTempFile().delete();
        }
        if (getSorted().exists())
            getSorted().delete();

        try {
            getTempFile().createNewFile();
            getSorted().createNewFile();
            log.info("write summary to file:"+getTempFile().getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    int getFileFlushThreshold(){
        return FILE_USAGE_THRESHOLD;
    }

    File createTempFileSorted() {
        return new File(FileUtils.getTempDirectory(),"codtempdata_sorted");
    }

    File createTempFile() {
        return new File(FileUtils.getTempDirectory(),"codtempdata");
    }


    @Override
    public void addSummary(SequenceSummaryData summary){
        checkForNewMin(summary);
        checkForNewMax(summary);

        summaryDataList.add(summary);
        if (summaryDataList.size()>getFileFlushThreshold()){
            flush();
        }
    }

    private void checkForNewMax(SequenceSummaryData summary) {
        if (totalCostOfDelayMax==null){
            totalCostOfDelayMax = summary;
        }else{
            if (totalCostOfDelayMax.getTotalCostOfDelay().compareTo(summary.getTotalCostOfDelay())==-1){
                totalCostOfDelayMax = summary;
            }
        }
    }

    private void checkForNewMin(SequenceSummaryData summary) {
        if (totalCostOfDelayMin==null){
            totalCostOfDelayMin = summary;
        }else{
            if (totalCostOfDelayMin.getTotalCostOfDelay().compareTo(summary.getTotalCostOfDelay())==1){
                totalCostOfDelayMin = summary;
            }
        }
    }

    private void flush() {
        try {
            log.info("flush next "+summaryDataList.size()+" lines to file: "+getTempFile().getAbsolutePath());
            FileUtils.writeLines(getTempFile(),summaryDataList,true);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        summaryDataList.clear();
    }


    @Override
    public File printSummary(){
        flush();
        Path initialFile = Paths.get(getTempFile().getAbsolutePath());
        setSorted(createTempFileSorted());

        Path sortedFile = Paths.get(getSorted().getAbsolutePath());
        try {
            getSorted().delete();
            getSorted().createNewFile();
            Stream<CharSequence> sortedLines = Files.lines(initialFile).sorted(
                    new Comparator<String>() {
                        @Override
                        public int compare(String o1, String o2) {
                            int codIndex = o1.lastIndexOf(SEARCHPATTERN);
                            BigDecimal o1Cod =
                                    new BigDecimal(o1.substring(codIndex+SEARCHPATTERN.length(),o1.indexOf(",",codIndex)));
                            codIndex = o2.lastIndexOf(SEARCHPATTERN);
                            BigDecimal o2Cod =
                                    new BigDecimal(o2.substring(codIndex+SEARCHPATTERN.length(),o2.indexOf(",",codIndex)));
                            return o1Cod.compareTo(o2Cod);
                        }
                    }
            ).map(Function.identity());

            Files.write(sortedFile, sortedLines::iterator, StandardOpenOption.CREATE);
            log.info("all sequences can be found sorted in file: "+getSorted().getAbsolutePath());
            log.info("Min:"+(totalCostOfDelayMin!=null?totalCostOfDelayMin.toString():"-")+
                    " Max:"+(totalCostOfDelayMax!=null?totalCostOfDelayMax.toString():"-"));
            return getSorted();

        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    @Override
    public void clear() {
        totalCostOfDelayMin = null;
        totalCostOfDelayMax = null;
        summaryDataList.clear();
        setTempFile(createTempFile());
        setSorted(createTempFileSorted());

        try {
            if (getSorted()!=null && getSorted().exists()) {
                getSorted().delete();
                getSorted().createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            getTempFile().delete();
            getTempFile().createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.info("clear called - new tempfile is: "+getTempFile().getAbsolutePath());
    }

    @Override
    public File getCurrentSummary() {
        return getSorted();
    }

    @Override
    public SequenceSummaryData getTotalCostOfDelayMax() {
        return totalCostOfDelayMax;
    }

    @Override
    public SequenceSummaryData getTotalCostOfDelayMin() {
        return totalCostOfDelayMin;
    }
}
