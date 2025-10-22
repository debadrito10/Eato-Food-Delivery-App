package com.eato.server.dto;

import com.eato.server.model.Dish;
import com.eato.server.model.Restaurant;
import java.util.List;
import java.util.stream.Collectors;

public class RestaurantMapper {
  public static RestaurantListDto toListDto(Restaurant r){
    var d=new RestaurantListDto();
    d.id=r.getId(); d.name=r.getName(); d.cuisine=r.getCuisine();
    d.city=r.getCity(); d.pincode=r.getPincode(); d.lat=r.getLat(); d.lng=r.getLng();
    return d;
  }
  public static RestaurantDetailDto toDetailDto(Restaurant r){
    var d=new RestaurantDetailDto();
    d.id=r.getId(); d.name=r.getName(); d.cuisine=r.getCuisine();
    d.city=r.getCity(); d.pincode=r.getPincode(); d.lat=r.getLat(); d.lng=r.getLng();
    d.dishes=r.getDishes().stream().map(RestaurantMapper::dish).collect(Collectors.toList());
    return d;
  }
  private static DishDto dish(Dish x){
    var d=new DishDto(); d.id=x.getId(); d.name=x.getName(); d.price=x.getprice(); return d;
  }
  public static List<RestaurantListDto> toListDtos(List<Restaurant> rs){
    return rs.stream().map(RestaurantMapper::toListDto).collect(Collectors.toList());
  }
}
