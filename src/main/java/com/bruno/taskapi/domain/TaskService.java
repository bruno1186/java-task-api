package com.bruno.taskapi.domain;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Regras de negocio para tarefas com armazenamento em memoria (thread-safe).
 * Sem dependencia de framework: a persistencia real pode ser plugada
 * trocando o mapa por um repositorio.
 */
public class TaskService {

    private final ConcurrentHashMap<Long, Task> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public Task create(String title) {
        long id = sequence.incrementAndGet();
        Task task = Task.create(id, title);
        store.put(id, task);
        return task;
    }

    public List<Task> list() {
        return store.values().stream()
                .sorted((a, b) -> Long.compare(a.id(), b.id()))
                .collect(Collectors.toList());
    }

    public Optional<Task> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Task complete(long id) {
        Task current = store.get(id);
        if (current == null) {
            throw new TaskNotFoundException(id);
        }
        Task done = current.markDone();
        store.put(id, done);
        return done;
    }

    public boolean delete(long id) {
        return store.remove(id) != null;
    }

    public int count() {
        return store.size();
    }
}
