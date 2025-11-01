package com.eato.server;

import com.eato.server.db.HibernateUtil;
import com.eato.server.db.RestaurantDao;
import com.eato.server.dto.PageDto;
import com.eato.server.dto.RestaurantMapper;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.hibernate.Session;
import org.mindrot.jbcrypt.BCrypt;
import com.eato.server.db.UserDao;
import com.eato.server.model.User;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.eato.server.AuthUtil;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import com.eato.server.HttpUtil;
import com.eato.server.db.OrderDao;
import com.eato.server.db.OrderItemSpec;
import com.eato.server.dto.OrderDtos;
import com.eato.server.dto.OrderMapper;
import com.eato.server.db.IdempotencyDao;


public class App {


  // top of class (with imports)
private static final String FRONTEND_ORIGIN =
    System.getenv().getOrDefault("FRONTEND_ORIGIN", "http://localhost:3000");
  public static void main(String[] args) throws Exception {
    int port = Integer.parseInt(
        System.getProperty("PORT", System.getenv().getOrDefault("PORT", "8080"))
    );

    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

    // Catch-all filter so errors return JSON (no more "empty reply from server")
    Filter catchAll = new Filter() {
      @Override public void doFilter(HttpExchange ex, Chain chain) {
        try {
          cors(ex);
          // Handle CORS preflight universally
          if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
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
    UserDao userDao = new UserDao();

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
    // POST /api/auth/register
server.createContext("/api/auth/register", ex -> {
  if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { Json.send(ex, 405, Map.of("error","Method Not Allowed")); return; }
  try (Session s = HibernateUtil.getSessionFactory().openSession()) {
    var req = HttpUtil.readJson(ex, com.eato.server.dto.AuthDtos.RegisterReq.class);
    if (req.email == null || req.email.isBlank() || req.password == null || req.password.length() < 6) {
      Json.send(ex, 400, Map.of("error","Invalid email or password too short")); return;
    }
    if (userDao.findByEmail(s, req.email).isPresent()) {
      Json.send(ex, 409, Map.of("error","Email already registered")); return;
    }
    var u = new User();
    u.setEmail(req.email.trim().toLowerCase());
    u.setPasswordHash(BCrypt.hashpw(req.password, BCrypt.gensalt(10)));
    userDao.save(s, u);
    String jwt = AuthUtil.issueToken(u.getId(), u.getEmail());
    Json.send(ex, 201, new com.eato.server.dto.AuthDtos.TokenResp(jwt, u.getEmail(), u.getId()));
  } catch (Exception e) {
    e.printStackTrace(); Json.send(ex, 500, Map.of("error","Server error"));
  }
}).getFilters().add(catchAll);

// POST /api/auth/login
server.createContext("/api/auth/login", ex -> {
  if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { Json.send(ex, 405, Map.of("error","Method Not Allowed")); return; }
  try (Session s = HibernateUtil.getSessionFactory().openSession()) {
    var req = HttpUtil.readJson(ex, com.eato.server.dto.AuthDtos.LoginReq.class);
    var opt = (req.email == null) ? java.util.Optional.<User>empty() : userDao.findByEmail(s, req.email);
    if (opt.isEmpty()) { Json.send(ex, 401, Map.of("error","Invalid credentials")); return; }
    var u = opt.get();
    if (!BCrypt.checkpw(req.password, u.getPasswordHash())) {
      Json.send(ex, 401, Map.of("error","Invalid credentials")); return;
    }
    String jwt = AuthUtil.issueToken(u.getId(), u.getEmail());
    Json.send(ex, 200, new com.eato.server.dto.AuthDtos.TokenResp(jwt, u.getEmail(), u.getId()));
  } catch (Exception e) {
    e.printStackTrace(); Json.send(ex, 500, Map.of("error","Server error"));
  }
}).getFilters().add(catchAll);
   
   server.createContext("/api/me", ex -> {
  if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) { Json.send(ex, 405, Map.of("error","Method Not Allowed")); return; }
  Long uid = authUserId(ex);
  if (uid == null) { Json.send(ex, 401, Map.of("error","Unauthorized")); return; }
  try (Session s = HibernateUtil.getSessionFactory().openSession()) {
    var opt = new UserDao().findById(s, uid);
    if (opt.isEmpty()) { Json.send(ex, 401, Map.of("error","Unauthorized")); return; }
    var me = new com.eato.server.dto.AuthDtos.MeResp();
    me.id = opt.get().getId();
    me.email = opt.get().getEmail();
    Json.send(ex, 200, me);
  }
}).getFilters().add(catchAll);

  // POST /api/cart/price  (optional preview; no auth required)
server.createContext("/api/cart/price", ex -> {
  if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
    Json.send(ex, 405, Map.of("error","Method Not Allowed")); return;
  }
  try (Session s = HibernateUtil.getSessionFactory().openSession()) {
    var req = HttpUtil.readJson(ex, OrderDtos.OrderCreateReq.class);
    if (req == null || req.items == null || req.items.isEmpty()) {
      Json.send(ex, 400, Map.of("error","items required")); return;
    }
    var specs = new java.util.ArrayList<OrderItemSpec>();
    for (var it : req.items) specs.add(new OrderItemSpec(it.dishId, it.qty));
    int total = new OrderDao().priceForCart(s, specs);
    Json.send(ex, 200, java.util.Map.of("total", total));
  } catch (IllegalArgumentException iae) {
    Json.send(ex, 400, Map.of("error", iae.getMessage()));
  } catch (Exception e) {
    e.printStackTrace(); Json.send(ex, 500, Map.of("error","Server error"));
  }
}).getFilters().add(catchAll);

// POST /api/orders  (JWT required)  and  GET /api/orders?page=&size= (JWT)
server.createContext("/api/orders", ex -> {
  if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
    Long uid = authUserId(ex);
    if (uid == null) { Json.send(ex, 401, Map.of("error","Unauthorized")); return; }
    
    String idemKey = ex.getRequestHeaders().getFirst("Idempotency-Key");

    try (Session s = HibernateUtil.getSessionFactory().openSession()) {
      var req = HttpUtil.readJson(ex, OrderDtos.OrderCreateReq.class);
      if (req == null || req.items == null || req.items.isEmpty()) {
        Json.send(ex, 400, Map.of("error","items required")); return;
      }
      // If client sent an Idempotency-Key, de-dupe
    if (idemKey != null && !idemKey.isBlank()) {
      var idDao = new IdempotencyDao();

      // First caller wins; others see existing result
      boolean inserted = idDao.tryInsert(s, idemKey, uid);
      if (!inserted) {
        Long existingOrderId = idDao.getOrderId(s, idemKey);
        if (existingOrderId != null) {
          var od = new OrderDao().findDetail(s, uid, existingOrderId);
          if (od.isPresent()) {
            Json.send(ex, 200, new OrderDtos.OrderCreateResp(existingOrderId, od.get().getTotal()));
            return;
          }
          // key exists but race not completed → fall through to create
        }
      }

      // Create and bind the idempotency key
      var specs = new java.util.ArrayList<OrderItemSpec>();
      for (var it : req.items) specs.add(new OrderItemSpec(it.dishId, it.qty));
      var created = new OrderDao().create(s, uid, specs, req.address);
      idDao.setOrderId(s, idemKey, created.getId());
      Json.send(ex, 201, new OrderDtos.OrderCreateResp(created.getId(), created.getTotal()));
      return;
    }

    // No Idempotency-Key → normal create
    var specs = new java.util.ArrayList<OrderItemSpec>();
    for (var it : req.items) specs.add(new OrderItemSpec(it.dishId, it.qty));
    var created = new OrderDao().create(s, uid, specs, req.address);
    Json.send(ex, 201, new OrderDtos.OrderCreateResp(created.getId(), created.getTotal()));
  } catch (IllegalArgumentException iae) {
    Json.send(ex, 400, Map.of("error", iae.getMessage()));
  } catch (Exception e) {
    e.printStackTrace(); Json.send(ex, 500, Map.of("error","Server error"));
  }
  return;
}

  if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
    Long uid = authUserId(ex);
    if (uid == null) { Json.send(ex, 401, Map.of("error","Unauthorized")); return; }
    var qp = HttpUtil.queryParams(ex);
    int page = HttpUtil.qpi(qp, "page", 0);
    int size = HttpUtil.qpi(qp, "size", 10);
    if (size < 1) size = 10; if (size > 50) size = 50;

    try (Session s = HibernateUtil.getSessionFactory().openSession()) {
      var dao = new OrderDao();
      var pg = dao.findByUser(s, uid, page, size);
      Json.send(ex, 200, new PageDto<>(OrderMapper.toListItems(pg.items), pg.total, page, size));
    } catch (Exception e) {
      e.printStackTrace(); Json.send(ex, 500, Map.of("error","Server error"));
    }
    return;
  }

  Json.send(ex, 405, Map.of("error","Method Not Allowed"));
}).getFilters().add(catchAll);

// GET /api/orders/{id}  (JWT required & owner-guarded)
server.createContext("/api/orders/", ex -> {
  if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
    Json.send(ex, 405, Map.of("error","Method Not Allowed")); return;
  }
  Long uid = authUserId(ex);
  if (uid == null) { Json.send(ex, 401, Map.of("error","Unauthorized")); return; }

  String[] parts = ex.getRequestURI().getPath().split("/");
  if (parts.length < 4) { Json.send(ex, 404, Map.of("error","Not found")); return; }

  long id;
  try { id = Long.parseLong(parts[3]); }
  catch (NumberFormatException nfe) { Json.send(ex, 400, Map.of("error","Invalid id")); return; }

  try (Session s = HibernateUtil.getSessionFactory().openSession()) {
    var opt = new OrderDao().findDetail(s, uid, id);
    if (opt.isEmpty()) { Json.send(ex, 404, Map.of("error","Not found")); return; }
    Json.send(ex, 200, OrderMapper.toDetail(s, opt.get()));
  } catch (Exception e) {
    e.printStackTrace(); Json.send(ex, 500, Map.of("error","Server error"));
  }
}).getFilters().add(catchAll);

    server.start();
    System.out.println("Eato backend running on http://localhost:" + port);
  }

  /** CORS headers for dev */
  private static void cors(HttpExchange ex) {
    ex.getResponseHeaders().set("Access-Control-Allow-Origin", FRONTEND_ORIGIN);
    ex.getResponseHeaders().set("Vary", "Origin");
    ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization, Idempotency-Key");
    ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
  }
  // Helper to get user id from Authorization header
private static Long authUserId(HttpExchange ex) {
  String h = ex.getRequestHeaders().getFirst("Authorization");
  if (h == null || !h.startsWith("Bearer ")) return null;
  String token = h.substring("Bearer ".length()).trim();
  try {
    DecodedJWT jwt = AuthUtil.verify(token);
    return jwt.getClaim("uid").asLong();
  } catch (Exception e) { return null; }
}

}

