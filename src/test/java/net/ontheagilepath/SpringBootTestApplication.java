package net.ontheagilepath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;

/**
 * Created by sebastianradics on 05.03.17.
 */
@SpringBootApplication
@TestConfiguration
public class SpringBootTestApplication {
    public static void main( String[] args )
    {
        SpringApplication.run(SpringBootTestApplication.class, args);
    }
}
