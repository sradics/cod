package net.ontheagilepath.aspects;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Created by sebastianradics on 12.03.17.
 */
@Component
@Aspect
public class ProgressUpdateAspect {
    private static final Logger log = Logger.getLogger( ProgressUpdateAspect.class.getName() );
    private ProgressBar progressBar;

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Around("execution(* updateProgress*(..))")
    public Object aroundupdateProgress(ProceedingJoinPoint pjp) throws Throwable
    {
        long currentPermutation = (long)pjp.getArgs()[0];
        long totalPermutation = (long)pjp.getArgs()[1];
        if (currentPermutation % 1000 == 0 || currentPermutation==totalPermutation) {
            if (progressBar != null) {
                double percentage = currentPermutation*1.0 / totalPermutation * 1.0;

                Platform.runLater(new Runnable() {//UI update must run the JavaFX thread!
                    @Override
                    public void run() {
                        progressBar.setProgress(percentage);
                    }
                });

            }
        }
        Object retVal = pjp.proceed();
        return retVal;
    }
}
