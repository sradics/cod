package net.ontheagilepath;

import net.ontheagilepath.binding.FeatureListType;
import net.ontheagilepath.binding.FeatureType;
import net.ontheagilepath.binding.ObjectFactory;
import org.joda.time.DateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.math.BigDecimal;
import java.util.Arrays;

@EnableAutoConfiguration
@Configuration
@ComponentScan(excludeFilters={
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value=App.class)
})
public class App 
{
    public static void main( String[] args )
    {
        SpringApplication.run(App.class, args);


    }


    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            DateTime startDate = new DateTime(2017,1,1,12,00);
            Feature[] feature = {
                    new FeatureBuilder().withName("A").withDurationInWeeks(BigDecimal.TEN).withCostOfDelayPerWeek(BigDecimal.valueOf(5000)).build(),
                    new FeatureBuilder().withName("B")
                            .withDurationInWeeks(BigDecimal.valueOf(5))
                            .withCostOfDelayPerWeek(BigDecimal.valueOf(8000))
                            .withDurationAndCostOfDelayPeriod(BigDecimal.valueOf(5),startDate.plusWeeks(4),startDate.plusWeeks(8))
                            .build(),
                    new FeatureBuilder().withName("C")
                            .withDurationInWeeks(BigDecimal.valueOf(5))
                            .withCostOfDelayPerWeek(BigDecimal.valueOf(15000))
                            .withDurationAndCostOfDelayPeriod(BigDecimal.valueOf(5),startDate.plusWeeks(3),startDate.plusWeeks(8))
                            .build(),

            };

            Feature[] featureSequence = ctx.getBean(Sequencer.class).calculateSequence(Arrays.asList(feature),startDate);

            SequenceSummarizer summarizer = ctx.getBean(SequenceSummarizer.class);
            summarizer.printSummary();
            System.out.println(Arrays.asList(featureSequence));
            System.out.println("Done");
            ObjectFactory objectFactory = new ObjectFactory();
            FeatureListType featureList = objectFactory.createFeatureListType();
            featureList.setProjectStartDate("01.10.2017");
            FeatureType featureType = objectFactory.createFeatureType();
            featureType.setCostOfDelayStartDate("01.12.2017");
            featureType.setCostOfDelayEndDate("20.12.2017");
            featureType.setName("A");
            featureType.setDurationInWeeks("10");
            featureType.setCostOfDelayPerWeek("8000");
            featureType.setCostOfDelayStartWeek("3");
            featureType.setCostOfDelayEndWeek("5");
            featureList.getFeature().add(featureType);

            try {
                JAXBElement<FeatureListType> gl =
                        objectFactory.createFeatures( featureList );
                JAXBContext jc = JAXBContext.newInstance( "net.ontheagilepath.binding" );
                Marshaller m = jc.createMarshaller();
                m.marshal( gl, System.out );
            } catch( JAXBException jbe ){
                throw jbe;
            }

        };

    }
}
