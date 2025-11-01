package com.eato.server.dto;

import com.eato.server.model.Order;
import com.eato.server.model.OrderItem;
import com.eato.server.model.Dish;
import org.hibernate.Session;

import java.util.*;
import java.util.stream.Collectors;

public class OrderMapper {

  public static OrderDtos.OrderListItem toListItem(Order o){
    var v = new OrderDtos.OrderListItem();
    v.id = o.getId();
    v.total = o.getTotal();
    v.createdAt = o.getCreatedAt();
    return v;
  }

  public static List<OrderDtos.OrderListItem> toListItems(List<Order> orders){
    return orders.stream().map(OrderMapper::toListItem).collect(Collectors.toList());
  }

  /** Builds detail DTO and resolves dish names in a single IN() fetch */
  public static OrderDtos.OrderDetail toDetail(Session s, Order o){
    var dto = new OrderDtos.OrderDetail();
    dto.id = o.getId();
    dto.total = o.getTotal();
    dto.createdAt = o.getCreatedAt();
    dto.address = o.getAddress();

    List<OrderItem> items = o.getItems();
    if (items == null || items.isEmpty()) return dto;

    Set<Long> ids = items.stream().map(OrderItem::getDishId).collect(Collectors.toSet());
    Map<Long,String> nameById = fetchDishNames(s, ids);

    for (OrderItem it : items){
      var di = new OrderDtos.OrderDetailItem();
      di.dishId = it.getDishId();
      di.name = nameById.getOrDefault(it.getDishId(), "Dish");
      di.qty = it.getQty();
      di.priceAtOrder = it.getPriceAtOrder();
      dto.items.add(di);
    }
    return dto;
  }

  private static Map<Long,String> fetchDishNames(Session s, Collection<Long> dishIds){
    if (dishIds.isEmpty()) return Map.of();
    List<Object[]> rows = s.createQuery(
        "select d.id, d.name from Dish d where d.id in (:ids)", Object[].class)
        .setParameterList("ids", dishIds).list();
    Map<Long,String> m = new HashMap<>();
    for (Object[] r : rows){
      m.put((Long) r[0], (String) r[1]);
    }
    return m;
  }
}
