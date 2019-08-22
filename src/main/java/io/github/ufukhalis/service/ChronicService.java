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
import java.util.function.Function;

@Service
public class ChronicService {
    private Map<String, Disposable> TASK_MAP = new ConcurrentHashMap<>();

    public Mono<Task> createTask(Task task) {

        return Mono.fromCallable(() -> createScheduler(task))
                .map(fluxTask -> {
                    this.TASK_MAP.put(task.getName(), fluxTask);
                    return task;
                });
    }

    public Mono<Void> deleteTask(String taskName) {
        return Mono.fromRunnable(() ->
            Optional.ofNullable(this.TASK_MAP.get(taskName))
                    .ifPresent(disposable -> {
                        disposable.dispose();
                        this.TASK_MAP.remove(taskName);
                    })
        );
    }

    private Disposable createScheduler(Task task) {
        return Flux.interval(Duration.ofSeconds(task.getPeriod()))
                .doOnEach(ignore -> {
                   int result = runCommand(task.getCommand());
                   System.out.println("Task name : " + task.getName() + " result : " + result);
                }).log()
                .subscribeOn(Schedulers.newSingle(task.getName()))
                .subscribe();
    }

    private int runCommand(String command) {
        return Try.of(() -> new ProcessBuilder(cmdFunc.apply(command)).inheritIO().start().waitFor())
                .getOrElseThrow(e -> new RuntimeException(e));
    }

    private Function<String, String[]> cmdFunc = command -> {
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");

        return isWindows ? new String[] { "cmd", "/c", command} : new String[] { "sh", "-c", command };
    };
}
