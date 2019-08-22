package io.github.ufukhalis.controller;

import io.github.ufukhalis.dto.Task;
import io.github.ufukhalis.service.ChronicService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class ChronicController {

    private ChronicService chronicService;

    public ChronicController(ChronicService chronicService) {
        this.chronicService = chronicService;
    }

    @PostMapping("/")
    public Mono<Task> createTask(@RequestBody Task task) {
        return chronicService.createTask(task);
    }

    @DeleteMapping("/{taskName}")
    public Mono<Void> deleteTask(@PathVariable("taskName") String taskName) {
        return chronicService.deleteTask(taskName);
    }
}
