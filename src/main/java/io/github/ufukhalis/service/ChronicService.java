package io.github.ufukhalis.service;

import io.github.ufukhalis.dto.Task;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
@Service
public class ChronicService {

    private Map<String, Disposable> TASK_MAP = new ConcurrentHashMap<>();
    private Map<String, Process> PROCESS_MAP = new ConcurrentHashMap<>();

    private ChronicLogService chronicLogService;

    public ChronicService(ChronicLogService chronicLogService) {
        this.chronicLogService = chronicLogService;
    }

    public Mono<Task> createTask(Task task) {
        if (this.TASK_MAP.get(task.getName()) != null) {
            return Mono.error(new RuntimeException("You can't create task with same name!"));
        }

        return Mono.fromCallable(() -> createScheduler(task))
                .map(fluxTask -> {
                    this.TASK_MAP.put(task.getName(), fluxTask);
                    log.info("Task {} has been created", task.getName());
                    return task;
                });
    }

    public Mono<Void> deleteTask(String taskName) {
        return Mono.fromRunnable(() ->
            Optional.ofNullable(this.TASK_MAP.get(taskName))
                    .ifPresent(disposable -> {
                        stopTask(disposable, taskName);
                    })
        );
    }

    private Disposable createScheduler(Task task) {
        return Flux.interval(Duration.ofSeconds(task.getPeriod()))
                .doOnEach(ignore -> {
                    final Process process = this.PROCESS_MAP.get(task.getName());
                    if (process != null) {
                        if (process.isAlive()) {
                            log.warn("Task {} process still alive.. Do nothing", task.getName());
                            return;
                        } else {
                            log.warn("Task {} looks like finished, destroying...", task.getName());
                            process.destroy();
                            this.PROCESS_MAP.remove(task.getName());
                        }
                    }

                    log.info("Task {} has started...", task.getName());
                    final Process runningProcess = runProcess(task.getName(), task.getCommand());
                    this.PROCESS_MAP.put(task.getName(), runningProcess);
                }).subscribeOn(Schedulers.newSingle(task.getName()))
                .subscribe();
    }

    private Process runProcess(String taskName, String command) {
        return Try.of(() -> {
            File logFile = new File(taskName + ".log");

            ProcessBuilder processBuilder = new ProcessBuilder(cmdFunc.apply(command))
                    .redirectOutput(ProcessBuilder.Redirect.appendTo(logFile))
                    .redirectError(ProcessBuilder.Redirect.appendTo(logFile));

            return processBuilder.start();
        }).getOrElseThrow(e -> new RuntimeException(e));
    }

    private void stopTask(Disposable disposable, String taskName) {
        disposable.dispose();
        this.TASK_MAP.remove(taskName);
        stopProcess(taskName);
        this.chronicLogService.removeLogs(taskName);
        log.warn("Task {} has been deleted", taskName);
    }

    private void stopProcess(String taskName) {
        final Process process = this.PROCESS_MAP.get(taskName);
        if (process != null) {
            process.destroy();
        }
        this.PROCESS_MAP.remove(taskName);
    }

    private Function<String, String[]> cmdFunc = command -> {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        return isWindows ? new String[] { "cmd", "/c", command} : new String[] { "sh", "-c", command };
    };
}
