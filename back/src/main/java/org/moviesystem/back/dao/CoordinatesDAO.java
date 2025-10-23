package org.moviesystem.back.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.moviesystem.back.model.Coordinates;

import java.util.List;

@ApplicationScoped
public class CoordinatesDAO {
    
    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;
    
    public List<Coordinates> findAll() {
        TypedQuery<Coordinates> query = entityManager.createQuery(
            "SELECT c FROM Coordinates c ORDER BY c.id", 
            Coordinates.class);
        return query.getResultList();
    }
    
    public Coordinates findById(Long id) {
        return entityManager.find(Coordinates.class, id);
    }
    
    public Coordinates findByXAndY(Integer x, Double y) {
        TypedQuery<Coordinates> query = entityManager.createQuery(
            "SELECT c FROM Coordinates c WHERE c.x = :x AND c.y = :y", 
            Coordinates.class);
        query.setParameter("x", x);
        query.setParameter("y", y);
        
        List<Coordinates> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
    
    public Coordinates create(Coordinates coordinates) {
        entityManager.getTransaction().begin();
        try {
            entityManager.persist(coordinates);
            entityManager.flush();
            entityManager.getTransaction().commit();
            return coordinates;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }
    
    public Coordinates update(Coordinates coordinates) {
        entityManager.getTransaction().begin();
        try {
            Coordinates updatedCoordinates = entityManager.merge(coordinates);
            entityManager.flush();
            entityManager.getTransaction().commit();
            return updatedCoordinates;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }
    
    public void delete(Long id) {
        entityManager.getTransaction().begin();
        try {
            Coordinates coordinates = entityManager.find(Coordinates.class, id);
            if (coordinates != null) {
                entityManager.remove(coordinates);
                entityManager.flush();
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }
}
