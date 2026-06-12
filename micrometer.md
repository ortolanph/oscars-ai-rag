Yes! Spring AI integrates naturally with Micrometer, which can export metrics to Prometheus. Here's how to wire it up:

## 1. Add Dependencies

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

---

## 2. Enable the Prometheus Endpoint

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health, prometheus
  metrics:
    tags:
      application: ${spring.application.name}
```

---

## 3. Record Metrics After Each Chat Call

Create a service that wraps your `ChatClient` calls and records the token metrics manually via `MeterRegistry`:

```java

@Service
@RequiredArgsConstructor
public class MeteredChatService {

    private final ChatClient chatClient;
    private final MeterRegistry meterRegistry;

    public String chat(String userMessage) {
        ChatResponse response = chatClient.prompt()
                .user(userMessage)
                .call()
                .chatResponse();

        recordMetrics(response);

        return response.getResult().getOutput().getContent();
    }

    private void recordMetrics(ChatResponse response) {
        Usage usage = response.getMetadata().getUsage();
        String finishReason = response.getResults().get(0)
                .getMetadata().getFinishReason();

        // Token counters (cumulative — Prometheus counters should only go up)
        Counter.builder("spring.ai.tokens.prompt")
                .description("Total prompt tokens used")
                .tag("finish_reason", finishReason != null ? finishReason : "unknown")
                .register(meterRegistry)
                .increment(usage.getPromptTokens());

        Counter.builder("spring.ai.tokens.completion")
                .description("Total completion tokens used")
                .tag("finish_reason", finishReason != null ? finishReason : "unknown")
                .register(meterRegistry)
                .increment(usage.getGenerationTokens());

        Counter.builder("spring.ai.tokens.total")
                .description("Total tokens used")
                .tag("finish_reason", finishReason != null ? finishReason : "unknown")
                .register(meterRegistry)
                .increment(usage.getTotalTokens());

        // Count finish reasons as a separate counter
        Counter.builder("spring.ai.finish_reason")
                .description("AI response finish reason occurrences")
                .tag("reason", finishReason != null ? finishReason : "unknown")
                .register(meterRegistry)
                .increment();
    }
}
```

---

## 4. (Optional) Spring AI Built-in Observations

Spring AI 1.0.0 ships with **auto-configured Micrometer observations** for chat models. You can enable them so you don't
have to record manually:

```yaml
spring:
  ai:
    chat:
      observations:
        include-prompt: true     # careful with sensitive data
        include-completion: true
```

These auto-publish metrics under `gen_ai.*` following the **OpenTelemetry Semantic Conventions for GenAI**, so
Prometheus will pick them up automatically at `/actuator/prometheus`.

---

## 5. Prometheus Scrape Config

In your `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'spring-ai-app'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: [ 'localhost:8080' ]
```

---

## What You'll See in Prometheus

With either approach, you can query metrics like:

```promql
# Token usage rate over time
rate(spring_ai_tokens_total_total[5m])

# Completion tokens per minute
rate(spring_ai_tokens_completion_total[1m])

# Finish reason distribution
sum by (reason) (spring_ai_finish_reason_total)
```

---

## Summary

| Approach                             | Effort  | Control                     |
|--------------------------------------|---------|-----------------------------|
| Manual `MeterRegistry` (step 3)      | Medium  | Full — tag however you want |
| Spring AI auto-observations (step 4) | Minimal | Standard OTel naming        |

For your project, combining both works well — use auto-observations for the standard GenAI metrics and add manual
counters for any domain-specific tags (e.g. tagging by weather query type or location).