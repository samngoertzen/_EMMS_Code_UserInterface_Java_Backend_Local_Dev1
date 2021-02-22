/**
 * 
 */
package database;

import java.sql.*;

/**
 * @author ZacheryHolsinger
 *
 */
public class dbConnection {
	
	// Driver settings
	static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    static final String DB_IP = "153.42.34.33";
    static final String DB_PORT = "3306";
    
    // Profile settings from /var/www/html/index.php
    static final String USER = "emmsdev";
    static final String PASS = "pumpkin";
    static final String Database = "EMMS";
    
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		testConnection();
	}
	
	
	/**
	 * Talks to Meters table in the database and gets all the meter ips!
	 */
	public static void updateIPs() {
		
	}
	
	
	/**
	 * Tests to see if connection can be established to configured database
	 * @return true if connection can be established
	 */
	public static boolean testConnection() {
		Connection conn = null;
        Statement stmt = null;
        try {
            //STEP 2: Register JDBC driver
            Class.forName("org.mariadb.jdbc.Driver");

            //STEP 3: Open a connection
            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(
                    "jdbc:mariadb://"+ DB_IP + ":" + DB_PORT + "/" + Database, USER, PASS);
            System.out.println("Connected database successfully...");
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            return false;
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            return false;
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            	return false;
            }// do nothing
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!"); 
		
		return true;
	}

}
