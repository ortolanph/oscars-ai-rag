package pt.pauloortolan.oscarsairag.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG service: retrieves relevant Oscars records from Redis,
 * injects them as context, and asks the ChatClient to answer.
 */
@Service
public class OscarsRagService {

    private static final int TOP_K = 5;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public OscarsRagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient  = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    public String ask(String prompt) {
        // 1. Retrieve the most semantically similar documents
        List<Document> docs = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(prompt)
                .topK(TOP_K)
                .build()
        );

        // 2. Format retrieved docs as plain context text
        String context = docs.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n- ", "- ", ""));

        // 3. Build an augmented prompt and call the LLM
        String augmentedPrompt = """
            You are an expert on the Academy Awards (Oscars).
            Answer the user's question using ONLY the context below.
            If the context does not contain enough information, say so honestly.

            Context:
            %s

            Question: %s
            """.formatted(context, prompt);

        return chatClient.prompt()
            .user(augmentedPrompt)
            .call()
            .content();
    }
}
