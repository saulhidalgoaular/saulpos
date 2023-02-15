package com.saulpos.javafxcrudgenerator;

import jakarta.persistence.Entity;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseConnection {
    private static DatabaseConnection INSTANCE = null;
    public SessionFactory sessionFactory;

    private DatabaseConnection() {

    }

    public static DatabaseConnection getInstance() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        if ( INSTANCE == null ){
            INSTANCE = new DatabaseConnection();
            INSTANCE.initialize();
        }
        return INSTANCE;
    }

    protected void initialize() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        Configuration configuration = new Configuration().configure();
        for (Class cls : getEntityClassesFromPackage("com.saulpos.model.bean")) {
            configuration.addAnnotatedClass(cls);
        }
        sessionFactory = configuration.buildSessionFactory();
    }
    public void runHqlQuery(String query, Map<String, Object> parameters) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
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
        }catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }finally {
            session.close();
        }
    }

    public List listHqlQuery(String query, Map<String, Object> parameters) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
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
        }catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }finally {
            session.close();
        }

        return ans;
    }

    public List listAll(String entityName) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        if (entityName == null){
            return new ArrayList();
        }
        return listHqlQuery("FROM " + entityName, null);
    }

    public Integer createEntry(Object newEntry) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {

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
        catch (Exception e)
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

    // https://stackoverflow.com/questions/8122792/add-annotated-class-in-hibernate-by-adding-all-classes-in-some-package-java
    public static List<Class<?>> getEntityClassesFromPackage(String packageName) throws ClassNotFoundException, IOException, URISyntaxException {
        List<String> classNames = getClassNamesFromPackage(packageName);
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (String className : classNames) {
            Class<?> cls = Class.forName(packageName + "." + className);
            Annotation[] annotations = cls.getAnnotations();

            for (Annotation annotation : annotations) {
                if (annotation instanceof Entity) {
                    classes.add(cls);
                }
            }
        }

        return classes;
    }

    public static ArrayList<String> getClassNamesFromPackage(String packageName) throws IOException, URISyntaxException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ArrayList<String> names = new ArrayList<String>();

        packageName = packageName.replace(".", "/");
        System.out.println(packageName);
        URL packageURL = classLoader.getResource(packageName);

        URI uri = new URI(packageURL.toString());
        File folder = new File(uri.getPath());
        File[] files = folder.listFiles();
        for (File file: files) {
            String name = file.getName();
            name = name.substring(0, name.lastIndexOf('.'));  // remove ".class"
            names.add(name);
        }

        return names;
    }

    public void delete(Object entry) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        Session session = getInstance().sessionFactory.openSession();
        Transaction tx = null;
        try{
            tx = session.beginTransaction();
            session.delete(entry);
            tx.commit();
        }catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }finally {
            session.close();
        }
    }

    public void update(Object entry) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        Session session = getInstance().sessionFactory.openSession();
        Transaction tx = null;
        try{
            tx = session.beginTransaction();
            session.update(entry);
            tx.commit();
        }catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }finally {
            session.close();
        }
    }

    public void saveOrUpdate(Object entry) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        Session session = getInstance().sessionFactory.openSession();
        Transaction tx = null;
        try{
            tx = session.beginTransaction();
            session.saveOrUpdate(entry);
            tx.commit();
        }catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }finally {
            session.close();
        }
    }
}