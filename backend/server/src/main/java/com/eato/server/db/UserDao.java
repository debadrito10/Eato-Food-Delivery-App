package com.eato.server.db;

import com.eato.server.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Optional;

public class UserDao {

  public Optional<User> findByEmail(Session s, String email) {
    var q = s.createQuery("from User where lower(email)=:e", User.class);
    q.setParameter("e", email.toLowerCase());
    return q.uniqueResultOptional();
  }

  public User save(Session s, User u) {
    Transaction tx = s.beginTransaction();
    s.persist(u);
    tx.commit();
    return u;
  }

  public Optional<User> findById(Session s, long id) {
    return Optional.ofNullable(s.get(User.class, id));
  }
}
