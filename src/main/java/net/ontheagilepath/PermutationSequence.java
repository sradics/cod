package net.ontheagilepath;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.logging.Logger;

/**
 * Created by sebastianradics on 23.02.17.
 */
public class PermutationSequence {
    private static final Logger log = Logger.getLogger( PermutationSequence.class.getName() );
    public static void main(String... args){
        int numberOfFeatures = 6;
        Integer[] vectorInput = new Integer[numberOfFeatures];
        for (int i = 0; i < numberOfFeatures; i++) {
            vectorInput[i] = i;
        }
        ICombinatoricsVector<Integer> initialVector = Factory.createVector(vectorInput);
        Generator<Integer> generator = Factory.createPermutationGenerator(initialVector);
        for (ICombinatoricsVector<Integer> perm : generator) {
            System.out.println(perm);
        }

    }
}
