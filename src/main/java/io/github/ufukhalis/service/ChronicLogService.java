package io.github.ufukhalis.service;

import io.vavr.control.Try;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Files;

@Service
public class ChronicLogService {

    public boolean removeLogs(String taskName) {
        return Try.of(() ->
                Files.deleteIfExists(new File(taskName + ".log").toPath())
        ).getOrElse(false);
    }

    public Mono<java.util.List<String>> findLogs(String taskName) {
        return Mono.just(
                Try.of(() ->
                        Files.readAllLines(new File(taskName + ".log").toPath())
                ).getOrElseThrow(e -> new RuntimeException("File couldn't read"))
        );
    }
}
