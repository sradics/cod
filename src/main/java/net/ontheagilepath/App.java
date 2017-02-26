package net.ontheagilepath;

import org.joda.time.DateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Hello world!
 *
 */
@SpringBootApplication
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
                    new FeatureBuilder().withName("A").withDurationInWeeks(BigDecimal.TEN).withCostOfDelayPerWeek(new Feature.CostOfDelayPerWeek(BigDecimal.valueOf(5000))).build(),
                    new FeatureBuilder().withName("B")
                            .withDurationInWeeks(BigDecimal.valueOf(5))
                            .withCostOfDelayPerWeek(new Feature.CostOfDelayPerWeek(BigDecimal.valueOf(8000)))
                            .withDurationAndCostOfDelayPeriod(BigDecimal.valueOf(5),startDate.plusWeeks(4),startDate.plusWeeks(8))
                            .build(),
                    new FeatureBuilder().withName("C")
                            .withDurationInWeeks(BigDecimal.valueOf(5))
                            .withCostOfDelayPerWeek(new Feature.CostOfDelayPerWeek(BigDecimal.valueOf(15000)))
                            .withDurationAndCostOfDelayPeriod(BigDecimal.valueOf(5),startDate.plusWeeks(3),startDate.plusWeeks(8))
                            .build(),

            };

            Feature[] featureSequence = ctx.getBean(Sequencer.class).calculateSequence(Arrays.asList(feature),startDate);

            SequenceSummarizer summarizer = ctx.getBean(SequenceSummarizer.class);
            summarizer.printSummary();
            System.out.println(Arrays.asList(featureSequence));
            System.out.println("Done");

        };

    }
}
