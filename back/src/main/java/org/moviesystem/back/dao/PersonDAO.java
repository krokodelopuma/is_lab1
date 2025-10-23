package org.moviesystem.back.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.moviesystem.back.model.Person;

import java.util.List;

@ApplicationScoped
public class PersonDAO {
    
    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;
    
    public List<Person> findAll() {
        TypedQuery<Person> query = entityManager.createQuery(
            "SELECT p FROM Person p ORDER BY p.name", 
            Person.class);
        return query.getResultList();
    }
    
    public Person findById(Long id) {
        return entityManager.find(Person.class, id);
    }
    
    public Person create(Person person) {
        entityManager.getTransaction().begin();
        try {
            entityManager.persist(person);
            entityManager.flush();
            entityManager.getTransaction().commit();
            return person;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }
    
    public Person update(Person person) {
        entityManager.getTransaction().begin();
        try {
            Person updatedPerson = entityManager.merge(person);
            entityManager.flush();
            entityManager.getTransaction().commit();
            return updatedPerson;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }
    
    public void delete(Long id) {
        entityManager.getTransaction().begin();
        try {
            Person person = entityManager.find(Person.class, id);
            if (person != null) {
                entityManager.remove(person);
                entityManager.flush();
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }
}
