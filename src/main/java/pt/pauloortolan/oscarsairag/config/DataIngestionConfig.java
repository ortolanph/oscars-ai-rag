package pt.pauloortolan.oscarsairag.config;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataIngestionConfig {

    private static final int BATCH_SIZE = 500;

    private final VectorStore vectorStore;
    private final OscarsProperties properties;
    private final ResourceLoader resourceLoader;

    @PostConstruct
    public void ingestData() throws IOException, CsvValidationException {
        log.info("DataIngestionConfig.ingestData()");
        log.info("Starting Oscars data ingestion from '{}'", properties.getCsvPath());

        List<Document> batch = new ArrayList<>(BATCH_SIZE);
        int count = 0;
        int maxRows = properties.getMaxRows();

        try (CSVReader reader = openCsv()) {
            String[] header = reader.readNext();
            if (header == null) {
                log.warn("CSV file is empty — nothing to ingest.");
                return;
            }

            String[] row;
            while ((row = reader.readNext()) != null) {
                if (maxRows > 0 && count >= maxRows) break;

                Document doc = toDocument(row);
                batch.add(doc);
                count++;

                if (batch.size() == BATCH_SIZE) {
                    vectorStore.add(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                vectorStore.add(batch);
            }
        }

        log.info("Ingestion complete — {} documents loaded into Redis vector store.", count);
    }

    private CSVReader openCsv() throws IOException {
        log.info("DataIngestionConfig.openCsv()");
        var resource = resourceLoader.getResource(properties.getCsvPath());
        return new CSVReader(new InputStreamReader(resource.getInputStream()));
    }

    /**
     * CSV columns: year_film, year_ceremony, ceremony, category, canon_category,
     * name, film, winner
     * <p>
     * Converts one row into a natural-language sentence so the embedding model
     * can reason about it semantically.
     */
    private Document toDocument(String[] row) {
        log.info("DataIngestionConfig.toDocument(row={})", Arrays.toString(row));
        String yearFilm = safeGet(row, 0);
        String yearCeremony = safeGet(row, 1);
        String ceremony = safeGet(row, 2);
        String category = safeGet(row, 3);
        String canonCategory = safeGet(row, 4);
        String name = safeGet(row, 5);
        String film = safeGet(row, 6);
        String winner = safeGet(row, 7);

        boolean isWinner = "true".equalsIgnoreCase(winner.trim());

        String text = String.format(
                "%s was nominated at the %s Academy Awards (ceremony #%s, year %s) " +
                        "in the category \"%s\" for the film \"%s\". %s",
                name, ordinal(ceremony), yearCeremony, yearFilm,
                canonCategory.isBlank() ? category : canonCategory,
                film,
                isWinner ? "They WON the award." : "They did not win."
        );

        return new Document(text, Map.of(
                "year_film", yearFilm,
                "year_ceremony", yearCeremony,
                "ceremony", ceremony,
                "category", category,
                "canon_category", canonCategory,
                "name", name,
                "film", film,
                "winner", winner
        ));
    }

    private String safeGet(String[] row, int idx) {
        return (idx < row.length && row[idx] != null) ? row[idx].trim() : "";
    }

    private String ordinal(String numberStr) {
        try {
            int n = Integer.parseInt(numberStr.trim());
            String suffix = switch (n % 100) {
                case 11, 12, 13 -> "th";
                default -> switch (n % 10) {
                    case 1 -> "st";
                    case 2 -> "nd";
                    case 3 -> "rd";
                    default -> "th";
                };
            };
            return n + suffix;
        } catch (NumberFormatException _) {
            return numberStr;
        }
    }
}
