package net.ontheagilepath.util;

import net.ontheagilepath.Feature;
import net.ontheagilepath.FeatureSequenceModel;
import net.ontheagilepath.binding.FeatureListType;
import net.ontheagilepath.binding.FeatureType;
import net.ontheagilepath.binding.ObjectFactory;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.List;
import java.util.logging.Logger;

import static net.ontheagilepath.util.DateTimeStringConverter.PATTERN;

/**
 * Created by sebastianradics on 10.03.17.
 */
@Component
public class JAXBFileHelperImpl implements JAXBFileHelper {
    private static final Logger log = Logger.getLogger( JAXBFileHelperImpl.class.getName() );

    @Override
    public FeatureSequenceModel loadInputDataFromFile(File file){
        if (file!=null && file.exists()){
            try {
                return loadInputDataFromStream(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return new FeatureSequenceModel();
    }

    public void saveToFile(File fileToSave, FeatureSequenceModel sequenceModel){

        if (fileToSave != null){
            List<Feature> features = sequenceModel.getFeatures();
            ObjectFactory objectFactory = new ObjectFactory();
            FeatureListType featureList = objectFactory.createFeatureListType();
            featureList.setProjectStartDate(sequenceModel.getProjectStartDate());
            for (Feature feature : features) {
                FeatureType featureType = objectFactory.createFeatureType();
                if (feature.getCostOfDelayStartDate()!=null)
                    featureType.setCostOfDelayStartDate(feature.getCostOfDelayStartDate().toString(PATTERN));
                if (feature.getCostOfDelayEndDate()!=null)
                    featureType.setCostOfDelayEndDate(feature.getCostOfDelayEndDate().toString(PATTERN));


                featureType.setName(feature.getName());
                if (feature.getDurationInWeeks()!=null)
                    featureType.setDurationInWeeks(feature.getDurationInWeeks().toEngineeringString());
                if (feature.getCostOfDelayPerWeek()!=null)
                    featureType.setCostOfDelayPerWeek(feature.getCostOfDelayPerWeek().toEngineeringString());

                featureType.setCostOfDelayStartWeek(null);
                featureType.setCostOfDelayEndWeek(null);
                featureList.getFeature().add(featureType);
            }


            try {
                JAXBElement<FeatureListType> gl =
                        objectFactory.createFeatures( featureList );
                JAXBContext jc = JAXBContext.newInstance( "net.ontheagilepath.binding" );
                Marshaller m = jc.createMarshaller();
                m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
                m.marshal( gl, new FileOutputStream(fileToSave) );
            } catch( Exception jbe ){
                throw new RuntimeException(jbe);
            }
        }
    }

    @Override
    public FeatureSequenceModel loadInputDataFromStream(InputStream fileToOpen) {

        try {
            JAXBContext jc = JAXBContext.newInstance("net.ontheagilepath.binding");
            Unmarshaller u = jc.createUnmarshaller();
            JAXBElement<FeatureListType> doc = (JAXBElement<FeatureListType>) u.unmarshal(fileToOpen);
            log.info("Number of features to load:"+doc.getValue().getFeature().size());
            FeatureSequenceModel sequenceModel= new FeatureSequenceModel();

            FeatureListType featureListType = doc.getValue();
            sequenceModel.setProjectStartDate(featureListType.getProjectStartDate());
            List<FeatureType> features = featureListType.getFeature();
            for (FeatureType feature : features) {
                sequenceModel.addFeature(
                        feature.getName(),
                        feature.getCostOfDelayPerWeek(),
                        feature.getDurationInWeeks(),
                        feature.getCostOfDelayStartWeek(),
                        feature.getCostOfDelayEndWeek(),
                        feature.getCostOfDelayStartDate(),
                        feature.getCostOfDelayEndDate()
                );
            }
            return sequenceModel;

        } catch (Exception e1) {
            log.info(e1.getMessage());
            throw new RuntimeException(e1);
        }


    }

}
