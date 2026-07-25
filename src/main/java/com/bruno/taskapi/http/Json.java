package com.bruno.taskapi.http;

import com.bruno.taskapi.domain.Task;
import java.util.List;

/**
 * Serializacao JSON minima feita a mao, para manter o projeto sem
 * dependencias de runtime. Suficiente para o dominio de tarefas.
 */
public final class Json {

    private static final char QUOTE = '\"';
    private static final char BACKSLASH = '\\';

    private Json() {}

    public static String escape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == QUOTE || c == BACKSLASH) {
                sb.append(BACKSLASH).append(c);
            } else if (c == '\n') {
                sb.append(BACKSLASH).append('n');
            } else if (c == '\r') {
                sb.append(BACKSLASH).append('r');
            } else if (c == '\t') {
                sb.append(BACKSLASH).append('t');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String quoted(String s) {
        return QUOTE + escape(s) + QUOTE;
    }

    public static String task(Task t) {
        return new StringBuilder()
                .append('{')
                .append(quoted("id")).append(':').append(t.id()).append(',')
                .append(quoted("title")).append(':').append(quoted(t.title())).append(',')
                .append(quoted("status")).append(':').append(quoted(t.status().name())).append(',')
                .append(quoted("createdAt")).append(':').append(quoted(t.createdAt().toString()))
                .append('}')
                .toString();
    }

    public static String tasks(List<Task> tasks) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < tasks.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(task(tasks.get(i)));
        }
        return sb.append(']').toString();
    }

    public static String error(String message) {
        return '{' + quoted("error") + ':' + quoted(message) + '}';
    }

    /** Extrai o valor de um campo string simples de um corpo JSON plano. */
    public static String field(String body, String key) {
        String needle = QUOTE + key + QUOTE;
        int k = body.indexOf(needle);
        if (k < 0) return null;
        int colon = body.indexOf(':', k + needle.length());
        if (colon < 0) return null;
        int q1 = body.indexOf(QUOTE, colon + 1);
        if (q1 < 0) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = q1 + 1; i < body.length(); i++) {
            char c = body.charAt(i);
            if (c == BACKSLASH && i + 1 < body.length()) { sb.append(body.charAt(++i)); continue; }
            if (c == QUOTE) break;
            sb.append(c);
        }
        return sb.toString();
    }
}
