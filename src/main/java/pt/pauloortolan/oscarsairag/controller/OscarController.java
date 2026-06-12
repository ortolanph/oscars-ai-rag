package pt.pauloortolan.oscarsairag.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/tellme")
    public String tellMe(@RequestParam String what) {
        log.info("AskController.tellMe(what={})", what);
        return service.ask(what);
    }
}
