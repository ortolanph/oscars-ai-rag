package pt.pauloortolan.oscarsairag.controller;

import com.example.oscarsrag.service.OscarsRagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AskController {

    private final OscarsRagService ragService;

    public AskController(OscarsRagService ragService) {
        this.ragService = ragService;
    }

    /**
     * Ask a question about the Oscars dataset.
     *
     * Example: GET /ask?prompt=Who won Best Picture in 1994?
     */
    @GetMapping("/ask")
    public String ask(@RequestParam String prompt) {
        return ragService.ask(prompt);
    }
}
