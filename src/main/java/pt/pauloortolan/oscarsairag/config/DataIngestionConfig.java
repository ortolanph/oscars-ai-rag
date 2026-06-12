package pt.pauloortolan.oscarsairag.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataIngestionConfig implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataIngestionConfig.class);

    private static final int BATCH_SIZE = 50;

    private final VectorStore vectorStore;
    private final OscarsProperties properties;
    private final ResourceLoader resourceLoader;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting Oscars data ingestion from '{}'", properties.getCsvPath());

        List<Document> batch = new ArrayList<>(BATCH_SIZE);
        int count = 0;
        int maxRows = properties.getMaxRows();

        try (CSVReader reader = openCsv()) {
            String[] header = reader.readNext(); // skip header row
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

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CSVReader openCsv() throws IOException {
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
        // Guard against malformed rows
        String yearFilm = safeGet(row, 0);
        String yearCeremony = safeGet(row, 1);
        String ceremony = safeGet(row, 2);
        String category = safeGet(row, 3);
        String canonCat = safeGet(row, 4);
        String name = safeGet(row, 5);
        String film = safeGet(row, 6);
        String winner = safeGet(row, 7);

        boolean isWinner = "true".equalsIgnoreCase(winner.trim());

        String text = String.format(
                "%s was nominated at the %s Academy Awards (ceremony #%s, year %s) " +
                        "in the category \"%s\" for the film \"%s\". %s",
                name, ordinal(yearCeremony), ceremony, yearFilm,
                canonCat.isBlank() ? category : canonCat,
                film,
                isWinner ? "They WON the award." : "They did not win."
        );

        return new Document(text, Map.of(
                "year_film", yearFilm,
                "year_ceremony", yearCeremony,
                "category", category,
                "canon_category", canonCat,
                "name", name,
                "film", film,
                "winner", winner
        ));
    }

    private String safeGet(String[] row, int idx) {
        return (idx < row.length && row[idx] != null) ? row[idx].trim() : "";
    }

    /**
     * Converts "1" → "1st", "2" → "2nd", etc.
     */
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
