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
    private File tempFile;
    private File sorted;
    private static final int FILE_USAGE_THRESHOLD = 10000;
    private static final String SEARCHPATTERN = "totalCostOfDelay=";

    public SequenceSummarizerImpl(){
        tempFile = new File(FileUtils.getTempDirectory(),"codtempdata");
        sorted = new File(FileUtils.getTempDirectory(),"codtempdata_sorted");

        if (tempFile.exists()) {
            tempFile.delete();
        }
        if (sorted.exists())
            sorted.delete();

        try {
            tempFile.createNewFile();
            sorted.createNewFile();
            log.info("write summary to file:"+tempFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addSummary(SequenceSummaryData summary){
        summaryDataList.add(summary);
        if (summaryDataList.size()>FILE_USAGE_THRESHOLD){
            flush();
        }
    }

    private void flush() {
        try {
            log.info("flush next "+summaryDataList.size()+" lines to file: "+tempFile.getAbsolutePath());
            FileUtils.writeLines(tempFile,summaryDataList,true);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        summaryDataList.clear();
    }


    @Override
    public File printSummary(){
        flush();
        Path initialFile = Paths.get(tempFile.getAbsolutePath());
        sorted = new File(FileUtils.getTempDirectory(),"codtempdata_sorted");

        Path sortedFile = Paths.get(sorted.getAbsolutePath());
        try {
            sorted.delete();
            sorted.createNewFile();
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
            log.info("all sequences can be found sorted in file: "+sorted.getAbsolutePath());
            return sorted;


        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear() {
        summaryDataList.clear();
        tempFile = new File(FileUtils.getTempDirectory(),"codtempdata");

        try {
            if (sorted!=null && sorted.exists()) {
                sorted.delete();
                sorted.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            tempFile.delete();
            tempFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.info("clear called - new tempfile is: "+tempFile.getAbsolutePath());
    }

    @Override
    public File getCurrentSummary() {
        return sorted;
    }
}
