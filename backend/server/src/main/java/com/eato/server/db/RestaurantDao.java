package com.eato.server.db;

import com.eato.server.model.Restaurant;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class RestaurantDao {

  public List<Restaurant> search(Session s, String q, String city, int page, int size) {
    StringBuilder hql = new StringBuilder("from Restaurant r where 1=1 ");
    if (q != null && !q.isBlank()) {
      hql.append("and (lower(r.name) like :q or lower(r.cuisine) like :q) ");
    }
    if (city != null && !city.isBlank()) {
      hql.append("and r.city = :city ");
    }
    hql.append("order by r.name asc");

    var query = s.createQuery(hql.toString(), Restaurant.class);
    if (q != null && !q.isBlank()) query.setParameter("q", "%" + q.toLowerCase() + "%");
    if (city != null && !city.isBlank()) query.setParameter("city", city);

    query.setFirstResult(Math.max(page, 0) * Math.max(size, 1));
    query.setMaxResults(Math.max(size, 1));
    return query.getResultList();
  }

  public long count(Session s, String q, String city) {
    StringBuilder hql = new StringBuilder("select count(r.id) from Restaurant r where 1=1 ");
    if (q != null && !q.isBlank()) {
      hql.append("and (lower(r.name) like :q or lower(r.cuisine) like :q) ");
    }
    if (city != null && !city.isBlank()) {
      hql.append("and r.city = :city ");
    }

    var query = s.createQuery(hql.toString(), Long.class);
    if (q != null && !q.isBlank()) query.setParameter("q", "%" + q.toLowerCase() + "%");
    if (city != null && !city.isBlank()) query.setParameter("city", city);
    return query.getSingleResult();
  }

  public Optional<Restaurant> findById(Session s, long id) {
    Restaurant r = s.get(Restaurant.class, id);
    if (r == null) return Optional.empty();
    // touch lazy collection to initialize before closing session
    r.getDishes().size();
    return Optional.of(r);
  }

  // (Optional) Simple transaction wrapper if you later add writes
  public <T> T inTx(Session s, java.util.function.Function<Session,T> work) {
    Transaction tx = s.beginTransaction();
    try {
      T res = work.apply(s);
      tx.commit();
      return res;
    } catch (RuntimeException e) {
      tx.rollback();
      throw e;
    }
  }
}
