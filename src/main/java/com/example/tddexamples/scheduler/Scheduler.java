package com.example.tddexamples.scheduler;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyList;

public final class Scheduler {

    private final DelayQueue<Action> queue;

    public Scheduler() {
        queue = new DelayQueue<>();
    }

    public void enqueue(Collection<? extends Action> actions) {
        checkNotNull(actions, "required argument 'actions' is null");
        queue.addAll(actions);
    }

    public Collection<CompletableFuture<Void>> execute() {
        return emptyList();
    }

    int getQueueSize() {
        return emptyList().size(); // WIP
    }
}
