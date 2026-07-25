package com.bruno.taskapi.http;

import com.bruno.taskapi.domain.TaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class TaskHttpServerTest {

    private TaskHttpServer server;
    private HttpClient client;
    private String base;

    @BeforeEach
    void setUp() throws Exception {
        server = new TaskHttpServer(new TaskService(), 0);
        server.start();
        client = HttpClient.newHttpClient();
        base = "http://localhost:" + server.port();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        HttpRequest.BodyPublisher pub = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        HttpRequest req = HttpRequest.newBuilder(URI.create(base + path))
                .method(method, pub)
                .header("Content-Type", "application/json")
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void healthReturnsOk() throws Exception {
        HttpResponse<String> res = send("GET", "/health", null);
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("ok"));
    }

    @Test
    void createThenListThenComplete() throws Exception {
        HttpResponse<String> created = send("POST", "/tasks", "{\"title\":\"estudar\"}");
        assertEquals(201, created.statusCode());
        assertTrue(created.body().contains("estudar"));
        assertTrue(created.body().contains("PENDING"));

        HttpResponse<String> list = send("GET", "/tasks", null);
        assertEquals(200, list.statusCode());
        assertTrue(list.body().contains("estudar"));

        HttpResponse<String> done = send("POST", "/tasks/1", null);
        assertEquals(200, done.statusCode());
        assertTrue(done.body().contains("DONE"));
    }

    @Test
    void createWithoutTitleReturns400() throws Exception {
        HttpResponse<String> res = send("POST", "/tasks", "{}");
        assertEquals(400, res.statusCode());
    }

    @Test
    void completeUnknownReturns404() throws Exception {
        HttpResponse<String> res = send("POST", "/tasks/999", null);
        assertEquals(404, res.statusCode());
    }

    @Test
    void deleteReturns204() throws Exception {
        send("POST", "/tasks", "{\"title\":\"x\"}");
        HttpResponse<String> res = send("DELETE", "/tasks/1", null);
        assertEquals(204, res.statusCode());
    }
}
