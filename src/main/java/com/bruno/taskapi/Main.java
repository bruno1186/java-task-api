package com.bruno.taskapi;

import com.bruno.taskapi.domain.TaskService;
import com.bruno.taskapi.http.TaskHttpServer;

/** Ponto de entrada: sobe o servidor HTTP na porta definida por PORT (default 8080). */
public class Main {

    public static void main(String[] args) throws Exception {
        int port = readPort();
        TaskService service = new TaskService();
        TaskHttpServer server = new TaskHttpServer(service, port);
        server.start();
        System.out.println("java-task-api ouvindo em http://localhost:" + server.port());
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }

    private static int readPort() {
        String env = System.getenv("PORT");
        if (env == null || env.isBlank()) {
            return 8080;
        }
        try {
            return Integer.parseInt(env.trim());
        } catch (NumberFormatException e) {
            return 8080;
        }
    }
}
