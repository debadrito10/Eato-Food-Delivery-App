package com.eato.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Json {
  static final ObjectMapper MAPPER = new ObjectMapper();

  public static void send(HttpExchange ex, int status, Object body) {
    try {
      byte[] bytes = MAPPER.writeValueAsBytes(body);
      Headers h = ex.getResponseHeaders();
      h.set("Content-Type", "application/json; charset=utf-8");
      // CORS for dev:
      h.set("Access-Control-Allow-Origin", "http://localhost:3000");
      h.set("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
      h.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
      ex.sendResponseHeaders(status, bytes.length);
      try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    } catch (IOException e) {
      e.printStackTrace();
      try {
        String msg = "{\"error\":\"serialization failed\"}";
        ex.sendResponseHeaders(500, msg.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = ex.getResponseBody()) { os.write(msg.getBytes(StandardCharsets.UTF_8)); }
      } catch (IOException ignored) {}
    }
  }

  public static void sendText(HttpExchange ex, int status, String body) {
    try {
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      ex.getResponseHeaders().set("Content-Type","text/plain; charset=utf-8");
      ex.getResponseHeaders().set("Access-Control-Allow-Origin","*");
      ex.sendResponseHeaders(status, bytes.length);
      try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    } catch (IOException ignored) {}
  }
}
