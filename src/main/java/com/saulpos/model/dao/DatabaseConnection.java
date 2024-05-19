/*
 * Copyright (C) 2012-2023 Saúl Hidalgo <saulhidalgoaular at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.saulpos.model.dao;

import com.saulpos.javafxcrudgenerator.model.Function;
import com.saulpos.javafxcrudgenerator.model.dao.AbstractBean;
import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementationSoftDelete;
import com.saulpos.javafxcrudgenerator.model.dao.AbstractDataProvider;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

    public List<Object[]> runQuery(String query){
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        return entityManager.createNativeQuery(query).getResultList();
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

    public List listBySample(Class clazz, AbstractBean sample, AbstractDataProvider.SearchType type, Function beforeCloseEntity) throws Exception {
        CriteriaBuilder builder = entityManagerFactory.getCriteriaBuilder();
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        Class<? extends AbstractBean> aClass = sample != null ? sample.getClass() : clazz;
        CriteriaQuery<? extends AbstractBean> query = builder.createQuery(aClass);
        Root<? extends AbstractBean> root = query.from(aClass);

        // Add restrictions based on annotated fields
        final Field[] allFields = aClass.getDeclaredFields();
        List<Predicate> restrictions = new ArrayList<>();
        for (Field field : allFields) {
            if (sample == null){
                continue;
            }
            final Property invoke = (Property) aClass.getDeclaredMethod(field.getName() + "Property").invoke(sample);
            Object value = invoke.getValue();
            if (value == null){
                continue;
            }
            addStringRestriction(type, builder, root, restrictions, field, value);
            addBeanRestriction(type, builder, root, restrictions, field, value);
        }
        restrictions.add(builder.equal(root.get("beanStatus"), AbstractBeanImplementationSoftDelete.BeanStatus.Active));

        query.where(restrictions.toArray(new Predicate[0]));

        List results = entityManager.createQuery(query).getResultList();
        System.out.println("Results: " + results.size());

        if (beforeCloseEntity != null){
            beforeCloseEntity.run(new Object[]{results});
        }

        entityManager.close();
        return results;
    }

    private static void addBeanRestriction(AbstractDataProvider.SearchType type, CriteriaBuilder builder, Root<? extends AbstractBean> root, List<Predicate> restrictions, Field field, Object value) {
        if (!(value instanceof BeanImplementation bean)){
            return;
        }
        // Add restriction
        if (AbstractDataProvider.SearchType.LIKE.equals(type)) {
            restrictions.add(
                    builder.like(root.get(field.getName()), "%" + bean.getId() + "%")
            );
        }else if (AbstractDataProvider.SearchType.EQUAL.equals(type)){
            restrictions.add(
                    builder.equal(root.get(field.getName()), bean.getId())
            );
        }
    }

    private static void addStringRestriction(AbstractDataProvider.SearchType type, CriteriaBuilder builder, Root<? extends AbstractBean> root, List<Predicate> restrictions, Field field, Object value) {
        if (!(value instanceof String searchString) || ((String) value).isBlank()){
            return;
        }
        // Add restriction
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

    public List listBySample(Class clazz, AbstractBean sample, AbstractDataProvider.SearchType type) throws Exception {
        return listBySample(clazz, sample, type, null);
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

        URI uri = null;
        if (packageURL != null) {
            uri = new URI(packageURL.toString());
        }
        if (uri == null){
            return names;
        }
        File folder = new File(uri.getPath());
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file: files) {
                String name = file.getName();
                name = name.substring(0, name.lastIndexOf('.'));  // remove ".class"
                names.add(name);
            }
        }

        return names;
    }

    public void delete(BeanImplementation entry) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try{
            entityManager.remove(entry);

        }finally {
            if (entityManager != null){
                entityManager.close();
            }
        }
    }

    public void update(BeanImplementation entry) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try{
            entityManager.getTransaction().begin();
            if (entry.getId() == 0) {
                entityManager.persist(entry);
            } else {
                entityManager.merge(entry);
            }
            entityManager.getTransaction().commit();
        } finally {
            if (entityManager != null){
                entityManager.close();
            }
        }
    }

    public void saveOrUpdate(BeanImplementation entry) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try{
            entityManagerFactory.createEntityManager().merge(entry);
        } finally {
            if (entityManager != null){
                entityManager.close();
            }
        }
    }

}
