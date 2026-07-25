package com.bruno.taskapi.domain;

/** Lancada quando uma tarefa nao existe no repositorio. */
public class TaskNotFoundException extends RuntimeException {

    private final long id;

    public TaskNotFoundException(long id) {
        super("Tarefa nao encontrada: " + id);
        this.id = id;
    }

    public long id() {
        return id;
    }
}
