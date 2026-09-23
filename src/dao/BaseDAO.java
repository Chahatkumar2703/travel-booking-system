package dao;

import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * BaseDAO provides connection management and reusable database routines.
 * Demonstrates Inheritance across DAO implementations.
 */
public abstract class BaseDAO {

    /**
     * Obtains an active connection from DatabaseConnection.
     */
    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
}
