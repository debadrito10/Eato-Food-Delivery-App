package com.eato.server.dto;

public class AuthDtos {
  public static class RegisterReq { public String email; public String password; }
  public static class LoginReq { public String email; public String password; }
  public static class TokenResp { public String token; public String email; public long userId;
    public TokenResp(String t, String e, long id){ this.token=t; this.email=e; this.userId=id; } }
  public static class MeResp { public long id; public String email; }
}
