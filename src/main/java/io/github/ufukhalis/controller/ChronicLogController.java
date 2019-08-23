package io.github.ufukhalis.controller;

import io.github.ufukhalis.service.ChronicLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/log")
public class ChronicLogController {

    private ChronicLogService chronicLogService;

    public ChronicLogController(ChronicLogService chronicLogService) {
        this.chronicLogService = chronicLogService;
    }

    @GetMapping("/{taskName}")
    public Mono<List<String>> findLogs(@PathVariable("taskName") String taskName) {
        return chronicLogService.findLogs(taskName);
    }
}
