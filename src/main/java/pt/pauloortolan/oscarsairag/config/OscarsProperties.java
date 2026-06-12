package pt.pauloortolan.oscarsairag.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.oscars")
@Getter
@Setter
public class OscarsProperties {

    /** Classpath or file path to the oscars CSV file. */
    private String csvPath = "classpath:datasources/oscars.csv";

    /** Max rows to ingest. -1 means all rows. */
    private int maxRows = 500;

}
