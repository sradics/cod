package net.ontheagilepath.aspects;

import javafx.concurrent.Task;
import net.ontheagilepath.Feature;
import net.ontheagilepath.SequenceSummarizer;
import net.ontheagilepath.SequenceSummaryData;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.util.logging.Logger;

/**
 * Created by sebastianradics on 11.03.17.
 */
@Component
@Aspect
public class SummaryAspect implements ApplicationEventPublisherAware {
    private static final Logger log = Logger.getLogger( SummaryAspect.class.getName() );
    private ApplicationEventPublisher applicationEventPublisher;

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
        applicationEventPublisher.publishEvent(new MessageEvent(this,"start writing summary file to: "+
                sequenceSummarizer.getCurrentSummary().getAbsolutePath()));
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                File summary = sequenceSummarizer.printSummary();
                applicationEventPublisher.publishEvent(new MessageEvent(this,"wrote full sequence details to:"+summary.getAbsolutePath()));
                log.info("print summary to file:"+summary);
                return true;
            }
        };
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();

    }

    @AfterReturning("execution(* processPermutation*(..))")
    public void afterReturningprocessPermutation()
    {

    }

    @Around("execution(* updateStats*(..))")
    public Object aroundUpdateStats(ProceedingJoinPoint pjp) throws Throwable
    {
        sequenceSummarizer.addSummary(new SequenceSummaryData(
                (BigDecimal)pjp.getArgs()[2], (Feature[])pjp.getArgs()[1]));
        Object retVal = pjp.proceed();
        return retVal;
    }




    @AfterReturning("execution(public * loadInputData*(..))")
    public void loadInputDataFromFilePointCut() {
        log.info("flush sequence summarizer");
        sequenceSummarizer.clear();

    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

}
