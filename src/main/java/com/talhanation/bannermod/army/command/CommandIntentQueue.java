package com.talhanation.bannermod.army.command;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Ordered command queue for a single recruit.
 *
 * <p>The head of the deque is the currently-executing intent (if any). Subsequent entries
 * are pending waypoints that will be applied when the head completes. Queue manipulation is
 * done through {@link CommandIntentQueueRuntime}; the queue itself is a dumb container.</p>
 *
 * <p>Not thread-safe — the runtime serialises access on the server tick.</p>
 */
public final class CommandIntentQueue {
    private final Deque<CommandIntent> deque = new ArrayDeque<>();

    public void clear() {
        deque.clear();
    }

    public void append(CommandIntent intent) {
        Objects.requireNonNull(intent, "intent");
        deque.addLast(intent);
    }

    public void prepend(CommandIntent intent) {
        Objects.requireNonNull(intent, "intent");
        deque.addFirst(intent);
    }

    public Optional<CommandIntent> head() {
        return Optional.ofNullable(deque.peekFirst());
    }

    public Optional<CommandIntent> popHead() {
        return Optional.ofNullable(deque.pollFirst());
    }

    public int size() {
        return deque.size();
    }

    public boolean isEmpty() {
        return deque.isEmpty();
    }

    public List<CommandIntent> snapshot() {
        return Collections.unmodifiableList(List.copyOf(deque));
    }
}
