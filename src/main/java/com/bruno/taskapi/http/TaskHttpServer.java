package com.bruno.taskapi.http;

import com.bruno.taskapi.domain.Task;
import com.bruno.taskapi.domain.TaskNotFoundException;
import com.bruno.taskapi.domain.TaskService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Servidor HTTP em Java puro usando o com.sun.net.httpserver do JDK.
 * Expoe um CRUD simples de tarefas em /tasks e um health check em /health.
 */
public class TaskHttpServer {

    private final TaskService service;
    private final HttpServer server;

    public TaskHttpServer(TaskService service, int port) throws IOException {
        this.service = service;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/health", this::handleHealth);
        this.server.createContext("/tasks", this::handleTasks);
        this.server.setExecutor(null);
    }

    public void start() { server.start(); }

    public void stop() { server.stop(0); }

    public int port() { return server.getAddress().getPort(); }

    private void handleHealth(HttpExchange ex) throws IOException {
        send(ex, 200, "{" + Json.escape("") + "\"status\":\"ok\"}");
    }

    private void handleTasks(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        try {
            if (path.equals("/tasks")) {
                switch (method) {
                    case "GET" -> send(ex, 200, Json.tasks(service.list()));
                    case "POST" -> create(ex);
                    default -> send(ex, 405, Json.error("metodo nao suportado"));
                }
                return;
            }
            long id = parseId(path);
            if (id < 0) {
                send(ex, 400, Json.error("id invalido"));
                return;
            }
            switch (method) {
                case "GET" -> getOne(ex, id);
                case "POST" -> send(ex, 200, Json.task(service.complete(id)));
                case "DELETE" -> delete(ex, id);
                default -> send(ex, 405, Json.error("metodo nao suportado"));
            }
        } catch (TaskNotFoundException e) {
            send(ex, 404, Json.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            send(ex, 400, Json.error(e.getMessage()));
        }
    }

    private void create(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String title = Json.field(body, "title");
        if (title == null || title.isBlank()) {
            send(ex, 400, Json.error("title obrigatorio"));
            return;
        }
        Task created = service.create(title);
        send(ex, 201, Json.task(created));
    }

    private void getOne(HttpExchange ex, long id) throws IOException {
        Optional<Task> task = service.findById(id);
        if (task.isPresent()) {
            send(ex, 200, Json.task(task.get()));
        } else {
            send(ex, 404, Json.error("tarefa nao encontrada"));
        }
    }

    private void delete(HttpExchange ex, long id) throws IOException {
        if (service.delete(id)) {
            send(ex, 204, "");
        } else {
            send(ex, 404, Json.error("tarefa nao encontrada"));
        }
    }

    private static long parseId(String path) {
        String[] parts = path.split("/");
        if (parts.length < 3) return -1;
        try {
            return Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void send(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        if (bytes.length == 0) {
            ex.sendResponseHeaders(status, -1);
            ex.close();
            return;
        }
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
