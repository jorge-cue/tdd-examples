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
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActionTest {

    private static final Instant I0 = Instant.now();

    @BeforeEach
    void setUp() {
        Action.clock = Clock.fixed(I0, ZoneOffset.UTC);
    }

    @Test
    void constructorArgumentsAreValidated() {
        var exception = assertThrows(NullPointerException.class, () -> new ActionForTest(null));
        assertEquals("required argument 'duration' is null", exception.getMessage());
    }

    @Test
    void getDelayArgumentIsValidated() {
        var action = new ActionForTest(Duration.ZERO);
        var exception = assertThrows(NullPointerException.class, () -> action.getDelay(null));
        assertEquals("required argument 'units' is null", exception.getMessage());
    }

    @Test
    void compareToArgumentIsValidated() {
        var action = new ActionForTest(Duration.ZERO);
        var exception1 = assertThrows(NullPointerException.class, () -> action.compareTo(null));
        assertEquals("required argument 'other' is null", exception1.getMessage());
        var other = new Delayed() {
            @Override
            public long getDelay(TimeUnit timeUnit) {
                return 0;
            }

            @Override
            public int compareTo(Delayed delayed) {
                return 0;
            }
        };
        var exception2 = assertThrows(IllegalArgumentException.class, () -> action.compareTo(other));
        assertEquals("argument 'other' is not an Action", exception2.getMessage());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("delayDecreasesWithClockMovingForwardArguments")
    void delayDecreasesWhenClockMovesForward(final String name, final Duration duration, final Long seconds) {
        var action = new ActionForTest(duration);
        Long s = seconds;
        Instant i = I0;
        do {
            assertEquals(s, action.getDelay(TimeUnit.SECONDS));
            i = i.plusSeconds(1L);
            Action.clock = Clock.fixed(i, ZoneOffset.UTC);
            s = s - 1;
        } while(s >= 0);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("actionOrderIsByDelayArguments")
    void actionOrderIsByDelay(final String name, final Duration one, final Duration two, final Integer expected) {
        var action1 = new ActionForTest(one);
        var action2 = new ActionForTest(two);
        var actual = action1.compareTo(action2);
        assertEquals(expected, actual);
    }

    static Stream<Arguments> delayDecreasesWithClockMovingForwardArguments() {
        return Stream.of(
                Arguments.of("Immediate", Duration.ZERO, 0L),
                Arguments.of("1 Second", Duration.ofSeconds(1), 1L),
                Arguments.of("5 Seconds", Duration.ofSeconds(5), 5L)
        );
    }

    static Stream<Arguments> actionOrderIsByDelayArguments() {
        return Stream.of(
                Arguments.of("Simultaneous", Duration.ofSeconds(1), Duration.ofSeconds(1), 0),
                Arguments.of("One before two", Duration.ofSeconds(1), Duration.ofSeconds(2), -1),
                Arguments.of("Two before one", Duration.ofSeconds(2), Duration.ofSeconds(1), 1)
        );
    }

    private static class ActionForTest extends Action {

        public ActionForTest(Duration duration) {
            super(duration);
        }

        @Override
        public void run() {
        }
    }
}