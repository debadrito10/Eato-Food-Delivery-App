package com.eato.server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Instant;
import java.util.Date;

public class AuthUtil {
  // Read from env or default for local dev; DO NOT commit real secret
  private static final String SECRET = System.getenv().getOrDefault("JWT_SECRET", "dev-secret-change-me");
  private static final Algorithm ALG = Algorithm.HMAC256(SECRET);
  private static final String ISS = System.getenv().getOrDefault("JWT_ISS", "eato");
  private static final long TTL_MIN =
      Long.parseLong(System.getenv().getOrDefault("JWT_TTL_MINUTES", "120"));
  private static final com.auth0.jwt.JWTVerifier VERIFIER = JWT.require(ALG)
      .withIssuer(ISS).build();
  public static String issueToken(long userId, String email) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(TTL_MIN * 60);
    return JWT.create()
        .withIssuer(ISS)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(exp)) 
        .withClaim("uid", userId)
        .withClaim("email", email == null ? "" : email.toLowerCase())
        .sign(ALG);
  }

  public static DecodedJWT verify(String token) {
    return VERIFIER.verify(token);
  }
}
