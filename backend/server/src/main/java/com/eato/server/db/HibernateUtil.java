package com.eato.server.db;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.net.URL;

public class HibernateUtil {
  private static final SessionFactory SESSION_FACTORY = build();

  private static SessionFactory build() {
    try {
      // Prove the resource is visible on classpath
      URL res = Thread.currentThread().getContextClassLoader().getResource("hibernate.cfg.xml");
      System.out.println("hibernate.cfg.xml resource = " + res);

      Configuration cfg = new Configuration();
      // Explicit path from classpath root:
      cfg.configure("hibernate.cfg.xml");

      return cfg.buildSessionFactory();
    } catch (Throwable ex) {
      // Print the *real* reason
      System.err.println("Failed to build SessionFactory: " + ex);
      if (ex.getCause() != null) System.err.println("Cause: " + ex.getCause());
      ex.printStackTrace();
      throw new ExceptionInInitializerError(ex);
    }
  }

  public static SessionFactory getSessionFactory() {
    return SESSION_FACTORY;
  }
}

