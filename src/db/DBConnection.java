package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author knol
 * @version 2018-08-30
 */

public class DBConnection {
	
	private Connection connection = null;
	private static DBConnection dbConnection;
	
	private static final String driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	private static final String dbName = "DMA-CSD-S211_10121167";
	private static final String serverAddress = "hildur.ucn.dk";
	private static final int    serverPort = 1433;
	private static final String userName = "DMA-CSD-S211_10121167";
	private static final String password = "Password1!";
	
	/**
	 * Part of a singleton design pattern.
	 * The constructor of the class.
	 * Tries to create connection with the database
	 */
	
	private DBConnection() {
		String connectionString = String.format("jdbc:sqlserver://%s:%d;databaseName=%s;user=%s;password=%s;encrypt=false", 
				serverAddress, serverPort, dbName, userName, password);
		try {
			Class.forName(driverClass);
			connection = DriverManager.getConnection(connectionString);
		} catch (ClassNotFoundException e) {
			System.err.println("Could not load JDBC driver");
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println("Could not connect to database " + dbName + "@" + serverAddress + ":" + serverPort + " as user " + userName + " using password ******");
			System.out.println("Connection string was: " + connectionString.substring(0, connectionString.length() - password.length()) + "....");
			e.printStackTrace();
		}
	}
	
	/**
	 * Part of a singleton design pattern.
	 * if dbConnection == null, creates new dbConnection
	 * @return dbConnection
	 */
	
	public static DBConnection getInstance() {
		if(dbConnection == null) {
			dbConnection = new DBConnection();
		}
		return dbConnection;
	}
	
	/**
	 * Other methods 
	 * @throws SQLException
	 */
	
	public void startTransaction() throws SQLException {
		connection.setAutoCommit(false);
	}
	
	public void commitTransaction() throws SQLException {
		connection.commit();
		connection.setAutoCommit(true);
	}
	
	public void rollbackTransaction() throws SQLException {
		connection.rollback();
		connection.setAutoCommit(true);
	}
	
	public int executeInsertWithIdentity(PreparedStatement ps) throws SQLException  {
		int res = -1;
		try {
			res = ps.executeUpdate();
			if(res > 0) {
				ResultSet rs = ps.getGeneratedKeys();
				rs.next();
				res = rs.getInt(1);
			}
		} catch (SQLException e) {
			throw e;
		}
		return res;
	}
	
	public int executeInsertWithIdentity(String sql) throws SQLException  {
		System.out.println("DBConnection, Inserting: " + sql);
		int res = -1;
		try (Statement s = connection.createStatement()) {
			res = s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			if(res > 0) {
				ResultSet rs = s.getGeneratedKeys();
				rs.next();
				res = rs.getInt(1);
			}
			//s.close(); -- the try block does this for us now
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
		return res;
	}
	
	
	public int executeUpdate(String sql) throws SQLException {
		System.out.println("DBConnection, Updating: " + sql);
		int res = -1;
		try (Statement s = connection.createStatement()){
			res = s.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
		return res;
	}
	
	/**
	 * Checks if there is a connection between database or not
	 * @return Connection
	 */
	public Connection getConnection() {
		return connection;
	}
	
	/**
	 * Closes the connection with the database
	 */
	public void disconnect() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
