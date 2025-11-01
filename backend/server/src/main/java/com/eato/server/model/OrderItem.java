package com.eato.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(name = "dish_id", nullable = false)
  private Long dishId;

  @Column(nullable = false)
  private int qty;

  @Column(name = "price_at_order", nullable = false)
  private int priceAtOrder;

  public OrderItem() {}

  public Long getId() { return id; }

  public Order getOrder() { return order; }
  public void setOrder(Order order) { this.order = order; }

  public Long getDishId() { return dishId; }
  public void setDishId(Long dishId) { this.dishId = dishId; }

  public int getQty() { return qty; }
  public void setQty(int qty) { this.qty = qty; }

  public int getPriceAtOrder() { return priceAtOrder; }
  public void setPriceAtOrder(int priceAtOrder) { this.priceAtOrder = priceAtOrder; }
}
