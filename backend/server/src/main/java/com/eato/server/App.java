package com.eato.server;

import com.eato.server.db.HibernateUtil;
import com.eato.server.db.RestaurantDao;
import com.eato.server.dto.PageDto;
import com.eato.server.dto.RestaurantMapper;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.hibernate.Session;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class App {

  public static void main(String[] args) throws Exception {
    int port = Integer.parseInt(
        System.getProperty("PORT", System.getenv().getOrDefault("PORT", "8080"))
    );

    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

    // Catch-all filter so errors return JSON (no more "empty reply from server")
    Filter catchAll = new Filter() {
      @Override public void doFilter(HttpExchange ex, Chain chain) {
        try {
          // Handle CORS preflight universally
          if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            cors(ex); ex.sendResponseHeaders(204, -1);
            return;
          }
          chain.doFilter(ex);
        } catch (Throwable t) {
          t.printStackTrace();
          Json.send(ex, 500, Map.of(
              "error", "Server error",
              "type", t.getClass().getSimpleName(),
              "msg", String.valueOf(t.getMessage())
          ));
        }
      }
      @Override public String description() { return "catch-all"; }
    };

    // Simple endpoints
    server.createContext("/health", ex -> {
      Json.send(ex, 200, Map.of("status", "ok"));
    }).getFilters().add(catchAll);

    server.createContext("/", ex -> {
      byte[] b = "Eato backend".getBytes(StandardCharsets.UTF_8);
      cors(ex);
      ex.sendResponseHeaders(200, b.length);
      ex.getResponseBody().write(b);
      ex.close();
    }).getFilters().add(catchAll);

    // Data access
    RestaurantDao restaurantDao = new RestaurantDao();

    // GET /api/restaurants?q=&page=&size=&city=
    server.createContext("/api/restaurants", ex -> {
      if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
        Json.send(ex, 405, Map.of("error", "Method Not Allowed"));
        return;
      }
      var qp = HttpUtil.queryParams(ex);
      String q = HttpUtil.qp(qp, "q", "");
      String city = HttpUtil.qp(qp, "city", "");
      int page = HttpUtil.qpi(qp, "page", 0);
      int size = HttpUtil.qpi(qp, "size", 10);
      if (size < 1) size = 10;
      if (size > 50) size = 50;

      try (Session s = HibernateUtil.getSessionFactory().openSession()) {
        var items = restaurantDao.search(s, q, city, page, size);
        long total = restaurantDao.count(s, q, city);
        Json.send(ex, 200, new PageDto<>(RestaurantMapper.toListDtos(items), total, page, size));
      }
    }).getFilters().add(catchAll);

    // GET /api/restaurants/{id}
    server.createContext("/api/restaurants/", ex -> {
      if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
        Json.send(ex, 405, Map.of("error", "Method Not Allowed"));
        return;
      }
      String[] parts = ex.getRequestURI().getPath().split("/");
      if (parts.length < 4) {
        Json.send(ex, 404, Map.of("error", "Not found"));
        return;
      }
      long id;
      try {
        id = Long.parseLong(parts[3]);
      } catch (NumberFormatException nfe) {
        Json.send(ex, 400, Map.of("error", "Invalid id"));
        return;
      }

      try (Session s = HibernateUtil.getSessionFactory().openSession()) {
        var opt = restaurantDao.findById(s, id);
        if (opt.isEmpty()) {
          Json.send(ex, 404, Map.of("error", "Restaurant not found"));
          return;
        }
        Json.send(ex, 200, RestaurantMapper.toDetailDto(opt.get()));
      }
    }).getFilters().add(catchAll);

    server.start();
    System.out.println("Eato backend running on http://localhost:" + port);
  }

  /** CORS headers for dev */
  private static void cors(HttpExchange ex) {
    ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
  }
}
