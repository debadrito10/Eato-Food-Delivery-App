package com.eato.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants")
public class Restaurant {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false) private String name;
  @Column(nullable = false) private String cuisine;
  @Column(nullable = false) private double lat;
  @Column(nullable = false) private double lng;

  private String city;
  private String pincode;

  @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Dish> dishes = new ArrayList<>();

  public Restaurant() {}
  public Restaurant(String name, String cuisine, double lat, double lng, String city, String pincode) {
    this.name = name; this.cuisine = cuisine; this.lat = lat; this.lng = lng; this.city = city; this.pincode = pincode;
  }

  public Long getId() { return id; }
  public String getName() { return name; } public void setName(String name) { this.name = name; }
  public String getCuisine() { return cuisine; } public void setCuisine(String cuisine) { this.cuisine = cuisine; }
  public double getLat() { return lat; } public void setLat(double lat) { this.lat = lat; }
  public double getLng() { return lng; } public void setLng(double lng) { this.lng = lng; }
  public String getCity() { return city; } public void setCity(String city) { this.city = city; }
  public String getPincode() { return pincode; } public void setPincode(String pincode) { this.pincode = pincode; }
  public List<Dish> getDishes() { return dishes; }
}
