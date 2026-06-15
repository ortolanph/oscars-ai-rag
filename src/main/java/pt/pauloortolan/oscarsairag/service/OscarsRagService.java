package pt.pauloortolan.oscarsairag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OscarsRagService {

    private static final int TOP_K = 5;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final MetricsRecorder metricsRecorder;

    public String ask(String prompt, String jsessionId) {
        log.info("OscarsRagService.ask(prompt = {})", prompt);
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(prompt)
                        .topK(TOP_K)
                        .build()
        );

        String context = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n- ", "- ", ""));

        String augmentedPrompt = """
                You are an expert on the Academy Awards (Oscars).
                Answer the user's question using ONLY the context below.
                If the context does not contain enough information, say so honestly.
                
                Context:
                %s
                
                Question: %s
                """.formatted(context, prompt);

        ChatResponse response = chatClient.prompt()
                .user(augmentedPrompt)
                .call()
                .chatResponse();

        metricsRecorder.recordMetrics(response, jsessionId);

        return response.getResult().getOutput().getText();
    }
}
