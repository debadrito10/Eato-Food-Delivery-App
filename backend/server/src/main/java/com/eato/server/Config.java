package com.eato.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
  private static final Properties PROPS = new Properties();

  static {
    try (FileInputStream fis = new FileInputStream("backend/application.properties")) {
      PROPS.load(fis);
      PROPS.forEach((k,v) -> System.setProperty(k.toString(), v.toString()));
    } catch (IOException ignored) {}
  }

  public static String get(String key, String def) {
    return System.getProperty(key, PROPS.getProperty(key, def));
  }

  public static int getInt(String key, int def) {
    try { return Integer.parseInt(get(key, String.valueOf(def))); }
    catch (Exception e) { return def; }
  }
}
