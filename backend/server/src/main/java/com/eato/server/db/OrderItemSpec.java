package com.eato.server.db;

public class OrderItemSpec {
  public long dishId;
  public int qty;
  public OrderItemSpec() {}
  public OrderItemSpec(long dishId, int qty) { this.dishId = dishId; this.qty = qty; }
}
