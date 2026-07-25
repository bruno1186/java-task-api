package com.bruno.taskapi.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Representa uma tarefa. Imutavel exceto pela transicao de status,
 * que retorna uma nova instancia para preservar a integridade do dominio.
 */
public final class Task {

    private final long id;
    private final String title;
    private final Status status;
    private final Instant createdAt;

    public enum Status { PENDING, DONE }

    public Task(long id, String title, Status status, Instant createdAt) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title nao pode ser vazio");
        }
        this.id = id;
        this.title = title.trim();
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public static Task create(long id, String title) {
        return new Task(id, title, Status.PENDING, Instant.now());
    }

    public Task markDone() {
        if (status == Status.DONE) {
            return this;
        }
        return new Task(id, title, Status.DONE, createdAt);
    }

    public long id() { return id; }
    public String title() { return title; }
    public Status status() { return status; }
    public Instant createdAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "Task{id=" + id + ", title='" + title + "', status=" + status + "}";
    }
}
