package com.saulpos.model.dao;

import com.saulpos.javafxcrudgenerator.annotations.Search;
import com.saulpos.javafxcrudgenerator.model.dao.AbstractBean;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import javafx.beans.property.Property;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.persister.collection.mutation.RowMutationOperations;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

    public void initialize() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        Configuration configuration = new Configuration().configure();
        for (Class cls : getEntityClassesFromPackage("com.saulpos.model.bean")) {
            configuration.addAnnotatedClass(cls);
        }
        sessionFactory = configuration.buildSessionFactory();
    }

    @Deprecated
    public void runHqlQuery(String query, Map<String, Object> parameters) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        Session session = getInstance().sessionFactory.openSession();
        Transaction tx = null;
        try {
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

    @Deprecated
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

    public void createEntry(Object newEntry) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {

        Session session = null;
        Transaction tx = null;

        try
        {
            session = getInstance().sessionFactory.openSession();
            EntityManager entityManager = sessionFactory.createEntityManager();
            tx = session.beginTransaction();
            entityManager.persist(newEntry);
            tx.commit();
        }
        catch (Exception e)
        {
            if (tx!=null)
                tx.rollback();
            throw e;
        } finally {
            session.close();

        }
    }

    public List listBySample(Class clazz, AbstractBean sample)  throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        Session session = sessionFactory.openSession();
        EntityManager entityManager = sessionFactory.createEntityManager();
        //entityManager.find()
        try {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<? extends AbstractBean> query = builder.createQuery(sample.getClass());
            Root<? extends AbstractBean> root = query.from(sample.getClass());

            // Add restrictions based on annotated fields
            final Field[] allFields = sample.getClass().getDeclaredFields();
            List<Predicate> restrictions = new ArrayList<>();
            for (Field field : allFields) {
                if (field.isAnnotationPresent(Search.class)) {
                    final Property invoke = (Property) sample.getClass().getDeclaredMethod(field.getName() + "Property").invoke(sample);
                    if (invoke.getValue() != null) {
                        // Add restriction
                        String searchString = (String) invoke.getValue();
                        restrictions.add(builder.like(root.get("name"), "%" + searchString + "%"));
                    }
                }
            }

            query.where(restrictions.toArray(new Predicate[0]));
            List results = session.createQuery(query).getResultList();
            return results;
        }
        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
        {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return null;
    }
    /*
    public List listBySample(Class clazz, AbstractBean sample) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        Session session = null;

        try
        {
            session = getInstance().sessionFactory.openSession();

            EntityManager entityManager = sessionFactory.createEntityManager();
            //entityManager.find()
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(sample.getClass());

            final Field[] allFields = sample.getClass().getDeclaredFields();

            //Restrictions
            List<Predicate> allRestrictions = new ArrayList<>();
            for (Field field : allFields) {
                if (!field.isAnnotationPresent(Search.class)) {
                    continue;
                }
                try {
                    final Property invoke = (Property) sample.getClass().getDeclaredMethod(field.getName() + "Property").invoke(sample);
                    if (invoke.getValue() == null) {
                        continue;
                    }
                    // TODO check the 0 later
                    //criteriaBuilder.equal(store.get("storeID"), pStoreID)
                    //Restrictions.
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    // TODO FIX ME. This should not happen tho.
                    throw new RuntimeException(e);
                }
            }

            criteriaQuery.where(
                    criteriaBuilder.and(
                            // TODO HERE PREDICATES
                            // Example
                            // criteriaBuilder.equal(customer.get("customerID"), pCustomerID))
                    )
            );
            TypedQuery query = entityManager.createQuery(criteriaQuery);

            // TODO ASSIGN PARAMETER
            //query.setParameter(pStoreID, selectedStore.get().getStoreID());


            List result = query.getResultList();

            return result;

        }
        catch (PropertyVetoException | IOException | URISyntaxException | ClassNotFoundException e)
        {
            throw e;
        } finally {
            session.close();

        }
    }
     */

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
            session.remove(entry);
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
            session.merge(entry);
            tx.commit();
        }catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }finally {
            session.close();
        }
    }
}
