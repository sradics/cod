package net.ontheagilepath.aspects;

import net.ontheagilepath.SequenceSummarizer;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.logging.Logger;

/**
 * Created by sebastianradics on 11.03.17.
 */
@Component
@Aspect
public class SummaryAspect {
    private static final Logger log = Logger.getLogger( SummaryAspect.class.getName() );

    @Autowired
    private SequenceSummarizer sequenceSummarizer;

    @Before("execution(public * calculateSequence*(..))")
    public void beforeCalculateSequence()
    {
        log.info("flush sequence summarizer before calculation");
        sequenceSummarizer.clear();

    }

    @AfterReturning("execution(public * calculateSequence*(..))")
    public void afterReturningCalculateSequence()
    {
        File summary = sequenceSummarizer.printSummary();
        log.info("print summary to file:"+summary);

    }


    @AfterReturning("execution(public * loadInputData*(..))")
    public void loadInputDataFromFilePointCut() {
        log.info("flush sequence summarizer");
        sequenceSummarizer.clear();

    }

}
