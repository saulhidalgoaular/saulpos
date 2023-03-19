package com.saulpos.model.dao;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBean;
import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementationSoftDelete;
import com.saulpos.javafxcrudgenerator.model.dao.AbstractDataProvider;
import com.saulpos.model.bean.UserB;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import javafx.beans.property.Property;

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

    public EntityManagerFactory entityManagerFactory;

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
        entityManagerFactory = Persistence.createEntityManagerFactory("jpa-saulpos");
    }

    @Deprecated
    public void runHqlQuery(String query, Map<String, Object> parameters) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        /*Transaction tx = null;
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
        }*/
    }

    @Deprecated
    public List listHqlQuery(String query, Map<String, Object> parameters) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        /*List ans = null;
        Session session = getInstance().sessionFactory.openSession();

        Transaction tx = null;
        try{
            tx = session.beginTransaction();


            Query entityQuery =  entityManagerFactory.createEntityManager().createQuery(query);
            if ( parameters != null ){
                for ( String key : parameters.keySet() ){
                    entityQuery.setParameter(key, parameters.get(key));
                }
            }
            ans = entityQuery.getResultList();

            tx.commit();
        }catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }finally {
            session.close();
        }

        return ans;*/
        return null;
    }

    public List listAll(String entityName) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        if (entityName == null){
            return new ArrayList();
        }
        return listHqlQuery("FROM " + entityName, null);
    }

    public void createEntry(Object newEntry) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        EntityTransaction transaction = null;
        try
        {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            transaction = entityManager.getTransaction();
            transaction.begin();

            entityManager.persist(newEntry);
            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction!=null)
                transaction.rollback();
            throw e;
        }
    }

    public List listBySample(Class clazz, AbstractBean sample, AbstractDataProvider.SearchType type)  throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {

        try {
            CriteriaBuilder builder = entityManagerFactory.getCriteriaBuilder();
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            CriteriaQuery<? extends AbstractBean> query = builder.createQuery(sample.getClass());
            Root<? extends AbstractBean> root = query.from(sample.getClass());

            // Add restrictions based on annotated fields
            final Field[] allFields = sample.getClass().getDeclaredFields();
            List<Predicate> restrictions = new ArrayList<>();
            for (Field field : allFields) {
                final Property invoke = (Property) sample.getClass().getDeclaredMethod(field.getName() + "Property").invoke(sample);
                Object value = invoke.getValue();
                if (value != null) {
                    if (!(value instanceof String) || ((String) value).isBlank()){
                        continue;
                    }
                    // Add restriction
                    String searchString = (String) value;
                    if (AbstractDataProvider.SearchType.LIKE.equals(type)) {
                        restrictions.add(
                                builder.like(root.get(field.getName()), "%" + searchString + "%")
                        );
                    }else if (AbstractDataProvider.SearchType.EQUAL.equals(type)){
                        restrictions.add(
                                builder.equal(root.get(field.getName()), searchString)
                        );
                    }
                }
            }
            restrictions.add(builder.equal(root.get("beanStatus"), AbstractBeanImplementationSoftDelete.BeanStatus.Active));

            query.where(restrictions.toArray(new Predicate[0]));

            List results = entityManager.createQuery(query).getResultList();
            System.out.println("Results");
            entityManager.close();
            return results;
        }
        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
        {
            e.printStackTrace();
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
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try{
            entityManager.remove(entry);

        }catch (Exception e) {

            throw e;
        }finally {
            if (entityManager != null){
                entityManager.close();
            }
        }
    }

    public void update(Object entry) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try{
            entityManager.getTransaction().begin();
            entityManager.merge(entry);
            entityManager.getTransaction().commit();
        }catch (Exception e) {
            throw e;
        }finally {
            if (entityManager != null){
                entityManager.close();
            }
        }
    }

    public void saveOrUpdate(Object entry) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try{
            entityManagerFactory.createEntityManager().merge(entry);
        }catch (Exception e) {
            throw e;
        }finally {
            if (entityManager != null){
                entityManager.close();
            }
        }
    }

    public void saveDefaultValues() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        UserB admin = new UserB();
        admin.setUserName("admin");
        admin.setPassword("admin");
        admin.hashPassword();
        admin.save();
    }
}
