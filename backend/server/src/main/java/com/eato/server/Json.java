package com.eato.server;
import com.fasterxml.jackson.databind.ObjectMapper;
public class Json {
  public static final ObjectMapper M = new ObjectMapper().findAndRegisterModules();
}
