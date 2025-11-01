package com.eato.server.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class OrderDtos {
  public static class Item {
    public long dishId;
    public int qty;
  }

  public static class OrderCreateReq {
    public List<Item> items;
    public String address; // JSON string (optional)
  }

  public static class OrderCreateResp {
    public long orderId;
    public int total;
    public OrderCreateResp(long orderId, int total){ this.orderId=orderId; this.total=total; }
  }

  public static class OrderListItem {
    public long id;
    public int total;
    public Instant createdAt;
  }

  public static class OrderDetailItem {
    public long dishId;
    public String name;
    public int qty;
    public int priceAtOrder;
  }

  public static class OrderDetail {
    public long id;
    public int total;
    public Instant createdAt;
    public String address;
    public List<OrderDetailItem> items = new ArrayList<>();
  }
}
