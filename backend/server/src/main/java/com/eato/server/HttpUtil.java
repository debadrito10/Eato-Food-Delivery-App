package com.eato.server;

import com.sun.net.httpserver.HttpExchange;
import java.net.URI; import java.net.URLDecoder; import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.IOException;  
public class HttpUtil {
  public static Map<String,List<String>> queryParams(HttpExchange ex){
    URI uri=ex.getRequestURI(); String q=uri.getRawQuery(); Map<String,List<String>> map=new HashMap<>();
    if(q==null || q.isBlank()) return map;
    for(String pair:q.split("&")){
      int i=pair.indexOf('='); String k=i>0?dec(pair.substring(0,i)):dec(pair);
      String v=i>0 && pair.length()>i+1?dec(pair.substring(i+1)):"";
      map.computeIfAbsent(k,kk->new ArrayList<>()).add(v);
    } return map;
  }
  public static String qp(Map<String,List<String>> qp,String key,String def){
    var v=qp.get(key); return (v==null||v.isEmpty()||v.get(0).isBlank())?def:v.get(0);
  }
  public static int qpi(Map<String,List<String>> qp,String key,int def){
    try{ return Integer.parseInt(qp(qp,key,String.valueOf(def))); }catch(Exception e){ return def; }
  }
  private static String dec(String s){ try{ return URLDecoder.decode(s, StandardCharsets.UTF_8.name()); }catch(Exception e){ return s; } }

public static <T> T readJson(HttpExchange ex, Class<T> clazz) throws IOException {
  try (var is = ex.getRequestBody()) {
    return com.eato.server.Json.MAPPER.readValue(is, clazz);
  }
}
public static boolean isOptions(HttpExchange ex) {
  return "OPTIONS".equalsIgnoreCase(ex.getRequestMethod());
}

public static boolean methodIs(HttpExchange ex, String expected) {
  return expected.equalsIgnoreCase(ex.getRequestMethod());
}

/** Returns token without the "Bearer " prefix, or null if absent/invalid */
public static String bearerToken(HttpExchange ex) {
  String h = ex.getRequestHeaders().getFirst("Authorization");
  if (h == null) return null;
  if (h.regionMatches(true, 0, "Bearer ", 0, 7)) {
    return h.substring(7).trim();
  }
  return null;
}

}
