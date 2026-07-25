package com.bruno.taskapi.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {

    @Test
    void createIncrementsIdAndStartsPending() {
        TaskService service = new TaskService();
        Task first = service.create("primeira");
        Task second = service.create("segunda");
        assertEquals(1L, first.id());
        assertEquals(2L, second.id());
        assertEquals(Task.Status.PENDING, first.status());
        assertEquals(2, service.count());
    }

    @Test
    void createRejectsBlankTitle() {
        TaskService service = new TaskService();
        assertThrows(IllegalArgumentException.class, () -> service.create("   "));
    }

    @Test
    void completeMarksTaskDone() {
        TaskService service = new TaskService();
        Task task = service.create("finalizar");
        Task done = service.complete(task.id());
        assertEquals(Task.Status.DONE, done.status());
        assertEquals(Task.Status.DONE, service.findById(task.id()).orElseThrow().status());
    }

    @Test
    void completeUnknownThrows() {
        TaskService service = new TaskService();
        TaskNotFoundException ex = assertThrows(TaskNotFoundException.class, () -> service.complete(99L));
        assertEquals(99L, ex.id());
    }

    @Test
    void deleteRemovesTask() {
        TaskService service = new TaskService();
        Task task = service.create("remover");
        assertTrue(service.delete(task.id()));
        assertFalse(service.delete(task.id()));
        assertTrue(service.findById(task.id()).isEmpty());
    }

    @Test
    void listIsOrderedById() {
        TaskService service = new TaskService();
        service.create("a");
        service.create("b");
        service.create("c");
        var ids = service.list().stream().map(Task::id).toList();
        assertEquals(java.util.List.of(1L, 2L, 3L), ids);
    }
}
