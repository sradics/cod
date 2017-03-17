package net.ontheagilepath;

import net.ontheagilepath.aspects.CancelCalculationEvent;
import org.joda.time.DateTime;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by sebastianradics on 21.02.17.
 */
@Component
public class SequencerImpl implements Sequencer,ApplicationListener<CancelCalculationEvent> {
    private static final Logger log = Logger.getLogger( SequencerImpl.class.getName() );
    private boolean cancel = false;
    @Autowired
    private TotalCostOfDelayCalculator totalCostOfDelayCalculator;


    @Autowired
    private SequencerImpl sequencer;

    @Override
    public void onApplicationEvent(CancelCalculationEvent event) {
        cancel = true;
    }

    @Override
    public Feature[] calculateSequence(Collection<Feature> features, DateTime startDate){
        cancel = false;
        if (features.isEmpty())
            return new Feature[]{};
        if (features.size()==1)
            return features.toArray(new Feature[]{});
        if (startDate==null)
            startDate=new DateTime();

        Feature[] featuresForSequenceTrials = features.toArray(new Feature[]{});
        Generator<Integer> generator = getiCombinatoricsVectors(features);
        long numberOfPermutations = generator.getNumberOfGeneratedObjects();
        long counter=0;

        PermutationTrialStats stats = new PermutationTrialStats();
        for (ICombinatoricsVector<Integer> perm : generator) {
            counter++;
            sequencer.updateProgress(counter,numberOfPermutations);
            sequencer.processPermutation(startDate, featuresForSequenceTrials, stats, perm);
            if (cancel){
                break;
            }
        }


        return stats.featuresForSequenceMinTrial;
    }

    @Override
    public Feature[] calculateWsjfSequence(Collection<Feature> features) {
        ArrayList<Feature> featureList = new ArrayList<Feature>();
        featureList.addAll(features);
        featureList.sort(new Comparator<Feature>() {
            @Override
            public int compare(Feature o1, Feature o2) {
                return o1.calculateCD3().compareTo(o2.calculateCD3())*-1;
            }
        });
        return featureList.toArray(new Feature[]{});
    }

    void updateProgress(long currentPermutation, long totalPermutations){
        if (currentPermutation % 10000==0){
            log.info("processed:" +currentPermutation+" out of: "+totalPermutations);
        }
    }

    void processPermutation(DateTime startDate, Feature[] featuresForSequenceTrials, PermutationTrialStats stats, ICombinatoricsVector<Integer> perm) {
        Feature[] featuresForSequenceCurrentTrial = getFeaturesInSequenceForCurrentTrial(featuresForSequenceTrials, perm);
        BigDecimal costOfDelayForSequence = totalCostOfDelayCalculator.calculateTotalCostOfDelayForSequence(featuresForSequenceCurrentTrial, startDate);

        sequencer.updateStats(stats, featuresForSequenceCurrentTrial, costOfDelayForSequence);
    }

    void updateStats(PermutationTrialStats stats, Feature[] featuresForSequenceCurrentTrial, BigDecimal costOfDelayForSequence) {
        if (stats.currentMinCostOfDelay==null){
            stats.currentMinCostOfDelay = costOfDelayForSequence;
            stats.featuresForSequenceMinTrial =featuresForSequenceCurrentTrial;
        } else if (costOfDelayForSequence.compareTo(stats.currentMinCostOfDelay)==-1){
            stats.featuresForSequenceMinTrial =featuresForSequenceCurrentTrial;
            stats.currentMinCostOfDelay = costOfDelayForSequence;
        }
    }

    public static class PermutationTrialStats{
        public BigDecimal currentMinCostOfDelay = null;
        public Feature[] featuresForSequenceMinTrial = null;
    }

    private Feature[] getFeaturesInSequenceForCurrentTrial(Feature[] featuresForSequenceTrials, ICombinatoricsVector<Integer> perm) {
        List<Integer> sequence =
                perm.getVector();
        log.finest(perm.toString());
        Feature[] featuresForSequenceCurrentTrial = new Feature[sequence.size()];
        for (int i = 0; i < sequence.size(); i++) {
            featuresForSequenceCurrentTrial[i] =  featuresForSequenceTrials[sequence.get(i)];
        }
        return featuresForSequenceCurrentTrial;
    }


    private Generator<Integer> getiCombinatoricsVectors(Collection<Feature> features) {
        int numberOfFeatures = features.size();
        Integer[] vectorInput = new Integer[numberOfFeatures];
        for (int i = 0; i < numberOfFeatures; i++) {
            vectorInput[i] = i;
        }
        ICombinatoricsVector<Integer> initialVector = Factory.createVector(vectorInput);
        return Factory.createPermutationGenerator(initialVector);
    }

}
