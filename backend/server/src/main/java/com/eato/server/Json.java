package com.eato.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; 
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Json {
  static final ObjectMapper MAPPER = new ObjectMapper()
  .registerModule(new JavaTimeModule())                  
  .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  
  public static void send(HttpExchange ex, int status, Object body) {
    try {
      byte[] bytes = MAPPER.writeValueAsBytes(body);
      ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
      ex.sendResponseHeaders(status, bytes.length);
      try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    } catch (IOException e) {
      e.printStackTrace();
      try {
        byte[] msg = "{\"error\":\"serialization failed\"}".getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(500, msg.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(msg); }
      } catch (IOException ignored) {}
    }
  }

  public static void sendText(HttpExchange ex, int status, String body) {
    try {
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      ex.getResponseHeaders().set("Content-Type","text/plain; charset=utf-8");
      ex.sendResponseHeaders(status, bytes.length);
      try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    } catch (IOException ignored) {}
  }
}
