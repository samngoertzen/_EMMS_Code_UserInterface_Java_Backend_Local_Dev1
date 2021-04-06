/**
 *  test update
 */
package database;

import java.sql.*;
import meter.InfoGET;

/**
 * @author ZacheryHolsinger
 *
 */
public class dbConnection {
	
	// Driver settings
	static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    static final String DB_IP = "153.42.35.209";
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
		//sendMySQL("SELECT * FROM Meters");
        getFrom(InfoGET.EMERGENCYBUTTON, "AB:CD:EF:gH");
	}

    /**
     * Converts the InfoGET enum to string field in the database
     * @author Bennett Andrews
     * @param field - Type InfoGET
     * @return String
     */
    public static String fieldFromInfoGET(InfoGET field) {

        switch (field) {

            case ALARM:
                return "";

            case CB_VERSION:
                return "";
            
            case DEBUG:
                return "";

            case EMERGENCYBUTTON:
                return "Emergency_button_state";

            case ENERGYALLOCATION:
                return "";

            case IP:
                return "";
            
            case LIGHTS:
                return "";
            
            case MAC:
                return "MAC";

            case PASSWORD:
                return "";
            
            case POWERDATA:
                return "";

            case POWERFAIL:
                return "";

            case RELAY:
                return "";

            case RESET_TIME:
                return "";
            
            case SSID:
                return "";

            case TIME:
                return "";

            default:
                return "";
        }
    }


    /**
     * Constructs a MySQL statement for fetching a specific datum and grabs it from the database.
     * @author Bennett Andrews
     * @return 
     */
    public static ResultSet getFrom(InfoGET field, String MAC) {
        String sfield = fieldFromInfoGET(field);
        String statement = "SELECT " + sfield + " FROM " + "Meters" + " WHERE (MAC ='" + MAC + "')";
        return sendMySQL(statement);
    }
	
	
	/**
	 * Talks to Meters table in the database and gets all the meter ips!
	 */
	public static void updateIPs() {
		
	}
	
	/**
	 * Executes mysql query on database and returns response
	 * @param statement
	 * @return
	 */
	public static ResultSet sendMySQL(String statement) {
		Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ResultSet returnrs = null;
        try {
            //STEP 2: Register JDBC driver
            Class.forName("org.mariadb.jdbc.Driver");

            //STEP 3: Open a connection
//            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(
                    "jdbc:mariadb://"+ DB_IP + ":" + DB_PORT + "/" + Database, USER, PASS);
//            System.out.println("Connected database successfully...");
            stmt = conn.createStatement();
            rs = stmt.executeQuery(statement);
            returnrs = rs;
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = rs.getString(i);
                    System.out.print(columnValue + " " + rsmd.getColumnName(i));
                }
                System.out.println("");
            }
            
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            return null;
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            return null;
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            	return null;
            }// do nothing
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore

                stmt = null;
            }
            
            //end finally try
        }//end try
//        System.out.println("Goodbye!"); 
		
		return returnrs;
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
        	//BENNETT YOU ARE AWESOME ALSO THIS COMMENT IS FOR GITHUB TESTING DELETE MEEEE
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
