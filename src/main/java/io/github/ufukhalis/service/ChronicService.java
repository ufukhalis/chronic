package io.github.ufukhalis.service;

import io.github.ufukhalis.dto.Task;
import io.vavr.control.Try;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChronicService {
    private Map<String, Disposable> FLUX_TASK_MAP = new ConcurrentHashMap<>();

    public Mono<Task> createTask(Task task) {

        return Mono.fromCallable(() -> createScheduler(task))
                .map(fluxTask -> {
                    this.FLUX_TASK_MAP.put(task.getName(), fluxTask);
                    return task;
                });
    }

    public Mono<Void> deleteTask(String taskName) {
        return Mono.fromRunnable(() ->
            Optional.ofNullable(this.FLUX_TASK_MAP.get(taskName))
                    .ifPresent(disposable -> {
                        disposable.dispose();
                        this.FLUX_TASK_MAP.remove(taskName);
                    })
        );
    }

    private Disposable createScheduler(Task task) {
        return Flux.interval(Duration.ofSeconds(task.getPeriod()))
                .doOnEach(ignore -> {
                   int result = runCommand(task.getCommand());
                   System.out.println("Task name : " + task.getName() + " result : " + result);
                })
                .subscribeOn(Schedulers.single())
                .subscribe();
    }

    private int runCommand(String command) {
        return Try.of(() -> new ProcessBuilder("cmd", "/c", command).inheritIO().start().waitFor())
                .getOrElseThrow(e -> new RuntimeException(e));
    }
}
