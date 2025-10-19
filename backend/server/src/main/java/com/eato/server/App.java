package com.eato.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class App {
  public static void main(String[] args) throws Exception {
    int port = Integer.parseInt(
    System.getProperty("PORT",
        System.getenv().getOrDefault("PORT", "8080"))
);

    var server = HttpServer.create(new InetSocketAddress(port), 0);

    // GET /health -> {"status":"ok"}
    server.createContext("/health", ex -> json(ex, 200, Map.of("status","ok")));

    // CORS + default root
    server.createContext("/", ex -> {
      if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) { cors(ex); ex.sendResponseHeaders(204,-1); return; }
      byte[] b = "Eato backend".getBytes(StandardCharsets.UTF_8);
      cors(ex); ex.sendResponseHeaders(200, b.length); ex.getResponseBody().write(b); ex.close();
    });

    server.start();
    System.out.println("Eato backend running on http://localhost:"+port);
  }

  private static void json(HttpExchange ex, int status, Object body) throws java.io.IOException {
    byte[] bytes = Json.M.writeValueAsBytes(body);
    cors(ex); ex.getResponseHeaders().add("Content-Type","application/json");
    ex.sendResponseHeaders(status, bytes.length); ex.getResponseBody().write(bytes); ex.close();
  }
  private static void cors(HttpExchange ex){
    ex.getResponseHeaders().add("Access-Control-Allow-Origin","*");
    ex.getResponseHeaders().add("Access-Control-Allow-Headers","Content-Type, Authorization");
    ex.getResponseHeaders().add("Access-Control-Allow-Methods","GET,POST,OPTIONS");
  }
}
