package net.ontheagilepath.util;

import net.ontheagilepath.FeatureSequenceModel;

import java.io.File;
import java.io.InputStream;

/**
 * Created by sebastianradics on 10.03.17.
 */
public interface JAXBFileHelper {
    FeatureSequenceModel loadInputDataFromFile(File file);

    FeatureSequenceModel loadInputDataFromStream(InputStream fileToOpen);
    void saveToFile(File fileToSave, FeatureSequenceModel sequenceModel);
}
