package com.eato.server.db;

import com.eato.server.model.Order;
import com.eato.server.model.OrderItem;
import com.eato.server.model.Dish; // you already have this entity
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.*;
import java.util.stream.Collectors;

public class OrderDao {

  public static class Page<T> {
    public final List<T> items;
    public final long total;
    public Page(List<T> items, long total){ this.items=items; this.total=total; }
  }

  /** Pure pricing (no persistence). Throws IllegalArgumentException on bad input. */
  public int priceForCart(Session s, List<OrderItemSpec> items) {
    var validated = validateItems(items);
    Map<Long, Dish> dishById = loadDishes(s, validated.keySet());
    int total = 0;
    for (var e : validated.entrySet()) {
      Dish d = dishById.get(e.getKey());
      if (d == null) throw new IllegalArgumentException("Unknown dishId: " + e.getKey());
      int qty = e.getValue();
      if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");
      total += qty * d.getprice(); // price is integer rupees
    }
    return total;
  }

  /** Creates order + items in a single transaction, returns the persisted order. */
  public Order create(Session s, long userId, List<OrderItemSpec> items, String addressJson) {
    var validated = validateItems(items);
    Map<Long, Dish> dishById = loadDishes(s, validated.keySet());

    Transaction tx = s.beginTransaction();
    try {
      // compute total from authoritative DB prices
      int total = 0;
      for (var e : validated.entrySet()) {
        Dish d = dishById.get(e.getKey());
        if (d == null) throw new IllegalArgumentException("Unknown dishId: " + e.getKey());
        int qty = e.getValue();
        if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");
        total += qty * d.getprice();
      }

      Order o = new Order();
      o.setUserId(userId);
      o.setTotal(total);
      if (addressJson != null && !addressJson.isBlank()) o.setAddress(addressJson);
      s.persist(o);

      // attach items
      for (var e : validated.entrySet()) {
        Dish d = dishById.get(e.getKey());
        OrderItem oi = new OrderItem();
        oi.setOrder(o);
        oi.setDishId(d.getId());
        oi.setQty(e.getValue());
        oi.setPriceAtOrder(d.getprice());
        s.persist(oi);
      }

      tx.commit();
      return o;
    } catch (RuntimeException ex) {
      if (tx!=null && tx.isActive()) tx.rollback();
      throw ex;
    }
  }

  /** Paged list of current user's orders (newest first) */
  public Page<Order> findByUser(Session s, long userId, int page, int size){
    var list = s.createQuery(
        "from Order o where o.userId = :uid order by o.createdAt desc", Order.class)
        .setParameter("uid", userId)
        .setFirstResult(Math.max(0, page)*Math.max(1, size))
        .setMaxResults(size)
        .list();
    long total = s.createQuery(
        "select count(o.id) from Order o where o.userId = :uid", Long.class)
        .setParameter("uid", userId)
        .uniqueResult();
    return new Page<>(list, total);
  }

  /** Owner-guarded detail fetch */
  public Optional<Order> findDetail(Session s, long userId, long orderId){
    Order o = s.get(Order.class, orderId);
    if (o == null) return Optional.empty();
    if (!Objects.equals(o.getUserId(), userId)) return Optional.empty();
    // initialize items (if needed later)
    o.getItems().size();
    return Optional.of(o);
  }

  // ---- helpers ----

  private Map<Long,Integer> validateItems(List<OrderItemSpec> items){
    if (items == null || items.isEmpty()) throw new IllegalArgumentException("items required");
    Map<Long,Integer> m = new LinkedHashMap<>();
    for (OrderItemSpec it : items){
      if (it == null) continue;
      if (it.qty <= 0) throw new IllegalArgumentException("qty must be > 0");
      m.merge(it.dishId, it.qty, Integer::sum);
    }
    return m;
  }

  private Map<Long, Dish> loadDishes(Session s, Collection<Long> ids){
    if (ids.isEmpty()) return Map.of();
    List<Dish> dishes = s.createQuery("from Dish d where d.id in (:ids)", Dish.class)
        .setParameterList("ids", ids)
        .list();
    return dishes.stream().collect(Collectors.toMap(Dish::getId, d->d));
  }
}
