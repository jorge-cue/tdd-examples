package com.example.tddexamples.scheduler;

import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Scheduler {

    private final DelayQueue<Action> queue;

    private final Executor executor;

    public Scheduler() {
        queue = new DelayQueue<>();
        executor = Executors.newWorkStealingPool();
    }

    public void enqueue(Collection<? extends Action> actions) {
        checkNotNull(actions, "required argument 'actions' is null");
        queue.addAll(actions);
    }

    @Scheduled(fixedRateString = "${application.scheduler.rate}")
    public void executeAtRate() {
        execute();
    }

    Collection<CompletableFuture<Void>> execute() {
        var ready = new ArrayList<Action>();
        queue.drainTo(ready);
        return ready.stream().map(r -> CompletableFuture.runAsync(r, executor)).collect(Collectors.toList());
    }

    int getQueueSize() {
        return queue.size();
    }
}
