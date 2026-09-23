package dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Generic DAO Interface demonstrating Interface abstraction and Generics.
 *
 * @param <T> Model entity type
 */
public interface GenericDAO<T> {

    T findById(int id) throws SQLException;

    List<T> findAll() throws SQLException;

    boolean save(T entity) throws SQLException;

    boolean update(T entity) throws SQLException;

    boolean delete(int id) throws SQLException;
}
