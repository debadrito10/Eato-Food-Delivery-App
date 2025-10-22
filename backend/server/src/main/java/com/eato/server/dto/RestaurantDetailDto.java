package com.eato.server.dto;
import java.util.ArrayList; import java.util.List;
public class RestaurantDetailDto extends RestaurantListDto { public List<DishDto> dishes = new ArrayList<>(); }
