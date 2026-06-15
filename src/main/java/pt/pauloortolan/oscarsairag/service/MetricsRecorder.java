package pt.pauloortolan.oscarsairag.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsRecorder {

    public static final String FINISH_REASON = "finish_reason";
    private final MeterRegistry meterRegistry;

    public void recordMetrics(ChatResponse response) {
        log.info("MetricsRecorder::recordMetrics(response={})", response);
        Usage usage = response.getMetadata().getUsage();
        String finishReason = response.getResults().get(0)
                .getMetadata().getFinishReason();

        // Token counters (cumulative — Prometheus counters should only go up)
        Counter.builder("spring.ai.tokens.prompt")
                .description("Total prompt tokens used")
                .tag(FINISH_REASON, getFinishReason(finishReason))
                .register(meterRegistry)
                .increment(usage.getPromptTokens());

        Counter.builder("spring.ai.tokens.completion")
                .description("Total completion tokens used")
                .tag(FINISH_REASON, getFinishReason(finishReason))
                .register(meterRegistry)
                .increment(usage.getCompletionTokens());

        Counter.builder("spring.ai.tokens.total")
                .description("Total tokens used")
                .tag(FINISH_REASON, getFinishReason(finishReason))
                .register(meterRegistry)
                .increment(usage.getTotalTokens());

        // Count finish reasons as a separate counter
        Counter.builder("spring.ai.finish_reason")
                .description("AI response finish reason occurrences")
                .tag("reason", getFinishReason(finishReason))
                .register(meterRegistry)
                .increment();
    }

    private static @NonNull String getFinishReason(String finishReason) {
        return finishReason != null ? finishReason : "unknown";
    }
}
