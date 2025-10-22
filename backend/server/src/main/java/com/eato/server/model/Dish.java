package com.eato.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "dishes")
public class Dish {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "restaurant_id", nullable = false)
  private Restaurant restaurant;

  @Column(nullable = false) private String name;
  @Column(name = "price", nullable = false) private int price;

  public Dish() {}
  public Dish(Restaurant restaurant, String name, int price) {
    this.restaurant = restaurant; this.name = name; this.price = price;
  }

  public Long getId() { return id; }
  public Restaurant getRestaurant() { return restaurant; } public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }
  public String getName() { return name; } public void setName(String name) { this.name = name; }
  public int getprice() { return price; } public void setprice(int price) { this.price = price; }
}
