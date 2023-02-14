package com.saulpos.javafxcrudgenerator;

import com.saulpos.model.bean.*;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.Map;

public class DatabaseConnection {
    private static DatabaseConnection INSTANCE = null;
    public SessionFactory sessionFactory;

    private DatabaseConnection() {

    }

    public static DatabaseConnection getInstance() throws PropertyVetoException {
        if ( INSTANCE == null ){
            INSTANCE = new DatabaseConnection();
            INSTANCE.initialize();
        }
        return INSTANCE;
    }

    protected void initialize() throws PropertyVetoException {
        sessionFactory = new Configuration().configure()
                .addAnnotatedClass(Product.class) // TODO Add package
                .addAnnotatedClass(Barcode.class)
                .addAnnotatedClass(Storage.class)
                .addAnnotatedClass(Unit.class)
                .addAnnotatedClass(Discount.class)
                .addAnnotatedClass(Unit.class)
                .buildSessionFactory();
    }
    public void runHqlQuery(String query, Map<String, Object> parameters) throws PropertyVetoException {
        Session session = getInstance().sessionFactory.openSession();
        Transaction tx = null;
        try{
            tx = session.beginTransaction();

            org.hibernate.query.Query sessionQuery = session.createQuery(query);
            if ( parameters != null ){
                for ( String key : parameters.keySet() ){
                    sessionQuery.setParameter(key, parameters.get(key));
                }
            }
            sessionQuery.executeUpdate();

            tx.commit();
        }catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            throw e;
        }finally {
            session.close();
        }
    }

    public List listHqlQuery(String query, Map<String, Object> parameters) throws PropertyVetoException {
        List ans = null;
        Session session = getInstance().sessionFactory.openSession();
        Transaction tx = null;
        try{
            tx = session.beginTransaction();

            org.hibernate.query.Query sessionQuery = session.createQuery(query);
            if ( parameters != null ){
                for ( String key : parameters.keySet() ){
                    sessionQuery.setParameter(key, parameters.get(key));
                }
            }
            ans = sessionQuery.list();

            tx.commit();
        }catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            throw e;
        }finally {
            session.close();
        }

        return ans;
    }

    public List listAll(String entityName) throws PropertyVetoException {
        return listHqlQuery("FROM " + entityName, null);
    }

    public Integer createEntry(Object newEntry) throws PropertyVetoException {

        Session session = null;
        Transaction tx = null;
        Integer id = null;

        try
        {
            session = getInstance().sessionFactory.openSession();
            tx = session.beginTransaction();
            id = (Integer) session.save(newEntry);
            tx.commit();
        }
        catch (HibernateException e)
        {
            if (tx!=null)
                tx.rollback();
            throw e;
        }
        finally
        {
            session.close();

        }
        return id;
    }

    public void delete(Object entry) throws PropertyVetoException{
        Session session = getInstance().sessionFactory.openSession();
        Transaction tx = null;
        try{
            tx = session.beginTransaction();
            session.delete(entry);
            tx.commit();
        }catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            throw e;
        }finally {
            session.close();
        }
    }

    public void update(Object entry) throws PropertyVetoException {
        Session session = getInstance().sessionFactory.openSession();
        Transaction tx = null;
        try{
            tx = session.beginTransaction();
            session.update(entry);
            tx.commit();
        }catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            throw e;
        }finally {
            session.close();
        }
    }

    public void saveOrUpdate(Object entry) throws PropertyVetoException {
        Session session = getInstance().sessionFactory.openSession();
        Transaction tx = null;
        try{
            tx = session.beginTransaction();
            session.saveOrUpdate(entry);
            tx.commit();
        }catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            throw e;
        }finally {
            session.close();
        }
    }
}
