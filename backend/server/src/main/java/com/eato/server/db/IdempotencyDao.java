package com.eato.server.db;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Optional;

public class IdempotencyDao {

  /** @return true if inserted; false if key already exists */
  public boolean tryInsert(Session s, String idemKey, long userId) {
    Transaction tx = s.beginTransaction();
    try {
      int rows = s.createNativeMutationQuery(
          "INSERT INTO idempotency_keys(id, user_id) VALUES (:k, :u)")
          .setParameter("k", idemKey)
          .setParameter("u", userId)
          .executeUpdate();
      tx.commit();
      return rows == 1;
    } catch (Exception e) {
      if (tx.isActive()) tx.rollback(); // likely PK conflict (23505)
      return false;
    }
  }

  /** @return existing orderId or null */
  public Long getOrderId(Session s, String key) {
    Optional<Long> res = s.createNativeQuery(
            "SELECT order_id FROM idempotency_keys WHERE id=:k", Long.class)
        .setParameter("k", key)
        .uniqueResultOptional();
    return res.orElse(null);
  }

  public void setOrderId(Session s, String key, long orderId) {
    Transaction tx = s.beginTransaction();
    try {
      s.createNativeMutationQuery(
          "UPDATE idempotency_keys SET order_id=:o WHERE id=:k")
          .setParameter("o", orderId)
          .setParameter("k", key)
          .executeUpdate();
      tx.commit();
    } catch (Exception e) {
      if (tx.isActive()) tx.rollback();
      throw e;
    }
  }
}
