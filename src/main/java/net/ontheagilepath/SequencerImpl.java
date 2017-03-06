package net.ontheagilepath;

import org.joda.time.DateTime;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by sebastianradics on 21.02.17.
 */
@Component
public class SequencerImpl implements Sequencer {
    private static final Logger log = Logger.getLogger( SequencerImpl.class.getName() );
    @Autowired
    private TotalCostOfDelayCalculator totalCostOfDelayCalculator;

    @Autowired
    private SequenceSummarizer sequenceSummarizer;

    @Override
    public Feature[] calculateSequence(Collection<Feature> features, DateTime startDate){
        if (features.isEmpty())
            return new Feature[]{};
        if (features.size()==1)
            return features.toArray(new Feature[]{});
        if (startDate==null)
            startDate=new DateTime();

        Feature[] featuresForSequenceTrials = features.toArray(new Feature[]{});
        Generator<Integer> generator = getiCombinatoricsVectors(features);

        BigDecimal currentMinCostOfDelay = null;
        Feature[] featuresForSequenceMinTrial = null;
        long counter=0;
        for (ICombinatoricsVector<Integer> perm : generator) {
            counter++;
            Feature[] featuresForSequenceCurrentTrial = getFeaturesInSequenceForCurrentTrial(featuresForSequenceTrials, perm);
            BigDecimal costOfDelayForSequence = totalCostOfDelayCalculator.calculateTotalCostOfDelayForSequence(featuresForSequenceCurrentTrial, startDate);

            sequenceSummarizer.addSummary(new SequenceSummaryData(costOfDelayForSequence,featuresForSequenceCurrentTrial));
            if (currentMinCostOfDelay==null){
                currentMinCostOfDelay = costOfDelayForSequence;
                featuresForSequenceMinTrial =featuresForSequenceCurrentTrial;
            } else if (costOfDelayForSequence.compareTo(currentMinCostOfDelay)==-1){
                featuresForSequenceMinTrial =featuresForSequenceCurrentTrial;
                currentMinCostOfDelay = costOfDelayForSequence;
            }
            if (counter % 100000 ==0)
                log.info("counter"+counter);
        }


        return featuresForSequenceMinTrial;
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
