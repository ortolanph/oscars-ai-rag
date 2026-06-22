package pt.pauloortolan.oscarsairag.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.pauloortolan.oscarsairag.service.OscarsRagService;

@Slf4j
@RestController
@RequestMapping("/oscars")
@RequiredArgsConstructor
public class OscarController {

    private final OscarsRagService service;

    @GetMapping("/tellme/{username}")
    public ResponseEntity<String> tellMe(@PathVariable String username, @RequestParam String what) {
        log.info("AskController.tellMe(username={},what={})", username, what);

        if(username == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(service.ask(username, what));
    }
}
