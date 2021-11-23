package com.example.tddexamples.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {

    private static final Instant T0 = Instant.now();

    // Subject Under Test
    private Scheduler scheduler = new Scheduler();

    @BeforeEach
    void setUp() {
        Action.clock = Clock.fixed(T0, ZoneOffset.UTC);
    }

    @Test
    void enqueueArgumentIsValidated() {
        var exception = assertThrows(NullPointerException.class, () -> scheduler.enqueue(null));
        assertEquals("required argument 'actions' is null", exception.getMessage());
        var actions = List.of(new ActionForTesting(Duration.ZERO));
        assertDoesNotThrow(() -> scheduler.enqueue(actions));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("scheduledActionsAreExecutedOnTimeArguments")
    void scheduledActionsAreExecutedOnTime(String name, Collection<Duration> durations, List<Integer> runsSize) {
        var actions = durations.stream().map(ActionForTesting::new).collect(Collectors.toList());
        scheduler.enqueue(actions);
        assertEquals(actions.size(), scheduler.getQueueSize());

        Instant t = T0; // Set t to T0
        for(Integer runSize: runsSize) {
            Action.clock = Clock.fixed(t, ZoneOffset.UTC); // Set clock to time t.
            var run = scheduler.execute();
            assertEquals(runSize, run.size(), "");
            t = t.plusSeconds(1); // Move time 1 second forward
        }
        assertEquals(0, scheduler.getQueueSize());
    }

    static Stream<Arguments> scheduledActionsAreExecutedOnTimeArguments() {
        return Stream.of(
                Arguments.of("No tasks to schedule", emptyList(), List.of(0)),
                Arguments.of("One immediate", List.of(Duration.ZERO), List.of(1)),
                Arguments.of("Two immediate", List.of(Duration.ZERO, Duration.ZERO), List.of(2)),
                Arguments.of("Two immediate, one after 5 seconds",
                        List.of(Duration.ZERO, Duration.ZERO, Duration.ofSeconds(5)),
                        List.of(2, 0, 0, 0, 0, 1))
        );
    }

    private static class ActionForTesting extends Action {

        public ActionForTesting(Duration duration) {
            super(duration);
        }

        @Override
        public void run() {
        }
    }

}